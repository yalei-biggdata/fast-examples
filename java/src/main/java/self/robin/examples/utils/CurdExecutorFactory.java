package self.robin.examples.utils;

import com.alibaba.druid.pool.DruidDataSourceFactory;
import self.robin.examples.utils.ibatis.CurdExecutor;

import javax.sql.DataSource;
import java.util.Properties;

/**
 * 默认使用 DruidDataSourceFactory 作为数据源
 *
 * @author Li Yalei - Robin
 * @since: 2021/9/15 12:10
 */
public class CurdExecutorFactory {

    /**
     * property文件中默认的 字段名。
     */
    private final String MAPPER_LOCATIONS_KEY = "mapper-locations";

    public static CurdExecutor newDruidSource(String url, String user, String passwd) throws Exception {
        Properties properties = new Properties();
        DataSource dataSource = DruidDataSourceFactory.createDataSource(properties);
        return new CurdExecutor(dataSource);
    }

}
