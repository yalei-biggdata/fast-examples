package self.robin.examples.hadoop.utils;

import jodd.time.JulianDate;
import lombok.Data;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Validate;
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
import org.apache.parquet.schema.GroupType;
import org.apache.parquet.schema.MessageType;
import org.apache.parquet.schema.PrimitiveType;
import org.apache.parquet.schema.Types;
import org.joda.time.DateTime;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * @Description: ...
 * @Author: Li Yalei - Robin
 * @Date: 2021/5/10 19:06
 */
public class ParquetHelper {

    /**
     * 文件对象
     */
    public static class ParquetFile {

        /**
         * 表结构
         */
        @Getter
        private MessageType fileSchema;

        /**
         * 字段下滑线转驼峰
         */
        private boolean mapUnderscoreToCamelCase = false;

        private Configuration configuration;

        private String filePath;

        public ParquetFile(Configuration configuration, String filePath) {
            this.configuration = Validate.notNull(configuration);
            this.filePath = Validate.notBlank(filePath);
        }

        public <T> void create(List<T> dataList) throws Exception {
            Validate.notEmpty(dataList);

            Class<?> clazz = dataList.get(0).getClass();
            Field[] fields = clazz.getDeclaredFields();

            List<Field> fieldList = Arrays.stream(fields).filter(field -> existGetMethod(field, clazz)).collect(Collectors.toList());
            List<String> fieldNames = fieldList.stream().map(Field::getName).collect(Collectors.toList());
            validColumnNames(fieldNames);
            initSchema(fieldNames);

            try (ParquetWriter<Group> writer = getWriter()) {
                SimpleGroupFactory simpleGroupFactory = new SimpleGroupFactory(fileSchema);
                for (T t : dataList) {
                    //校验目录
                    SimpleGroup group = (SimpleGroup) simpleGroupFactory.newGroup();
                    for (Field field : fieldList) {
                        field.setAccessible(true);
                        Object value = field.get(t);
                        //TODO 类型未分开处理
                        group.append(mapUnderscoreToCamelCase(field.getName()), String.valueOf(value));
                    }
                    writer.write(group);
                }
            }
        }

        public String getFilename() {
            if (StringUtils.isBlank(this.filePath)) {
                return null;
            }
            if (filePath.lastIndexOf(File.separator) != -1) {
                return filePath.substring(filePath.lastIndexOf(File.separator));
            }
            if (filePath.lastIndexOf("/") != -1) {
                return filePath.substring(filePath.lastIndexOf("/"));
            }
            return null;
        }

        public void mapUnderscoreToCamelCase(boolean onOff) {
            this.mapUnderscoreToCamelCase = onOff;
        }

        private void validColumnNames(List<String> columns) {
            for (String column : columns) {
                Validate.isTrue(StringUtils.isNotBlank(column) &&
                        column.matches("^[0-9a-zA-Z_]{1,}$"), "列名无效=" + column);
            }
        }

        private String mapUnderscoreToCamelCase(String column) {
            if (mapUnderscoreToCamelCase) {
                return Stream.of(StringUtils.splitByCharacterTypeCamelCase(column.trim())).filter(str -> !"_".equals(str))
                        .map(String::toLowerCase).collect(Collectors.joining("_"));
            }
            return column.trim();
        }

        private boolean existGetMethod(Field field, Class<?> clazz) {
            String methodName = "get" + (field.getName().charAt(0) + "").toUpperCase() + field.getName().substring(1);
            try {
                if (clazz.getDeclaredMethod(methodName) != null) {
                    return true;
                }
                return false;
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
                return false;
            }
        }

        /**
         * @param colNameList 列名
         * @param dataList
         */
        public <T> void create(List<String> colNameList, List<List<T>> dataList) throws IOException {
            Validate.notEmpty(colNameList);
            Validate.notEmpty(dataList);
            Validate.isTrue(colNameList.size() == dataList.size());

            validColumnNames(colNameList);
            initSchema(colNameList);

            try (ParquetWriter<Group> writer = getWriter();) {
                SimpleGroupFactory simpleGroupFactory = new SimpleGroupFactory(fileSchema);
                //校验目录
                for (List<T> one : dataList) {
                    Validate.isTrue(colNameList.size() == one.size());
                    SimpleGroup group = (SimpleGroup) simpleGroupFactory.newGroup();
                    for (int j = 0; j < one.size(); j++) {
                        group.append(mapUnderscoreToCamelCase(colNameList.get(j)), String.valueOf(one.get(j)));
                    }
                    writer.write(group);
                }
            }
        }

