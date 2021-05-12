package self.robin.examples.utils.ibatis;

import com.alibaba.druid.pool.DruidDataSourceFactory;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.Validate;
import org.apache.ibatis.jdbc.SqlRunner;
import org.apache.ibatis.mapping.SqlCommandType;
import org.apache.ibatis.session.Configuration;
import self.robin.examples.utils.JdbcUtils;
import self.robin.examples.utils.json.JSONObject;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.Connection;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 默认使用 DruidDataSourceFactory 作为数据源
 * 本类依赖:
 * ali-fastjosn, mybaits,
 */
@Slf4j
public class CurdRunner implements CrudInterface{

    @Getter
    private DataSource dataSource;

    protected MappersParser mappersParser;
    /**
     * property文件中默认的 字段名。
     */
    private final String MAPPER_LOCATIONS_KEY = "mapper-locations";

    public CurdRunner(DataSource dataSource, Configuration configuration){
        this.mappersParser = new MappersParser(configuration);
        this.dataSource = dataSource;
    }

    public CurdRunner(Properties properties, Configuration configuration) throws Exception{
        this.mappersParser = new MappersParser(configuration);
        this.dataSource = DruidDataSourceFactory.createDataSource(properties);
    }

    public CurdRunner(Properties properties, String mapperLocations) throws Exception{
        this.mappersParser = new MappersParser(mapperLocations);
        this.mappersParser.parse();
        this.dataSource = DruidDataSourceFactory.createDataSource(properties);
    }

    public CurdRunner(Properties properties) throws Exception{
        String mapperLocations = properties.getProperty(MAPPER_LOCATIONS_KEY);
        this.mappersParser = new MappersParser(mapperLocations);
        this.mappersParser.parse();
        this.dataSource = DruidDataSourceFactory.createDataSource(properties);
    }

    @Override
    public int insert(String id, Object param){
        MappersParser.SqlModel sqlModel = mappersParser.getAndFlatParam(id, param);
        int effect =  modify(SqlCommandType.INSERT, sqlModel.getSql(), sqlModel.getParams());
        log.debug("[Effect] "+effect);
        return effect;
    }

    @Override
    public int update(String id, Object param){
        MappersParser.SqlModel sqlModel = mappersParser.getAndFlatParam(id, param);
        int effect = modify(SqlCommandType.UPDATE, sqlModel.getSql(), sqlModel.getParams());
        log.debug("[Effect] "+effect);
        return effect;
    }

    @Override
    public int delete(String id, Object param){
        MappersParser.SqlModel sqlModel = mappersParser.getAndFlatParam(id, param);
        int effect = modify(SqlCommandType.DELETE, sqlModel.getSql(), sqlModel.getParams());
        log.debug("[Effect] "+effect);
        return effect;
    }

    @Override
    public int delete(String sqlStr, Object[] params) {
        int effect = modify(SqlCommandType.DELETE, sqlStr, params);
        log.debug("[Effect] "+effect);
        return effect;
    }

    public int modify(SqlCommandType type, String sqlStr, Object[] params){
        Validate.notNull(params, "params 不能为null");

        log.debug("["+type+"] SQL:"+sqlStr);
        log.debug("["+type+"] Params"+ JSONObject.toJSONString(params));

        Connection conn = null;
        try {
            conn = getConnection();
            SqlRunner runner = new SqlRunner(conn);
            switch (type){
                case DELETE:
                    return runner.delete(sqlStr, params);
                case INSERT:
                    return runner.insert(sqlStr, params);
                case UPDATE:
                    return runner.update(sqlStr, params);
                default:
                    throw new RuntimeException("不支持的sql类型 "+type);
            }
        }catch (Exception e){
            e.printStackTrace();
            return 0;
        }finally {
            JdbcUtils.closeConnection(conn);
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
    public <T,E> T select(String id, Object param, Class<T> resultType, Class<E> elementType) {
        Objects.requireNonNull(param, "param 不能为null");
        MappersParser.SqlModel sqlModel = mappersParser.getAndFlatParam(id, param);
        return select(sqlModel.getSql(), sqlModel.getParams(), resultType, elementType);
    }

    private <T,E> T select(String sqlStr, Object[] params, Class<T> resultType, Class<E> elementType){
        log.debug("[Sql] "+sqlStr);
        log.debug("[Param] "+ JSONObject.toJSONString(params));

        //字段驼峰转下划线
        Map<String, Field> fieldMappings = getHumpFieldNameMappings(elementType);

        Connection conn = null;
        try {
            conn = getConnection();
            SqlRunner runner = new SqlRunner(conn);

            T retObj;
            if(Collection.class.isAssignableFrom(resultType)){
                List<Map<String, Object>> result = runner.selectAll(sqlStr, params);
                if(result==null){
                    return (T) new ArrayList<>();
                }

                List<E> retList = new ArrayList<>();
                for (Map<String, Object> stringObjectMap : result) {
                    if(isSingleType(resultType)){
                        if(result!=null && result.size()==1){
                            Object value = convert2LongIfBigDecimal(stringObjectMap.values().iterator().next());
                            retList.add(value==null ? null : (E)value);
                        }else {
                            throw new RuntimeException("结果集类型错误，"+result);
                        }
                    }else {
                        E element = mapToObject(stringObjectMap, elementType, fieldMappings);
                        retList.add(element);
                    }
                }
                log.debug("[Total] "+retList.size());
                retObj = (T) retList;
            }else {
                Map<String, Object> result = runner.selectOne(sqlStr, params);
                if(result==null || result.size()==0){
                    log.debug("[Total] "+result);
                    return null;
                }
                if(isSingleType(resultType)){
                    Object value = convert2LongIfBigDecimal(result.values().iterator().next());
                    retObj = (value==null) ? null : (T)value;
                }else {
                    E element = mapToObject(result, elementType, fieldMappings);
                    retObj = (T) element;
                }
                log.debug("[Total] 1");
            }
            return retObj;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }finally {
            JdbcUtils.closeConnection(conn);
        }
    }


    private Map<String, Field> getHumpFieldNameMappings(Class tClass){
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


    private <T> T toJavaObject(Map<String, Object> data, Class<T> javaClass) throws Exception{
        if(javaClass==null){
            throw new RuntimeException("javaClass 不能为空");
        }
        if(data==null || data.isEmpty()){
            return null;
        }
        T obj = javaClass.newInstance();
        for (Field declaredField : javaClass.getDeclaredFields()) {
            declaredField.setAccessible(true);
            Class<?> type = declaredField.getType();
        }
        return null;
    }

    private boolean isSingleType(Class<?> resultType){
        if(resultType.isPrimitive() || resultType==String.class || Number.class.isAssignableFrom(resultType)){
            return true;
        }
        return false;
    }

    private Object convert2LongIfBigDecimal(Object object){
        if(object==null){
            return null;
        }
        if(object.getClass() == BigDecimal.class){
            return ((BigDecimal)object).longValue();
        }
        return object;
    }

    private Connection getConnection() throws Exception{
        return dataSource.getConnection();
    }

    private final <T> T mapToObject(Map<String, Object> map, Class<T> tClass, Map<String, Field> fieldMappings){
        Field[] fields = tClass.getDeclaredFields();
        try {
            T obj = tClass.newInstance();
            Field field ;
            for (Map.Entry<String, Field> stringFieldEntry : fieldMappings.entrySet()) {
                field = stringFieldEntry.getValue();
                field.setAccessible(true);
                Object value = map.get(stringFieldEntry.getKey());
                field.set(obj, value);
            }
            return obj;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

}
