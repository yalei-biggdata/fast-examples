package self.robin.examples.spark.demo;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;

import java.util.Arrays;

/**
 * ...
 *
 * @author Robin-Li
 * @since: 2022-12-30 18:01
 */
public class Demo {

    public static void main(String[] args) {
        SparkSession sparkSession = SparkSession.builder().getOrCreate();
        Dataset<Row> ds = sparkSession.createDataFrame(Arrays.asList(new Bean()), null);

    }

    static class Bean {

        private Long episodeId;

        private Long playlistId;

    }
}
