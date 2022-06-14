package self.robin.examples;

import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.common.restartstrategy.RestartStrategies;
import org.apache.flink.api.common.time.Time;
import org.apache.flink.api.java.functions.KeySelector;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.runtime.state.filesystem.FsStateBackend;
import org.apache.flink.streaming.api.environment.CheckpointConfig;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.ProcessFunction;
import org.apache.flink.streaming.api.functions.source.SourceFunction;
import org.apache.flink.util.Collector;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * ...
 *
 * @author Robin-Li
 * @since: 2022-04-15 10:56
 */
@Slf4j
public class RichFunctionException {

    /**
     * 不仅限于，含有process/window 的算子，有 open 操作的算子可能都具有此问题
     *
     * @param args
     * @throws Exception
     */
    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        env.setRestartStrategy(RestartStrategies.fixedDelayRestart(3, Time.seconds(2)));
//        // 启用
//        env.enableCheckpointing(20);
//        // save to fs
//        env.setStateBackend(new FsStateBackend("file:///E:/tmp/chkdir"));
//        //
//        env.getCheckpointConfig().enableExternalizedCheckpoints(CheckpointConfig.ExternalizedCheckpointCleanup.RETAIN_ON_CANCELLATION);

        env
                .addSource(new SourceFunction<Tuple3<String, Integer, Long>>() {
                    @Override
                    public void run(SourceContext<Tuple3<String, Integer, Long>> sourceContext) throws Exception {
                        int count = 1;
                        while (true) {
                            sourceContext.collect(new Tuple3<>("key", count++, System.currentTimeMillis()));
                            Thread.sleep(200);
                        }
                    }

                    @Override
                    public void cancel() {
                    }
                })
                .map(one -> Tuple3.of(one.f0, one.f1, one.f2))
//                .process(new ProcessFunction<Tuple3<String, Integer, Long>, String>() {
//
//                    private List<Tuple3> list = new ArrayList<>();
//
//                    @Override
//                    public void open(Configuration parameters) throws Exception {
//                        log.error("open: " + LocalDateTime.now());
//                    }
//
//                    @Override
//                    public void processElement(Tuple3<String, Integer, Long> tuple3,
//                                               Context context, Collector<String> collector) throws Exception {
//                        if (tuple3.f1 % 10 == 0) {
//                            throw new RuntimeException("自定义异常");
//                        }
//                        collector.collect(tuple3.toString());
//                        list.add(tuple3);
//                    }
//
//                    @Override
//                    public void close() throws Exception {
//                        log.error("close: " + LocalDateTime.now());
//                    }
//                })
                .keyBy(new KeySelector<Tuple3<String, Integer, Long>, String>() {
                    @Override
                    public String getKey(Tuple3<String, Integer, Long> s) throws Exception {
                        return s.f0;
                    }
                })
                .sum(1)
                .print();

        // run
        env.execute("local-tst-job");
    }

    public static class MyMapFunction implements MapFunction<Tuple3<String, Integer, Long>, Tuple3<String, Integer, Long>> {

        private static int count = 1;

        @Override
        public Tuple3<String, Integer, Long> map(Tuple3<String, Integer, Long> tuple3) throws Exception {
            if (tuple3.f1 % 10 == 0) {
                log.error("bad data... " + tuple3);
                throw new RuntimeException("自定义异常");
            }
            System.out.println("count=" + count++);
            return tuple3;
        }
    }

}
