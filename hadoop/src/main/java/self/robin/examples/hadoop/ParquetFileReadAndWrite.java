package self.robin.examples.hadoop;


import com.alibaba.fastjson.JSON;
import jodd.time.JulianDate;
import lombok.Data;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.Path;
import org.apache.parquet.column.ParquetProperties;
import org.apache.parquet.example.data.Group;
import org.apache.parquet.example.data.simple.SimpleGroup;
import org.apache.parquet.example.data.simple.SimpleGroupFactory;
import org.apache.parquet.hadoop.ParquetFileWriter;
import org.apache.parquet.hadoop.ParquetReader;
import org.apache.parquet.hadoop.ParquetWriter;
import org.apache.parquet.hadoop.example.ExampleParquetWriter;
import org.apache.parquet.hadoop.example.GroupReadSupport;
import org.apache.parquet.hadoop.metadata.CompressionCodecName;
import org.apache.parquet.io.api.Binary;
import org.apache.parquet.schema.MessageType;
import org.apache.parquet.schema.PrimitiveType;
import org.apache.parquet.schema.Types;
import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @Description: ...
 * @Author: Li Yalei - Robin
 * @Date: 2021/5/8 19:44
 */
public class ParquetFileReadAndWrite {

    //表结构
    private static final MessageType FILE_SCHEMA;
    private static final String TABLE_NAME = "parquet_with_time";

    static {
        //定义表结构
        Types.MessageTypeBuilder messageTypeBuilder = Types.buildMessage();
        messageTypeBuilder
                .optional(PrimitiveType.PrimitiveTypeName.BINARY)
                .named("name")
                .optional(PrimitiveType.PrimitiveTypeName.INT32)
                .named("age")
                .optional(PrimitiveType.PrimitiveTypeName.INT96)
                .named("create_time");

        FILE_SCHEMA = messageTypeBuilder.named(TABLE_NAME);
    }

    /**
     * 主函数
     *
     * @param args
     */
    public static void main(String[] args) throws Exception {
        System.out.println("Start");

        //获取文件写入的目录
        UserGroupInformationDemo ugi = new UserGroupInformationDemo();
        ugi.doAs("user", (config, fileSystem) -> {
            try {
                String filePath = "/tmp/robin/parquet_test_0";
                filePath = "/tmp/robin/hive.db/test_parquet/my_parquet_0";

                //重建文件
                fileSystem.delete(new Path(filePath), false);

                //写入数据
                writeToFile(filePath, config);

                //读取数据
                testRead(filePath, config);

            } catch (Exception exception) {
                exception.printStackTrace();
            }
            return null;
        });
        System.out.println("Done.");
    }

    private static void testRead(String path, Configuration configuration) throws Exception {
        Path file = new Path(path);

        try (ParquetReader<Group> reader = ParquetReader
                .builder(new GroupReadSupport(), file)
                .withConf(configuration).build();) {


            SimpleGroup group;
            while ((group = (SimpleGroup) reader.read()) != null) {
                System.out.println("schema:" + group.getType().toString());
                System.out.println("name:" + group.getValueToString(0, 0));
                System.out.println("age:" + group.getValueToString(1, 0));
                System.out.println("time:" + group.getValueToString(2, 0));
            }
        }

    }

    /**
     * 写入数据
     */
    private static void writeToFile(String filePath, Configuration configuration) throws Exception {
        //获取数据数据
        List<TestItem> testItems = getTestData();
        System.out.println(JSON.toJSONString(testItems));

        //查看多条数据是否可以一次性写入一个Parquet文件
        try (ParquetWriter<Group> writer = getWriter(filePath, configuration);) {
            SimpleGroupFactory simpleGroupFactory = new SimpleGroupFactory(FILE_SCHEMA);
            //校验目录
            for (TestItem testItem : testItems) {
                SimpleGroup group = (SimpleGroup)simpleGroupFactory.newGroup();
                group.append("name", testItem.getName());
                group.append("age", testItem.getAge());
                group.append("create_time", Binary.fromConstantByteArray(getTimestamp(testItem.getCreateTime())));
                writer.write(group);
            }
        }
    }

    /**
     * Parquet写入配置
     *
     * @param filePath
     * @return
     * @throws IOException
     */
    private static ParquetWriter<Group> getWriter(String filePath, Configuration configuration) throws Exception {
        Path path = new Path(filePath);

        return ExampleParquetWriter.builder(path)
                .withWriteMode(ParquetFileWriter.Mode.CREATE)
                .withWriterVersion(ParquetProperties.WriterVersion.PARQUET_1_0)
                .withCompressionCodec(CompressionCodecName.SNAPPY)
                .withConf(configuration)
                .withType(FILE_SCHEMA)
                .build();
    }

    /**
     * 测试的表结构数据
     */
    @Data
    private static class TestItem {
        private String name;
        private int age;
        private Date createTime;
    }

    /**
     * 测试数据
     *
     * @return
     */
    private static List<TestItem> getTestData() {
        DateTime now = DateTime.now();
        return IntStream.range(1, 6).mapToObj(i -> {
            TestItem item = new TestItem();
            item.setName("hello_" + i);
            item.setAge(i + 10);
            item.setCreateTime(now.minusHours(i).toDate());
            return item;
        }).collect(Collectors.toList());
    }

    /**
     * 将Date转换成时间戳
     *
     * @param date
     * @return
     */
    private static byte[] getTimestamp(Date date) {

        DateTime dateTime = new DateTime(date, DateTimeZone.UTC);
        byte[] bytes = new byte[12];
        LocalDateTime localDateTime = LocalDateTime.of(dateTime.getYear(), dateTime.getMonthOfYear(), dateTime.getDayOfMonth(),
                dateTime.getHourOfDay(), dateTime.getMinuteOfHour(), dateTime.getSecondOfMinute());
        //转纳秒
        long nanos = dateTime.getHourOfDay() * TimeUnit.HOURS.toNanos(1)
                + dateTime.getMinuteOfHour() * TimeUnit.MINUTES.toNanos(1)
                + dateTime.getSecondOfMinute() * TimeUnit.SECONDS.toNanos(1);
        //转儒略日
        ZonedDateTime localT = ZonedDateTime.of(localDateTime, ZoneId.systemDefault());
        ZonedDateTime utcT = localT.withZoneSameInstant(ZoneId.of("UTC"));
        JulianDate julianDate = JulianDate.of(utcT.toLocalDateTime());

        //写入INT96时间戳
        ByteBuffer.wrap(bytes)
                .order(ByteOrder.LITTLE_ENDIAN)
                //8位
                .putLong(nanos)
                //4位
                .putInt(julianDate.getJulianDayNumber());
        return bytes;
    }

}
