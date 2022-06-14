package self.robin.examples;

/**
 * ...
 *
 * @author Robin-Li
 * @since: 2022-06-01 14:50
 */
public class EventTimeDemo {

    /**
     * 如果使用eventTime需要注意一下几点；
     * 问题1. 窗口存在不触发的情况：
     * 原因：窗口函数处理过程，由keyed算子, windowAssigner, Trigger(每个类型窗口有默认trigger,所以通常不用设置，如果自带的无法满足需要，可以自定义);
     * 与 processingTime不一样，eventTime的Trigger的触发，是通过event事件流更新TriggerContext中的currentTimestamp，
     * 然后判断 eventTime > currentTimestamp来驱动的；如果流中数据是间断的，将不能保证窗口按照预期触发；
     * <p>
     * ---------比如------------
     * dataStream
     * // 过滤指定条件的数据
     * .filter
     * // 过滤后的数据分配时间戳
     * ..assignTimestampsAndWatermarks(WatermarkStrategy.forBoundedOutOfOrderness(Duration.ofSeconds(2))
     * .withTimestampAssigner((event, timestamp) -> event.getAddTime()))
     * // 窗口操作
     * .keyedBy(...)
     * // 每秒触发1次
     * .window(TumblingEventTimeWindows.of(Time.seconds(1)))
     * // 窗口处理逻辑
     * .process(...);
     * // 打印
     * .print("abc")
     * --------------------------
     * 上面的过程可能不触发，因为过滤后可能间隔很久才会有一条数据，那么实际的效果，不是每秒触发一个窗口；实际的触发时机会和下一条数据到来的时刻有关；
     * <p>
     * 解决办法：
     * 1. 把时间戳分配放到过滤之前；以保证源源不断的数据流；可以及时的更新TriggerContext中的currentTimestamp;
     * 2. 使用processingTimeTrigger; 不推荐
     *
     * @param args
     */
    public static void main(String[] args) {

    }
}
