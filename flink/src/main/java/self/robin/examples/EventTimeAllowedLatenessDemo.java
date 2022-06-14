package self.robin.examples;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.eventtime.*;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.streaming.api.functions.windowing.ProcessWindowFunction;
import org.apache.flink.streaming.api.windowing.assigners.TumblingEventTimeWindows;
import org.apache.flink.streaming.api.windowing.time.Time;
import org.apache.flink.streaming.api.windowing.windows.TimeWindow;
import org.apache.flink.util.Collector;

import java.time.Duration;
import java.util.Iterator;

/**
 * 1. 使用 eventTime
 * 2. 水印 5s
 * 3. 验证计算的触发时机
 * <p>
 * 1. 时间窗口，是左闭右开区间，例如5窗口：[0,5)
 *
 * @author Robin-Li
 * @since: 2022-05-25 16:41
 */
@Slf4j
public class EventTimeAllowedLatenessDemo {

    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        //env.getConfig().setAutoWatermarkInterval();
        //env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);

        env.addSource(new SelfSource())
                .assignTimestampsAndWatermarks(
                        WatermarkStrategy.<DataWithTime>forGenerator(context -> new WatermarkGenerator<DataWithTime>() {
                            private long maxTimestamp = 0;
                            @Override
                            public void onEvent(DataWithTime event, long eventTimestamp, WatermarkOutput output) {
                                this.maxTimestamp = Math.max(maxTimestamp, eventTimestamp);
                                output.emitWatermark(new Watermark(this.maxTimestamp));
                            }

                            @Override
                            public void onPeriodicEmit(WatermarkOutput output) {
                                output.emitWatermark(new Watermark(this.maxTimestamp));
                            }
                        })
                                /*WatermarkStrategy.<DataWithTime>forBoundedOutOfOrderness(Duration.ofSeconds(2))*/
                                .withTimestampAssigner(
                                        (event, timestamp) -> event.getTimestamp()))
                .keyBy(new KeySelector<DataWithTime, String>() {
                    @Override
                    public String getKey(DataWithTime tuple3) throws Exception {
                        return tuple3.key;
                    }
                })
                .window(TumblingEventTimeWindows.of(Time.seconds(5)))
                //.allowedLateness(Time.seconds(6))
                .process(new ProcessWindowFunction<DataWithTime, DataWithTime, String, TimeWindow>() {
                    @Override
                    public void process(String key,
                                        Context context,
                                        Iterable<DataWithTime> elements,
                                        Collector<DataWithTime> out)
                            throws Exception {
                        Iterator<DataWithTime> ite = elements.iterator();
                        while (ite.hasNext()) {
                            DataWithTime dwt = ite.next();
                            System.out.println(", key = " + key + ", e_time = " + dwt.getTimestamp());
                        }
                    }
                })
                .print();

        env.execute("SavePointTest");
    }

    public static class SelfSource implements SourceFunction<DataWithTime> {

        @Override
        public void run(SourceContext<DataWithTime> sourceContext) throws Exception {
            sourceContext.collect(new DataWithTime("key_0", 1, 0L));
            sourceContext.collect(new DataWithTime("key_0", 2, 1000L));
            sourceContext.collect(new DataWithTime("key_0", 3, 2000L));
            sourceContext.collect(new DataWithTime("key_0", 3, 3000L));
            sourceContext.collect(new DataWithTime("key_0", 5, 5000L));
            sourceContext.collect(new DataWithTime("key_0", 7, 7000L));
            sourceContext.collect(new DataWithTime("key_0", 4, 4000L));
            sourceContext.collect(new DataWithTime("key_0", 6, 6000L));
            sourceContext.collect(new DataWithTime("key_0", 8, 8000L));
            sourceContext.collect(new DataWithTime("key_0", 9, 9000L));


            sourceContext.collect(new DataWithTime("key_1", 1, 0L));
            sourceContext.collect(new DataWithTime("key_1", 2, 1000L));
            sourceContext.collect(new DataWithTime("key_1", 3, 2000L));
            sourceContext.collect(new DataWithTime("key_1", 3, 3000L));
            sourceContext.collect(new DataWithTime("key_1", 5, 5000L));
            sourceContext.collect(new DataWithTime("key_1", 7, 7000L));
            sourceContext.collect(new DataWithTime("key_1", 4, 4000L));
            sourceContext.collect(new DataWithTime("key_1", 6, 6000L));
            sourceContext.collect(new DataWithTime("key_1", 8, 8000L));
            sourceContext.collect(new DataWithTime("key_1", 9, 9000L));
        }

        @Override
        public void cancel() {
        }
    }

    @Data
    @AllArgsConstructor
    public static class DataWithTime {

        private String key;

        private Integer num;

        private Long timestamp;
    }
}
