package self.robin.examples.spark.sources.excel;

import org.apache.spark.rdd.RDD;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.sources.BaseRelation;
import org.apache.spark.sql.sources.PrunedScan;
import org.apache.spark.sql.sources.TableScan;
import org.apache.spark.sql.types.StructType;
import scala.collection.JavaConversions;

import java.util.Arrays;
import java.util.List;

/**
 * @Description: ...
 * @Author: Li Yalei - Robin
 * @Date: 2020/12/25 12:49
 */
public class ExcelRelation extends BaseRelation implements TableScan, PrunedScan {

    private SQLContext sqlContext;

    private StructType userSchema;

    public ExcelRelation(SQLContext sqlContext, StructType userSchema) {
        this.sqlContext = sqlContext;
        this.userSchema = userSchema;
    }

    @Override
    public SQLContext sqlContext() {
        return this.sqlContext;
    }

    @Override
    public StructType schema() {
        return inferSchema();
    }

    @Override
    public RDD<Row> buildScan(String[] requiredColumns) {


        return null;
    }

    @Override
    public RDD<Row> buildScan() {
        return buildScan(inferSchema().fieldNames());
    }

    private StructType inferSchema(){
        if(!this.userSchema.isEmpty()){
            return this.userSchema;
        }

        // workbook 取第一行
        return new StructType();
    }
}
