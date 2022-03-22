/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package self.robin.examples.hadoop;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.fs.Path;
import org.apache.hadoop.hive.ql.exec.FileSinkOperator.RecordWriter;
import org.apache.hadoop.hive.ql.exec.Utilities;
import org.apache.hadoop.hive.ql.io.HiveOutputFormat;
import org.apache.hadoop.hive.ql.io.IOConstants;
import org.apache.hadoop.hive.ql.io.parquet.convert.HiveSchemaConverter;
import org.apache.hadoop.hive.ql.io.parquet.write.DataWritableWriteSupport;
import org.apache.hadoop.hive.serde.serdeConstants;
import org.apache.hadoop.hive.serde2.ColumnSet;
import org.apache.hadoop.hive.serde2.MetadataTypedColumnsetSerDe;
import org.apache.hadoop.hive.serde2.SerDeException;
import org.apache.hadoop.hive.serde2.lazy.LazySerDeParameters;
import org.apache.hadoop.hive.serde2.lazy.LazySimpleSerDe;
import org.apache.hadoop.hive.serde2.typeinfo.TypeInfo;
import org.apache.hadoop.hive.serde2.typeinfo.TypeInfoUtils;
import org.apache.hadoop.io.Writable;
import org.apache.hadoop.io.WritableComparable;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.util.Progressable;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 魔镜输出专用，业务流定制
 *
 * @author
 * @since
 */
public class AdhocExportOutputFormat<K extends WritableComparable, V extends Writable> implements HiveOutputFormat<K, V> {


    private static final Log LOG = LogFactory.getLog(AdhocExportOutputFormat.class);

    /**
     * create the final out file, and output row by row. After one row is
     * appended, a configured row separator is appended
     *
     * @param jc              the job configuration file
     * @param outPath         the final output file to be created
     * @param valueClass      the value class used for create
     * @param isCompressed    whether the content is compressed or not
     * @param tableProperties the tableProperties of this file's corresponding table
     * @param progress        progress used for status report
     * @return the RecordWriter
     */
    @Override
    public RecordWriter getHiveRecordWriter(JobConf jc, Path outPath,
                                            Class<? extends Writable> valueClass, boolean isCompressed,
                                            Properties tableProperties, Progressable progress) throws IOException {

        LOG.info("path:" + outPath.toString());
        int rowSeparator = 0;
        String rowSeparatorString = tableProperties.getProperty(serdeConstants.LINE_DELIM, "\n");
        try {
            rowSeparator = Byte.parseByte(rowSeparatorString);
        } catch (NumberFormatException e) {
            rowSeparator = rowSeparatorString.charAt(0);
        }

        final String columnNameProperty = tableProperties.getProperty(IOConstants.COLUMNS);
        final String columnTypeProperty = tableProperties.getProperty(IOConstants.COLUMNS_TYPES);

        Configuration config = ((Configuration) jc);
        try {
            Field filed = config.getClass().getField("properties");
            Properties obj = (Properties) filed.get(config);
            printProperties("config", obj);
        } catch (Exception e) {
            e.printStackTrace();
        }

        printProperties("tableProperties", tableProperties);


        LOG.info("jc=" + jc.toString());
        LOG.info("aaa=" + jc.get("my.config.aaa"));
        LOG.info("bbb=" + jc.get("my.config.bbb"));
        LOG.info("tableProperties=" + tableProperties.toString());
        LOG.info("progress=" + progress.getClass());
        LOG.info("columnNameProperty=" + columnNameProperty);
        LOG.info("columnTypeProperty=" + columnTypeProperty);
        List<String> columnNames;
        List<TypeInfo> columnTypes;

        if (columnNameProperty.length() == 0) {
            columnNames = new ArrayList<String>();
        } else {
            columnNames = Arrays.asList(columnNameProperty.split(","));
        }

        if (columnTypeProperty.length() == 0) {
            columnTypes = new ArrayList<TypeInfo>();
        } else {
            columnTypes = TypeInfoUtils.getTypeInfosFromTypeString(columnTypeProperty);
        }

        LOG.info("columnNames=" + columnNames.stream().collect(Collectors.joining(",")));
        LOG.info("columnTypes=" + columnTypes.stream().map(type -> type.toString()).collect(Collectors.joining(",")));

        DataWritableWriteSupport.setSchema(HiveSchemaConverter.convert(columnNames, columnTypes), jc);

        LOG.info("columnNames2=" + columnNames.stream().collect(Collectors.joining(",")));
        LOG.info("columnTypes2=" + columnTypes.stream().map(type -> type.toString()).collect(Collectors.joining(",")));

        LOG.info("valueClass=" + valueClass.getName());
        LOG.info("rowSep=" + rowSeparatorString);
        LOG.info("fieldSep=" + tableProperties.getProperty(serdeConstants.FIELD_DELIM, "\001"));

        final WritableSerDe serde = new WritableSerDe(jc, tableProperties);
        LOG.info("toStr=" + serde.getSerDe().toString());

        try {
            LazySimpleSerDe lsd = new LazySimpleSerDe();
            lsd.initialize(jc, tableProperties);
            LazySerDeParameters lsdp = new LazySerDeParameters(jc, tableProperties, LazySimpleSerDe.class.getName());
            LOG.info("columnNames3=" + lsdp.getColumnNames().stream().collect(Collectors.joining(",")));
            LOG.info("columnTypes3=" + lsdp.getColumnTypes().stream().map(type -> type.toString()).collect(Collectors.joining(",")));

        } catch (SerDeException e) {
            e.printStackTrace();
        }

        final int finalRowSeparator = rowSeparator;
        FileSystem fs = outPath.getFileSystem(jc);
        final OutputStream outStream = Utilities.createCompressedStream(jc, fs.create(outPath, progress), isCompressed);
        return new RecordWriter() {

            @Override
            public void write(Writable r) throws IOException {
                ColumnSet cs = serde.deserialize(r);
                LOG.info(r.toString() + "<=>" + cs.col.stream().collect(Collectors.joining(",")));
            }

            @Override
            public void close(boolean abort) throws IOException {
                outStream.close();
            }
        };
    }

    @Override
    public org.apache.hadoop.mapred.RecordWriter<K, V> getRecordWriter(
            FileSystem fileSystem, JobConf jobConf, String s, Progressable progressable) throws IOException {
        throw new RuntimeException("Hello, I[OutputFormat.getRecordWriter] Should never be used");
    }

    @Override
    public void checkOutputSpecs(FileSystem fileSystem, JobConf jobConf) throws IOException {
        // TODO check path & ...
    }

    private void printProperties(String tag, Properties properties) {
        properties.forEach((key, value) -> {
            LOG.info(tag + " :" + key + " -> " + value);
        });
    }

    public class WritableSerDe {

        private MetadataTypedColumnsetSerDe serDe;

        public WritableSerDe(Configuration job, Properties tbl) {
            try {
                serDe = new MetadataTypedColumnsetSerDe();
                serDe.initialize(job, tbl);
            } catch (SerDeException e) {
                throw new RuntimeException(e);
            }
            Objects.requireNonNull(serDe, "MetadataTypedColumnsetSerDe required");
        }

        public ColumnSet deserialize(Writable row) {
            try {
                return (ColumnSet) serDe.deserialize(row);
            } catch (SerDeException e) {
                throw new RuntimeException(e);
            }
        }

        public MetadataTypedColumnsetSerDe getSerDe() {
            return serDe;
        }
    }

    public static void main(String[] args) {
        System.out.println();
    }
}
