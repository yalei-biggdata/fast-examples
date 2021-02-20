package self.robin.examples.spark.sources.v2.excel;


import com.google.gson.Gson;
import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * @Description: ...
 * @Author: Li Yalei - Robin
 * @Date: 2021/2/20 17:45
 */
public class ExcelDataSourceV2Test {

    @Test
    public void testDs2(){
        Map<String, String> env = System.getenv();
        String homePath = System.getenv("HOMEPATH");
        String downloadPath = "C:/Users/liyalei/Downloads/";

        String dataSource = ExcelDataSourceV2.class.getName();
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
                .option("headers", true)
                //可选：startRow, default:1
                .option("startRow", 1)
                //可选：startRow, default:1
                .option("startCol", 3)
                //必填：文件路径，多个路径用逗号分隔
                .load(path);

        rows.show();
    }

}