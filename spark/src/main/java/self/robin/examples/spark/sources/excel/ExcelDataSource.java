package self.robin.examples.spark.sources.excel;

import lombok.val;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileStatus;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.mapreduce.Job;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.apache.spark.api.java.JavaSparkContext;
import org.apache.spark.broadcast.Broadcast;
import org.apache.spark.sql.SparkSession;
import org.apache.spark.sql.catalyst.InternalRow;
import org.apache.spark.sql.execution.datasources.*;
import org.apache.spark.sql.internal.SQLConf;
import org.apache.spark.sql.sources.Filter;
import org.apache.spark.sql.types.DataTypes;
import org.apache.spark.sql.types.StructField;
import org.apache.spark.sql.types.StructType;
import org.apache.spark.util.SerializableConfiguration;
import scala.Function1;
import scala.Option;
import scala.collection.Iterator;
import scala.collection.JavaConversions;
import scala.collection.Seq;
import scala.collection.immutable.Map;
import scala.collection.mutable.ListBuffer;
import scala.runtime.AbstractFunction1;

import java.io.InputStream;
import java.io.Serializable;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 * @Description: ...
 * @Author: Li Yalei - Robin
 * @Date: 2020/12/22 21:14
 */
public class ExcelDataSource implements FileFormat, Serializable {


    @Override
    public Option<StructType> inferSchema(SparkSession sparkSession, Map<String, String> options, Seq<FileStatus> files) {
        XlsxOptions xlsxOptions = new XlsxOptions(options);

        java.util.Map<FileStatus, Workbook> fileWb = new java.util.HashMap<>();

        for (FileStatus fileStatus : (List<FileStatus>)JavaConversions.seqAsJavaList(files)) {
            Workbook wb = parseWorkbook(sparkSession.sparkContext().hadoopConfiguration(), fileStatus.getPath().toString());
            fileWb.put(fileStatus, wb);
        }

        if (fileWb.isEmpty()) {
            throw new RuntimeException("At least one input file required");
        }

        return (Option<StructType>)Option.apply(inferHeaders(sparkSession, xlsxOptions, fileWb));
    }

    /**
     * parse headers from file
     *
     * @param sparkSession
     * @param options
     * @param workbooks
     * @return
     */
    public StructType inferHeaders(SparkSession sparkSession, XlsxOptions options, java.util.Map<FileStatus, Workbook> workbooks) {
        //Iterator<InternalRow> vals = readFile(options, sparkSession.sparkContext().hadoopConfiguration(), workbooks.values().iterator().next(), null);
        
        return new StructType(new StructField[]{
                new StructField("a", DataTypes.StringType, true, null),
                new StructField("b", DataTypes.StringType, true, null),
                new StructField("c", DataTypes.StringType, true, null),
                new StructField("d", DataTypes.StringType, true, null),
                new StructField("e", DataTypes.StringType, true, null)
        });
        //return new StructType();
    }

    @Override
    public boolean supportBatch(SparkSession sparkSession, StructType dataSchema) {
        return false;
    }

    @Override
    public OutputWriterFactory prepareWrite(SparkSession sparkSession, Job job, Map<String, String> options, StructType dataSchema) {
        throw new RuntimeException("unImplement OutputWriterFactory");
    }

    @Override
    public Option<Seq<String>> vectorTypes(StructType requiredSchema, StructType partitionSchema, SQLConf sqlConf) {
        throw new RuntimeException("unImplement vectorTypes");
    }

    @Override
    public Function1<PartitionedFile, Iterator<InternalRow>> buildReaderWithPartitionValues(SparkSession sparkSession, StructType dataSchema,
                                                                                            StructType partitionSchema,
                                                                                            StructType requiredSchema, Seq<Filter> filters,
                                                                                            Map<String, String> options,
                                                                                            Configuration hadoopConf) {


        return FileFormat$class.buildReaderWithPartitionValues(this, sparkSession, dataSchema, partitionSchema, requiredSchema, filters, options, hadoopConf);

        //return buildReader(sparkSession, dataSchema, partitionSchema, requiredSchema, filters, options, hadoopConf);
    }

