package self.robin.examples.utils.ibatis;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.reflect.FieldUtils;
import org.apache.ibatis.builder.xml.XMLConfigBuilder;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.session.Configuration;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import java.io.StringReader;
import java.lang.reflect.Field;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * mappers xml 解析器, 提取sql
 *
 * @author mrli
 * @date 2020/8/3
 */

@Slf4j
public class MappersParser {

    private String mappersPath;

    private Configuration configuration;

    public MappersParser(String mappersPath) {
        this.mappersPath = Objects.requireNonNull(mappersPath);
        try {
            this.configuration = innerParse();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public MappersParser(Configuration configuration) {
        this.configuration = Objects.requireNonNull(configuration);
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    /**
     * 构造配置文件
     */
    private Configuration innerParse() throws Exception {
        if (StringUtils.isBlank(mappersPath)) {
            throw new RuntimeException("mapperLocations is null");
        }

        StringBuilder sbd = new StringBuilder();
        sbd.append("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>");
        sbd.append("<!DOCTYPE configuration  PUBLIC \"-//mybatis.org//DTD Config 3.0//EN\" \"http://mybatis.org/dtd/mybatis-3-config.dtd\">");
        sbd.append("<configuration>");

        //下划线转驼峰
        sbd.append("<settings>");
        sbd.append("<setting name=\"mapUnderscoreToCamelCase\" value=\"true\"/>");
        sbd.append("</settings>");

        sbd.append(" <mappers>");

        ResourcePatternResolver resourceResolver = new PathMatchingResourcePatternResolver();
        Resource[] res = resourceResolver.getResources(mappersPath);
        for (Resource resource : res) {
            String fileClasspath = pickClasspath(resource.getURI());
            sbd.append("<mapper resource=\"" + fileClasspath + "\"/>");
        }
        sbd.append(" </mappers>");

        sbd.append("</configuration>");
        XMLConfigBuilder builder = new XMLConfigBuilder(new StringReader(sbd.toString()));
        return builder.parse();
    }

    private static String pickClasspath(URI fileUri) {
        String uri = fileUri.toString();
        uri = StringUtils.substringAfterLast(uri, "/classes");
        return StringUtils.substringAfter(uri, "/");
    }

    /**
     * 将对象解析为，sql语句中对应的变量
     *
     * @param id
     * @param param
     * @return
     */
    public SqlModel getAndFlatParam(String id, Object param) {
        log.debug("[DB] id=" + id + ", databseId=" + configuration.getDatabaseId());
        MappedStatement ms = configuration.getMappedStatement(id);
        Objects.requireNonNull(ms, "未找到id为" + id + "的sql定义");
        BoundSql boundSql = ms.getBoundSql(param);
        Objects.requireNonNull(boundSql, "未找到id为" + id + "，且参数类型为" + param.getClass() + "的sql定义");

        String sql = boundSql.getSql();
        Class<?> clazz = param.getClass();

        List<Object> paramList = new ArrayList<>();
        if (Map.class.isAssignableFrom(clazz)) {
            Map mapTmp = (Map) param;
            for (ParameterMapping parameterMapping : boundSql.getParameterMappings()) {
                String property = parameterMapping.getProperty();
                paramList.add(mapTmp.get(property));
            }
        } else {
            for (ParameterMapping parameterMapping : boundSql.getParameterMappings()) {
                String property = parameterMapping.getProperty();
                try {
                    Field field = FieldUtils.getDeclaredField(clazz, property);
                    if (field == null) {
                        throw new RuntimeException("can't found property  " + property + " from Class " + clazz);
                    }
                    field.setAccessible(true);
                    paramList.add(field.get(param));
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        }
        Object[] params = paramList.toArray(new Object[0]);
        return new SqlModel(sql, params);
    }

    public static class SqlModel {
        private String sql;
        private Object[] params;

        public SqlModel(String sql, Object[] params) {
            this.sql = sql;
            this.params = params;
        }

        public String getSql() {
            return this.sql;
        }

        public Object[] getParams() {
            return this.params;
        }

        public void setSql(String sql) {
            this.sql = sql;
        }

        public void setParams(Object[] params) {
            this.params = params;
        }

    }
}