        private void initSchema(List<String> colNameList) {
            Types.MessageTypeBuilder messageTypeBuilder = Types.buildMessage();
            Types.GroupBuilder<MessageType> groupBuilder = messageTypeBuilder
                    .optional(PrimitiveType.PrimitiveTypeName.BINARY)
                    .named(mapUnderscoreToCamelCase(colNameList.get(0).trim()));

            for (int i = 1; i < colNameList.size(); i++) {
                groupBuilder = groupBuilder.optional(PrimitiveType.PrimitiveTypeName.BINARY)
                        .named(mapUnderscoreToCamelCase(colNameList.get(i)));
            }
            fileSchema = messageTypeBuilder.named(getFilename());
        }

        private ParquetWriter<Group> getWriter()
                throws IOException {
            String fileLocation = getFileLocation();
            return ExampleParquetWriter.builder(new Path(fileLocation))
                    .withWriteMode(ParquetFileWriter.Mode.CREATE)
                    .withWriterVersion(ParquetProperties.WriterVersion.PARQUET_1_0)
                    .withCompressionCodec(CompressionCodecName.SNAPPY)
                    .withConf(configuration)
                    .withType(fileSchema)
                    .build();
        }

        /**
         * 获取文件位置
         *
         * @return
         */
        private String getFileLocation() {
            return this.filePath;
        }

        public static ParquetFileBuilder newBuilder() {
            return new ParquetFileBuilder();
        }

        /**
         * 读数据
         *
         * @return
         * @throws IOException
         */
        public List<Map<String, String>> read() throws IOException {
            Path file = new Path(filePath);
            try (ParquetReader<Group> reader = ParquetReader
                    .builder(new GroupReadSupport(), file)
                    .withConf(configuration).build();) {

                List<Map<String, String>> retData = new ArrayList<>();
                SimpleGroup group;
                while ((group = (SimpleGroup) reader.read()) != null) {
                    GroupType type = group.getType();
                    Map<String, String> one = new HashMap<>();
                    for (int i = 0; i < type.getFieldCount(); i++) {
                        one.put(type.getFieldName(i), group.getValueToString(i, 0));
                    }
                    retData.add(one);
                }
                return retData;
            }
        }

        public <E> List<E> readAs(Class<E> clazz) throws IOException {
            throw new RuntimeException();
        }

        /**
         * 时间戳类型的处理
         *
         * @param date
         * @return
         */
        private static byte[] getTimestamp2(Date date) {
            LocalDateTime now = LocalDateTime.ofInstant(date.toInstant(), ZoneId.of("UTC"));
            //转纳秒
            long nanos = now.getHour() * TimeUnit.HOURS.toNanos(1)
                    + now.getMinute() * TimeUnit.MINUTES.toNanos(1)
                    + now.getSecond() * TimeUnit.SECONDS.toNanos(1);
            //转儒略日
            JulianDate julianDate = JulianDate.of(now);

            //写入INT96时间戳
            byte[] bytes = new byte[12];
            ByteBuffer.wrap(bytes)
                    .order(ByteOrder.LITTLE_ENDIAN)
                    .putLong(nanos)//8位
                    .putInt(julianDate.getJulianDayNumber());//4位
            return bytes;
        }
    }

    public static class ParquetFileBuilder {

        private Configuration configuration;

        private String filePath;

        public ParquetFileBuilder filePath(String filePath) {
            this.filePath = filePath;
            return this;
        }

        public ParquetFileBuilder config(Configuration config) {
            this.configuration = config;
            return this;
        }

        public ParquetFile build() {
            return new ParquetFile(configuration, filePath);
        }
    }

    /**
     * 支持的类型
     */
    public enum DataType {
        INTEGER, LONG, STRING, BOOL, DATE, BYTES, DOUBLE, FLOAT
    }

    @Data
    private static class TestItem {
        private String name;
        private int age;
        private Date createTime;
    }

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

}
