import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.FileUtil;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hive.ql.exec.FileSinkOperator;
import org.apache.hadoop.hive.serde.serdeConstants;
import org.apache.hadoop.hive.serde2.SerDeException;
import org.apache.hadoop.hive.serde2.SerDeUtils;
import org.apache.hadoop.hive.serde2.lazy.LazySimpleSerDe;
import org.apache.hadoop.hive.serde2.objectinspector.StructObjectInspector;
import org.apache.hadoop.io.Text;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.util.Progressable;
import org.junit.Assert;
import org.junit.Test;
import self.robin.examples.hadoop.AdhocExportOutputFormat;

import java.io.File;
import java.io.IOException;
import java.util.Properties;

import static org.mockito.Mockito.mock;

/**
 * @author Li Yalei - Robin
 * @since: 2021/8/20 15:56
 */
public class TstOutputFormat {

    private static File workDir;
    private static JobConf jc = null;
    private static FileSystem fs = null;
//    private static MiniMRCluster mrCluster = null;

    private static void createWorkDir() throws IOException {
        String testDir = System.getProperty("test.tmp.dir", "./");
        testDir = testDir + "/test_output_format/";
        workDir = new File(new File(testDir).getCanonicalPath());
        FileUtil.fullyDelete(workDir);
        workDir.mkdirs();
    }


    @Test
    public void testLazySimpleSerDe() throws Throwable {
        try {
            // Create the SerDe
            System.out.println("test: testLazySimpleSerDe");
            LazySimpleSerDe serDe = new LazySimpleSerDe();
            Configuration conf = new Configuration();
            Properties tbl = createProperties();
            SerDeUtils.initializeSerDe(serDe, conf, tbl, null);

            // Data
            Text t = new Text("123\t456\t789\t1000\t5.3\thive and hadoop\t1.\tNULL");
            // Test
            deserializeAndSerializeLazySimple(serDe, t);
            System.out.println("test: testLazySimpleSerDe - OK");
        } catch (Throwable e) {
            e.printStackTrace();
            throw e;
        }
    }


    private void deserializeAndSerializeLazySimple(LazySimpleSerDe serDe, Text t)
            throws SerDeException {

        // Get the row structure
        StructObjectInspector oi = (StructObjectInspector) serDe
                .getObjectInspector();

        // Deserialize
        Object row = serDe.deserialize(t);
        Assert.assertEquals("serialized size correct after deserialization", serDe.getSerDeStats()
                .getRawDataSize(), t.getLength());
        // Serialize
        Text serializedText = (Text) serDe.serialize(row, oi);
        Assert.assertEquals("serialized size correct after serialization", serDe.getSerDeStats()
                        .getRawDataSize(),
                serializedText.toString().length());
    }


    private Properties createProperties() {
        Properties tbl = new Properties();
        // Set the configuration parameters
        tbl.setProperty(serdeConstants.SERIALIZATION_FORMAT, "9");
        tbl.setProperty("columns", "abyte,ashort,aint,along,adouble,astring,anullint,anullstring");
        tbl.setProperty("columns.types", "tinyint:smallint:int:bigint:double:string:int:string");
        tbl.setProperty(serdeConstants.SERIALIZATION_NULL_FORMAT, "NULL");
        return tbl;
    }

    @Test
    public void testOut() throws Exception {
        createWorkDir();
        Configuration conf = new Configuration(true);
        conf.set("yarn.scheduler.capacity.root.queues", "default");
        conf.set("yarn.scheduler.capacity.root.default.capacity", "100");

        fs = FileSystem.get(conf);
        System.setProperty("hadoop.log.dir", new File(workDir, "/logs").getAbsolutePath());
        // LocalJobRunner does not work with mapreduce OutputCommitter. So need
        // to use MiniMRCluster. MAPREDUCE-2350
        jc = new JobConf(conf);

        Properties tableProps = new Properties();
        tableProps.setProperty("columns", "foo,bar");
        tableProps.setProperty("columns.types", "int:int");

        final Progressable mockProgress = mock(Progressable.class);

        String workPath = workDir.getAbsolutePath();
        System.out.println("workDir=" + workPath);

        AdhocExportOutputFormat<WritableComparable, Writable> outputFormat = new AdhocExportOutputFormat<>();
        FileSinkOperator.RecordWriter writer = outputFormat.getHiveRecordWriter(jc, new Path(workPath + "/foo.txt"), null, false, tableProps, mockProgress);


    }
}