    @Override
    public boolean isSplitable(SparkSession sparkSession, Map<String, String> options, Path path) {
        return false;
    }

    @Override
    public Function1<PartitionedFile, Iterator<InternalRow>> buildReader(SparkSession sparkSession,
                                                                         StructType dataSchema,
                                                                         StructType partitionSchema,
                                                                         StructType requiredSchema,
                                                                         Seq<Filter> filters,
                                                                         Map<String, String> options,
                                                                         Configuration hadoopConf) {
        //TODO verify schema
        val xlsxOptions = new XlsxOptions(options);

        Broadcast<SerializableConfiguration> broadcastedHadoopConf = JavaSparkContext.fromSparkContext(sparkSession.sparkContext())
                .broadcast(new SerializableConfiguration(hadoopConf));

        return new InternalFunction1(requiredSchema, broadcastedHadoopConf, xlsxOptions);
    }

    /**
     * 此内部类只为了能够序列化用
     */
    class InternalFunction1 extends AbstractFunction1<PartitionedFile, Iterator<InternalRow>>
            implements Serializable {

        private StructType requiredSchema;
        private Broadcast<SerializableConfiguration> hadoopConf;
        private XlsxOptions xlsxOptions;

        public InternalFunction1(StructType requiredSchema, Broadcast<SerializableConfiguration> hadoopConf, XlsxOptions xlsxOptions) {
            this.requiredSchema = requiredSchema;
            this.hadoopConf = hadoopConf;
            this.xlsxOptions = xlsxOptions;
        }

        @Override
        public Iterator<InternalRow> apply(PartitionedFile file) {
            Configuration config = hadoopConf.getValue().value();
            Workbook wb = parseWorkbook(config, file.filePath());
            return readFile(xlsxOptions, config, wb, requiredSchema);
        }
    }

    private Workbook parseWorkbook(Configuration hadoopConf, String filePath) {
        try {
            InputStream inputStream = CodecStreams.createInputStreamWithCloseResource(hadoopConf, new Path(new URI(filePath)));

            String extString = filePath.substring(filePath.lastIndexOf("."));

            Workbook workbook;
            if (".xls".equalsIgnoreCase(extString)) {
                workbook = new HSSFWorkbook(inputStream);
            } else if (".xlsx".equalsIgnoreCase(extString)) {
                workbook = new XSSFWorkbook(inputStream);
            } else {
                throw new RuntimeException("File format is not supported");
            }
            return workbook;
            // CSVDataSource(parsedOptions).readFile(conf, file, parser, requiredSchema)
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * read file
     *
     * @param requiredSchema
     * @param hadoopConf
     * @return
     */
    public Iterator<InternalRow> readFile(XlsxOptions options, Configuration hadoopConf, Workbook workbook, StructType requiredSchema) {

        ListBuffer<InternalRow> rowListBuffer = new ListBuffer();

        int sheetNbr = workbook.getNumberOfSheets();
        for (int i = 0; i < sheetNbr; i++) {
            Sheet sheet = workbook.getSheetAt(i);
            java.util.Iterator<Row> rowIte = sheet.rowIterator();

            Row row;
            while (rowIte.hasNext()) {
                row = rowIte.next();
                java.util.Iterator<Cell> cellIte = row.cellIterator();

                List cellBuffer = new ArrayList();
                Cell cell;
                while (cellIte.hasNext()) {
                    cell = cellIte.next();
                    switch (cell.getCellTypeEnum()) {
                        case NUMERIC:
                            cellBuffer.add(cell.getNumericCellValue());
                            break;
                        case BOOLEAN:
                            cellBuffer.add(cell.getBooleanCellValue());
                            break;
                        case STRING:
                            cellBuffer.add(cell.getStringCellValue());
                            break;
                        case BLANK:
                            cellBuffer.add(null);
                        default:
                            throw new RuntimeException("unSupport cell type");
                    }
                }
                InternalRow internalRow = InternalRow.fromSeq(JavaConversions.asScalaBuffer(cellBuffer).toSeq());
                rowListBuffer.$plus$eq(internalRow);
            }
        }
        return rowListBuffer.iterator();
    }


}
