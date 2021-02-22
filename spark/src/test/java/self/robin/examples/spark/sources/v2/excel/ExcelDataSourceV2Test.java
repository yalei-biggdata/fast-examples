package self.robin.examples.spark.sources.v2.excel;


import com.google.gson.Gson;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.junit.Test;
import self.robin.examples.spark.sources.excel.ExcelDataSource;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @Description: ...
 * @Author: Li Yalei - Robin
 * @Date: 2021/2/20 17:45
 */
public class ExcelDataSourceV2Test {

    @Test
    public void testDsV2(){
        String dataSource = ExcelDataSourceV2.class.getName();
        test(dataSource);
    }

    @Test
    public void testDs(){
        String dataSource = ExcelDataSource.class.getName();
        test(dataSource);
    }

    private void test(String dataSource){
        String downloadPath = "file:/C:/Users/liyalei/Downloads/";

        String path = downloadPath + "ds-test.xlsx";

        SparkSession spark = SparkSession.builder().master("local[2]").appName("local test").getOrCreate();

        Map<String, String> schemaMap = new HashMap<>();
        schemaMap.put("a", "String");
        schemaMap.put("b", "String");
        schemaMap.put("c", "String");
        schemaMap.put("d", "String");

        Dataset<Row> rows = spark.read().format(dataSource)
                //可选: 指定 schema 信息
//                .option("schema", new Gson().toJson(schemaMap))
                //必填：是否有表头
                .option("header", true)
                //必填：文件路径，多个路径用逗号分隔
                .load(path);

        rows.show();
    }

}