package self.robin.examples.utils.ibatis;

import com.google.gson.Gson;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.jdbc.SqlRunner;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.session.Configuration;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * crud执行类
 * 本类依赖:
 * ali-druid, mybaits,
 *
 * @author robin - li
 * @since 2019/8/3
 */
@Slf4j
public class CurdRunner implements CrudInterface {

    @Getter
    private DataSource dataSource;

    /**
     * 可以为空，如果使用xml存放sql时，此对象不能为空
     */
    protected MappersParser mappersParser;

    public MappersParser getMappersParser() {
        return mappersParser;
    }

    public CurdRunner(DataSource dataSource) {
        this(dataSource, (MappersParser) null);
    }

    public CurdRunner(DataSource dataSource, MappersParser mappersParser) {
        this.dataSource = dataSource;
        this.mappersParser = mappersParser;
    }

    public CurdRunner(DataSource dataSource, Configuration configuration) {
        this(dataSource, new MappersParser(configuration));
    }

    @Override
    public int insert(String id, Object param) {
        MappersParser.SqlModel sqlModel = mappersParser.getAndFlatParam(id, param);
        int effect = modify(SqlCommandType.INSERT, sqlModel.getSql(), sqlModel.getParams());
        log.debug("[Effect] " + effect);
        return effect;
    }

    @Override
    public int update(String id, Object param) {
        MappersParser.SqlModel sqlModel = mappersParser.getAndFlatParam(id, param);
        int effect = modify(SqlCommandType.UPDATE, sqlModel.getSql(), sqlModel.getParams());
        log.debug("[Effect] " + effect);
        return effect;
    }

    @Override
    public int delete(String id, Object param) {
        MappersParser.SqlModel sqlModel = mappersParser.getAndFlatParam(id, param);
        int effect = modify(SqlCommandType.DELETE, sqlModel.getSql(), sqlModel.getParams());
        log.debug("[Effect] " + effect);
        return effect;
    }

    @Override
    public int delete(String sqlStr, Object[] params) {
        int effect = modify(SqlCommandType.DELETE, sqlStr, params);
        log.debug("[Effect] " + effect);
        return effect;
    }

