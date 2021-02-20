package self.robin.examples.spark.sources.v2.excel;

import com.google.gson.Gson;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.util.StringUtils;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.spark.sql.Row;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.catalyst.parser.CatalystSqlParser;
import org.apache.spark.sql.internal.SQLConf;
import org.apache.spark.sql.sources.v2.DataSourceOptions;
import org.apache.spark.sql.sources.v2.DataSourceV2;
import org.apache.spark.sql.sources.v2.ReadSupport;
import org.apache.spark.sql.sources.v2.reader.DataReader;
import org.apache.spark.sql.sources.v2.reader.DataReaderFactory;
import org.apache.spark.sql.sources.v2.reader.DataSourceReader;
import org.apache.spark.sql.types.Metadata;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.apache.spark.util.SerializableConfiguration;

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

import static self.robin.examples.spark.sources.v2.excel.SparkWorkbookHelper.*;

/**
 * @Description: ...
 * @Author: Li Yalei - Robin
 * @Date: 2021/2/8 10:20
 */
public class ExcelDataSourceV2 implements DataSourceV2, ReadSupport, Serializable {

    @Override
    public DataSourceReader createReader(DataSourceOptions options) {
        return new ExcelDataSourceV2Reader(SerializableOptions.of(options));
    }

    class ExcelDataSourceV2Reader implements DataSourceReader, Serializable {

        private SerializableOptions options;

        private volatile StructType schema;

        private Collection<String> paths;

        ExcelDataSourceV2Reader(SerializableOptions options) {
            this.options = options;
            init();
        }

        private void init(){
            Optional<String> pathOpt = options.get("path");
            if (!pathOpt.isPresent()) {
                throw new RuntimeException("path 不能为空");
            }
            paths = StringUtils.getStringCollection(pathOpt.get(), ",");
        }

        /**
         * 解析传进来的schema信息
         */
        public void buildStructType(Map<String, String> map) {
            List<StructField> fieldList = new ArrayList<>();
            for (Map.Entry<String, String> entry : map.entrySet()) {
                StructField structField = new StructField(entry.getKey(),
                        new CatalystSqlParser(new SQLConf()).parseDataType(entry.getValue()),
                        true, Metadata.empty());

                fieldList.add(structField);
            }
            this.schema = new StructType(fieldList.toArray(new StructField[0]));
        }

        @Override
        public StructType readSchema() {

            if (this.schema != null && !this.schema.isEmpty()) {
                return this.schema;
            }
            Optional<String> schemaOpt = options.get("schema");

            if(schemaOpt.isPresent()){
                Map<String, String> map = new Gson().fromJson(schemaOpt.get(), LinkedHashMap.class);
                buildStructType(map);
            }else {
                tryParseColsFromFiles();
            }
            return this.schema;
        }

        private void tryParseColsFromFiles(){
            boolean header = options.getBoolean("header", false);
            int startRow = options.getInt("startRow", 1);
            int startCol = options.getInt("startCol", 1);
            //尝试从excel解析
            //默认取第一个表单
            //要求所有excel表单中的列必须一样多
            List<String> colNames = new ArrayList<>();
            int size = paths.stream().map(path -> {
                try {
                    Workbook wb = createWorkbook(path, getConfiguration());
                    //默认取第一个表单
                    List<String> cols = getColumnNames(wb.getSheetAt(0), startRow, startCol, header);
                    //保存首个解析出的列名
                    if(colNames.isEmpty()){
                        colNames.addAll(cols);
                    }
                    //要求所有excel表单中的列必须一样多
                    return cols.size();
                } catch (IOException e) {
                    e.printStackTrace();
                    return -1;
                }
            }).collect(Collectors.toSet()).size();

            if(size!=1){
                //说明所有excel文件的列不一致
                throw new RuntimeException("提供的excel文件中表单的列不一致，请检查");
            }

            Map<String, String> map = new LinkedHashMap<>();
            for (String col : colNames) {
                map.put(col, "String");
            }
            buildStructType(map);
        }


        @Override
        public List<DataReaderFactory<Row>> createDataReaderFactories() {

            SerializableConfiguration serConfig = new SerializableConfiguration(getConfiguration());
            boolean header = options.getBoolean("header", false);
            int startRow = options.getInt("startRow", 1);
            int startCol = options.getInt("startCol", 1);

            return paths.parallelStream().map(path -> new DataReaderFactory<Row>() {

                @Override
                public DataReader<Row> createDataReader() {
                    return new WorkbookReader(startRow, startCol, header, path, serConfig);
                }
            }).collect(Collectors.toList());
        }

        /**
         * 获取提交的配置信息
         * @return
         */
        private Configuration getConfiguration(){
            SparkSession spark = SparkSession.getActiveSession().get();
            Configuration config = spark.sparkContext().hadoopConfiguration();
            config.set("develop.master", spark.sparkContext().master());
            return config;
        }
    }

    class WorkbookReader implements DataReader<Row>, Serializable {

        /**
         * 是否第一行是表格头
         */
        private boolean header;
        /**
         * 文件路径
         */
        private String path;

        /**
         * excel
         */
        private Workbook workbook;

        private SheetIterator sheetIterator;

        /**
         * excel文件的path信息，以及表单中数据的位置信息
         * @param startRow 数据在表单的起始行
         * @param startCol 数据在表单的起始列
         * @param header 首行是否是表头（首行的定义是=>startRow所在行）
         * @param path 文件路径
         * @param configuration hadoop
         */
        public WorkbookReader(int startRow, int startCol, boolean header, String path,
                              SerializableConfiguration configuration) {
            this.header = header;
            this.path = path;
            if (path == null || path.equals("")) {
                throw new RuntimeException("path is null");
            }
            init(configuration.value());
        }

        /**
         * 因为此处代码不在driver端运行，所以不能 SparkSession.getActiveSession()
         */
        private void init(Configuration conf) {
            try {
                this.workbook = createWorkbook(path, conf);
                this.sheetIterator = new SheetIterator(0, this.workbook.iterator());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        @Override
        public boolean next() throws IOException {
            return sheetIterator.hasNext();
        }

        @Override
        public Row get() {
            return poiRow2SparkRow(this.sheetIterator.next());
        }

        @Override
        public void close() throws IOException {
            if (this.workbook != null) {
                this.workbook.close();
            }
        }
    }

}
