package self.robin.examples;

import org.apache.flink.api.common.functions.MapFunction;
import org.apache.flink.streaming.api.datastream.SingleOutputStreamOperator;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

/**
 * ...
 *
 * @author Robin-Li
 * @since: 2022-06-14 18:10
 */
public class CheckoutDemo {

    public static void main(String[] args) throws Exception {

        StreamExecutionEnvironment env = StreamExecutionEnvironment.getExecutionEnvironment();
        env.setParallelism(1);
        //env.getConfig().setAutoWatermarkInterval();
        //env.setStreamTimeCharacteristic(TimeCharacteristic.EventTime);

        SingleOutputStreamOperator<Object> sourceStream = env.addSource(new EventTimeAllowedLatenessDemo.SelfSource())
                .map(new MapFunction<EventTimeAllowedLatenessDemo.DataWithTime, Object>() {
                    @Override
                    public Object map(EventTimeAllowedLatenessDemo.DataWithTime value) throws Exception {
                        System.out.println("value=>" + value);
                        return null;
                    }
                });
        env.execute("111");
    }
}