    public int modify(SqlCommandType type, String sqlStr, Object[] params) {
        Objects.requireNonNull(params, "params 不能为null");

        log.debug("[" + type + "] SQL:" + sqlStr);
        log.debug("[" + type + "] Params" + new Gson().toJson(params));

        Connection conn = null;
        try {
            conn = getConnection();
            SqlRunner runner = new SqlRunner(conn);
            switch (type) {
                case DELETE:
                    return runner.delete(sqlStr, params);
                case INSERT:
                    return runner.insert(sqlStr, params);
                case UPDATE:
                    return runner.update(sqlStr, params);
                default:
                    throw new RuntimeException("不支持的sql类型 " + type);
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            closeConnection(conn);
        }
    }

    @Override
    public boolean execute(String sql) {
        Connection conn = null;
        try {
            conn = getConnection();
            return conn.createStatement().execute(sql);
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            closeConnection(conn);
        }
    }

    @Override
    public <T> T selectOne(String sqlStr, Object[] params, Class<T> resultType) {
        return select(sqlStr, params, resultType, Map.class);
    }

    @Override
    public <T> List<T> selectList(String sqlStr, Object[] params, Class<T> resultType) {
        return select(sqlStr, params, List.class, resultType);
    }

    @Override
    public <T> T selectOne(String id, Object param, Class<T> resultType) {
        return select(id, param, resultType, Map.class);
    }

    @Override
    public <T> List<T> selectList(String id, Object param, Class<T> resultType) {
        return select(id, param, List.class, resultType);
    }

    @Override
    public <T, E> T select(String id, Object param, Class<T> resultType, Class<E> elementType) {
        MappersParser.SqlModel sqlModel = mappersParser.getAndFlatParam(id, param);
        return select(sqlModel.getSql(), sqlModel.getParams(), resultType, elementType);
    }

    private <T, E> T select(String sqlStr, Object[] params, Class<T> resultType, Class<E> elementType) {
        Objects.requireNonNull(params, "param 不能为null");
        Objects.requireNonNull(sqlStr, "sqlStr 不能为null");
        Objects.requireNonNull(resultType, "resultType 不能为null");
        Objects.requireNonNull(elementType, "elementType 不能为null");

        log.debug("[Sql] " + sqlStr);
        log.debug("[Param] " + new Gson().toJson(params));

        //字段驼峰转下划线
        Map<String, Field> fieldMappings = getHumpFieldNameMappings(elementType);

        Connection conn = null;
        try {
            conn = getConnection();
            SqlRunner runner = new SqlRunner(conn);

            T retObj;
            if (Collection.class.isAssignableFrom(resultType)) {
                List<Map<String, Object>> result = runner.selectAll(sqlStr, params);
                if (result == null || result.isEmpty()) {
                    return (T) new ArrayList<>(0);
                }

                List<E> retList = new ArrayList<>();
                for (Map<String, Object> objectMap : result) {
                    if (isSingleType(elementType)) {
                        if (objectMap != null && objectMap.size() == 1) {
                            Object value = convert2LongIfBigDecimal(objectMap.values().iterator().next());
                            retList.add(value == null ? null : (E) value);
                        } else {
                            throw new RuntimeException("结果集类型错误，" + objectMap);
                        }
                    } else {
                        E element = mapToObject(objectMap, elementType, fieldMappings);
                        retList.add(element);
                    }
                }
                log.debug("[Total] " + retList.size());
                retObj = (T) retList;
            } else {
                Map<String, Object> objectMap = runner.selectOne(sqlStr, params);
                if (objectMap == null || objectMap.isEmpty()) {
                    log.debug("[Total] " + objectMap);
                    return null;
                }
                if (isSingleType(elementType)) {
                    Object value = convert2LongIfBigDecimal(objectMap.values().iterator().next());
                    retObj = (value == null) ? null : (T) value;
                } else {
                    E element = mapToObject(objectMap, elementType, fieldMappings);
                    retObj = (T) element;
                }
                log.debug("[Total] 1");
            }
            return retObj;
        } catch (Exception e) {
            throw new RuntimeException(e);
        } finally {
            closeConnection(conn);
        }
    }

    private Map<String, Field> getHumpFieldNameMappings(Class tClass) {
        Field[] fields = tClass.getDeclaredFields();
        Map<String, Field> fieldMap = new HashMap<>();
        for (Field field : fields) {
            fieldMap.put(humpToLine2(field.getName()).toUpperCase(), field);
        }
        return fieldMap;
    }

    private Pattern humpPattern = Pattern.compile("[A-Z]");

    private String humpToLine2(String str) {
        Matcher matcher = humpPattern.matcher(str);
        StringBuffer sb = new StringBuffer();
        while (matcher.find()) {
            matcher.appendReplacement(sb, "_" + matcher.group(0).toLowerCase());
        }
        matcher.appendTail(sb);
        return sb.toString();
    }

    private boolean isSingleType(Class<?> resultType) {
        if (resultType.isPrimitive() || resultType == String.class || Number.class.isAssignableFrom(resultType)) {
            return true;
        }
        return false;
    }

    private Object convert2LongIfBigDecimal(Object object) {
        if (object == null) {
            return null;
        }
        if (object.getClass() == BigDecimal.class) {
            return ((BigDecimal) object).longValue();
        }
        return object;
    }

    private Connection getConnection() throws Exception {
        return dataSource.getConnection();
    }

    private final <T> T mapToObject(Map<String, Object> map, Class<T> tClass, Map<String, Field> fieldMappings)
            throws Exception {
        T obj = tClass.newInstance();
        Field field;
        for (Map.Entry<String, Field> fieldEntry : fieldMappings.entrySet()) {
            field = fieldEntry.getValue();
            field.setAccessible(true);
            Object value = map.get(fieldEntry.getKey());
            field.set(obj, value);
        }
        return obj;
    }

    public static void closeConnection(Connection con) {
        if (con != null) {
            try {
                con.close();
            } catch (SQLException ex) {
                log.debug("Could not close JDBC Connection", ex);
            } catch (Throwable ex) {
                log.debug("Unexpected exception on closing JDBC Connection", ex);
            }
        }
    }

}
