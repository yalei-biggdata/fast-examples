package self.robin.examples;

import lombok.extern.slf4j.Slf4j;
import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.api.java.tuple.Tuple3;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;
import org.apache.flink.streaming.api.functions.source.SourceFunction;

/**
 * ...
 *
 * @author Robin-Li
 * @since: 2022-04-14 19:24
 */
@Slf4j
public class ExceptionRestartJavaDemo {

    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);

        // 不需要重试
        //env.setRestartStrategy(RestartStrategies.noRestart());

        // 固定重试次数
        //env.setRestartStrategy(RestartStrategies.fixedDelayRestart(3, Time.seconds(3)));

        // 固定频率的重试, param0: 最多失败几次, param1：在指定时间内, param2: 异常间隔（超过这个时间，异常次数清0，重新计数）
        //env.setRestartStrategy(RestartStrategies.failureRateRestart(5, Time.seconds(10), Time.milliseconds(10)));

        env.addSource(new SourceFunction<Tuple3<String, Integer, Long>>() {
            @Override
            public void run(SourceContext<Tuple3<String, Integer, Long>> sourceContext) throws Exception {
                int count = 1;
                while (true) {
                    sourceContext.collect(new Tuple3<>("key_" + count, count++, System.currentTimeMillis()));
                    Thread.sleep(100);
                }
            }

            @Override
            public void cancel() {
            }
        }).map(new MapFunction<Tuple3<String, Integer, Long>, Tuple3<String, Integer, Long>>() {
            @Override
            public Tuple3<String, Integer, Long> map(Tuple3<String, Integer, Long> o) throws Exception {
                if (o.f1 % 10 == 0) {
                    log.error("Bad data ... " + o);
                    throw new RuntimeException("自定义异常");
                }
                return o;
            }
        }).print();

        // run
        env.execute("jobName: " + ExceptionRestartJavaDemo.class.getSimpleName());
    }

}
