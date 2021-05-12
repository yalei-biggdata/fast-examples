package self.robin.examples.utils.query;

import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.jdbc.Null;
import org.apache.ibatis.type.TypeHandler;
import org.apache.ibatis.type.TypeHandlerRegistry;
import self.robin.examples.utils.JdbcUtils;
import self.robin.examples.utils.json.JSON;

import javax.sql.DataSource;
import java.io.Closeable;
import java.io.IOException;
import java.sql.*;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;

/**
 * @author mrli
 * @date 2020/7/27
 */
@Slf4j
public class QueryManager implements Closeable {

    protected final DataSource dataSource;

    /** key:taskId, value:future */
    protected Map<String, Task> taskMap;

    protected static ExecutorService commonPool;

    protected ZkOperator zkOperator;

    private final TypeHandlerRegistry typeHandlerRegistry;

    static {
        ForkJoinPool pool = ForkJoinPool.commonPool();
        commonPool = pool.getPoolSize()<2? Executors.newCachedThreadPool(): pool;
    }

    private Consumer<List<String>> taskIdsConsumer = taskIds -> {
        log.info("[QueryManager] trigger changes");
        //遍历
        for (String taskId : taskMap.keySet()) {
            if(!taskIds.contains(taskId)){
                Task task = taskMap.remove(taskId);
                task.cancel();
                log.info("[QueryManager] cancel task , taskId="+taskId);
            }
        }
    };

    public QueryManager(DataSource dataSource, ZkOperator zkOperator){
        this.dataSource = dataSource;
        this.taskMap = new ConcurrentHashMap<>();
        this.zkOperator = zkOperator;
        this.zkOperator.setChildChangesOperate(taskIdsConsumer);
        this.typeHandlerRegistry = new TypeHandlerRegistry();
    }

    /**
     * 提供同步查询presto （结果集是Map）
     *
     * @param sql 待执行的sql
     * @param timeout 超时设置
     * @return
     */
    public List<Map> query(String sql, int timeout) {
        return query(sql, timeout, Map.class);
    }

