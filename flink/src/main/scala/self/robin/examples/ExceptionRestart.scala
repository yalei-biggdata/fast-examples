package self.robin.examples

import org.apache.flink.streaming.api.functions.source.SourceFunction
import org.apache.flink.streaming.api.scala.StreamExecutionEnvironment

/**
 * ...
 *
 * @author Robin-Li
 * @since: 2022-04-14 18:15  
 */
object ExceptionRestart {

    def main(args: Array[String]): Unit = {

//        val env = StreamExecutionEnvironment.getExecutionEnvironment
//
//        //env.setRestartStrategy(RestartStrategies.noRestart())
//
//        val source = env.addSource(new SourceFunction[(String, String, Long)]() {
//
//            override def run(sourceContext: SourceFunction.SourceContext[(String, String, Long)]): Unit = {
//                for (i <- 1 to 10) {
//                    sourceContext.collect(("key", "index:" + i, System.currentTimeMillis()))
//                }
//            }
//
//            override def cancel(): Unit = false
//        })
//
//        source.map(tuple3 => {
//
//        })
    }

}
