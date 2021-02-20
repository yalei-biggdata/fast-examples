package self.robin.examples.spark.sources.excel;

import org.apache.spark.sql.Dataset;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SQLContext;
import org.apache.spark.sql.SaveMode;
import org.apache.spark.sql.sources.BaseRelation;
import org.apache.spark.sql.sources.CreatableRelationProvider;
import org.apache.spark.sql.sources.RelationProvider;
import org.apache.spark.sql.sources.SchemaRelationProvider;
import org.apache.spark.sql.types.StructType;
import scala.collection.immutable.Map;

/**
 * @Description: ...
 * @Author: Li Yalei - Robin
 * @Date: 2020/12/24 19:22
 */
public class ExcelRelationProvider implements RelationProvider, SchemaRelationProvider {


    @Override
    public BaseRelation createRelation(SQLContext sqlContext, Map<String, String> parameters) {
        return createRelation(sqlContext, parameters, null);
    }

    @Override
    public BaseRelation createRelation(SQLContext sqlContext, Map<String, String> parameters, StructType schema) {


        return null;
    }

}