    /**
     * 执行同步查询
     * @param sql 待执行的sql
     * @param timeout 查询超时设定
     * @param tClass 查询结果对应的 class 类
     * @param <T>
     * @return
     */
    public <T> List<T> query(String sql, int timeout, Class<T> tClass){
        try(Connection conn = getConnection();
            Statement stmt = conn.createStatement())
        {
            stmt.setQueryTimeout(timeout);
            ResultSet rs = stmt.executeQuery(sql);
            List<T> data = getResults(rs, tClass);
            return data;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    protected Connection getConnection() throws SQLException {
        Connection connection = dataSource.getConnection();
        return connection;
    }

    /**
     * 异步查询,（注返回结果是Map类型）
     * @param sql 待执行的sql
     * @param timeout 查询超时设定(秒)
     * @param response 当查询完成后,会结果提供给consumer调用,
     *                 当throwable!=null时，查询发生了异常，返回结果为null
     */
    public String queryAsync(String sql, int timeout,
                             Consumer<QueryResponse<Map>> response) {
        return queryAsync(sql, null,timeout, Map.class, response);
    }

    /**
     * 异步查询
     * @param sql 待执行的sql
     * @param timeout 查询超时设定（秒）
     * @param resultType 查询结果对应的 class 类
     * @param response 当查询完成后,会结果提供给consumer调用
     *                 当throwable!=null时，查询发生了异常，返回结果为null
     * @param <T>
     *
     * @return 任务id
     */
    public <T> String queryAsync(String sql, int timeout, Class<T> resultType,
                                 Consumer<QueryResponse<T>> response) {
        return queryAsync(sql, null, timeout, resultType, response);
    }

    /**
     * 异步查询,（注返回结果是Map类型）
     * @param sql 待执行的sql
     * @param params 占位符对应的参数
     * @param timeout 查询超时设定(秒)
     * @param response 当查询完成后,会结果提供给consumer调用,
     *                 当throwable!=null时，查询发生了异常，返回结果为null
     */
    public String queryAsync(String sql, Object[] params, int timeout,
                             Consumer<QueryResponse<Map>> response) {
        return queryAsync(sql, params, timeout, Map.class, response);
    }

    /**
     * 异步查询
     * @param sql 待执行的sql
     * @param params sql中占位符待填充的变量
     * @param timeout 查询超时设定（秒）
     * @param resultType 查询结果对应的 class 类
     * @param response 当查询完成后,会结果提供给consumer调用
     *                 当throwable!=null时，查询发生了异常，返回结果为null
     * @param <T>
     *
     * @return 任务id
     */
    public <T> String queryAsync(String sql, Object[] params, int timeout, Class<T> resultType,
                                 Consumer<QueryResponse<T>> response) {

        try
        {
            log.debug("[Query Manager] sql="+sql+", params="+ JSON.toJSONString(params));
            //记录到zk
            String id =  zkOperator.applyId();
            //创建查询
            Connection conn = getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            setParameters(stmt, params);
            stmt.setQueryTimeout(timeout);

            //标记取消异常
            AtomicBoolean cancelFlag = new AtomicBoolean(false);

            CompletableFuture<List<T>> future = CompletableFuture.supplyAsync(() -> {
                try {
                    ResultSet resultSet = stmt.executeQuery();
                    List<T> datas = getResults(resultSet, resultType);
                    //关闭资源
                    JdbcUtils.closeResultSet(resultSet);
                    JdbcUtils.closeStatement(stmt);
                    JdbcUtils.closeConnection(conn);
                    return datas;
                } catch (Throwable e) {
                    if(cancelFlag.get()){
                       //如果是取消的话
                       throw new CancellationException("取消任务");
                    }
                    throw new RuntimeException(e);
                }
            }, commonPool);

            taskMap.put(id, new Task(future, conn, stmt, cancelFlag));
            future.whenComplete(((datas, throwable) -> {
                if(datas==null){
                    datas = new ArrayList<>();
                }
                //回调
                response.accept(new QueryResponse<>(id, sql, datas, throwable));
                //删除节点
                zkOperator.release(id);
            }));

            return id;
        }catch (Exception e){
            throw new RuntimeException(e);
        }
    }

    /**
     *
     * @param taskId 查询任务的id
     * @return
     */
    public boolean isRunning(String taskId){
        return zkOperator.exist(taskId);
    }

    /** 取消任务 */
    public void cancel(String taskId){
        zkOperator.release(taskId);
    }

    @Override
    public void close() throws IOException {
        this.zkOperator.close();
    }

    protected  <T> List<T> getResults(ResultSet resultSet, Class<T> tClass)
            throws SQLException
    {
        try(ResultSet rs = resultSet) {
            final TypeHandlerRegistry typeHandlerRegistry = new TypeHandlerRegistry();

            List<T> list = new ArrayList<>();

            List<String> columns = new ArrayList<>();
            List<TypeHandler<?>> typeHandlers = new ArrayList<>();
            ResultSetMetaData rsmd = rs.getMetaData();
            for (int i = 0, n = rsmd.getColumnCount(); i < n; i++) {
                columns.add(rsmd.getColumnLabel(i + 1));
                try {
                    Class<?> type = Resources.classForName(rsmd.getColumnClassName(i + 1));
                    TypeHandler<?> typeHandler = typeHandlerRegistry.getTypeHandler(type);
                    if (typeHandler == null) {
                        typeHandler = typeHandlerRegistry.getTypeHandler(Object.class);
                    }
                    typeHandlers.add(typeHandler);
                } catch (Exception e) {
                    typeHandlers.add(typeHandlerRegistry.getTypeHandler(Object.class));
                }
            }
            while (rs.next()) {
                Map<String, Object> row = new LinkedHashMap<>();
                for (int i = 0, n = columns.size(); i < n; i++) {
                    String name = columns.get(i);
                    TypeHandler<?> handler = typeHandlers.get(i);
                    row.put(name.toLowerCase(Locale.ENGLISH), handler.getResult(rs, name));
                }
                if (tClass == Map.class) {
                    list.add(((T) row));
                } else {
                    T obj = new JSONObject(row).toJavaObject(tClass);
                    list.add(obj);
                }
            }
            return list;
        }
    }

    /**
     * 保证任务可以正常取消
     */
    final class Task {

        private CompletableFuture future;

        private Connection connection;

        private Statement statement;

        private AtomicBoolean cancel;

        public Task(CompletableFuture future, Connection connection,
                    Statement statement, AtomicBoolean cancel) {
            this.future = future;
            this.connection = connection;
            this.statement = statement;
            this.cancel = cancel;
        }

        public void cancel() {
            if(this.cancel!=null){
                this.cancel.set(true);
            }
            JdbcUtils.closeStatement(statement);
            JdbcUtils.closeConnection(connection);
            future.cancel(true);
        }
    }

    private void setParameters(PreparedStatement ps, Object... args) throws SQLException {
        if(args==null || args.length==0){
            return;
        }

        for (int i = 0, n = args.length; i < n; i++) {
            if (args[i] == null) {
                throw new SQLException("SqlRunner requires an instance of Null to represent typed null values for JDBC compatibility");
            } else if (args[i] instanceof Null) {
                ((Null) args[i]).getTypeHandler().setParameter(ps, i + 1, null, ((Null) args[i]).getJdbcType());
            } else {
                TypeHandler typeHandler = typeHandlerRegistry.getTypeHandler(args[i].getClass());
                if (typeHandler == null) {
                    throw new SQLException("SqlRunner could not find a TypeHandler instance for " + args[i].getClass());
                } else {
                    typeHandler.setParameter(ps, i + 1, args[i], null);
                }
            }
        }
    }


}
