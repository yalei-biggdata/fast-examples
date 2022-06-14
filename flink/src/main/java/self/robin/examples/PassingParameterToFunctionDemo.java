package self.robin.examples;

import org.apache.flink.api.common.functions.RichMapFunction;
import org.apache.flink.api.java.DataSet;
import org.apache.flink.api.java.ExecutionEnvironment;
import org.apache.flink.api.java.utils.ParameterTool;
import org.apache.flink.configuration.Configuration;
import org.apache.flink.streaming.api.environment.StreamExecutionEnvironment;

import java.util.HashMap;

/**
 * ...
 *
 * @author Robin-Li
 * @since: 2022-06-10 15:39
 */
public class PassingParameterToFunctionDemo {

    /**
     * 方式一, 构造传参
     */
    public class $1Map<IN, OUT> extends RichMapFunction<IN, OUT> {

        private String dc;

        public $1Map(String dc) {
            this.dc = dc;
        }

        @Override
        public OUT map(IN value) throws Exception {
            return null;
        }
    }

    /**
     * 方式二， 注册全局变量
     */
    public static void main1(String[] args) {
        // 程序入参
        args = "-input 12".split("\\S");
        ParameterTool parameters = ParameterTool.fromArgs(args);

        // 测试常用
        HashMap<String, String> paramMap = new HashMap<>(parameters.toMap());
        paramMap.put("input", "12");
        parameters = ParameterTool.fromMap(paramMap);

        // set up the execution environment
        // 1. batch
        final ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
        env.getConfig().setGlobalJobParameters(parameters);

        // 2. stream
        final StreamExecutionEnvironment streamEnv = StreamExecutionEnvironment.getExecutionEnvironment();
        env.getConfig().setGlobalJobParameters(parameters);
    }

    // 使用
    public class $2Map<IN, OUT> extends RichMapFunction<IN, OUT> {

        private String dc;

        @Override
        public void open(Configuration parameters) throws Exception {
            ParameterTool parameterTool = (ParameterTool)
                    getRuntimeContext().getExecutionConfig().getGlobalJobParameters();
            dc = parameterTool.get("input");
        }

        @Override
        public OUT map(IN value) throws Exception {
            return null;
        }

    }

    /**
     * 方式三，withParameters(Configuration)
     * 貌似仅使用  DataSet
     */
    public static void main2(String[] args) {
        final ExecutionEnvironment env = ExecutionEnvironment.getExecutionEnvironment();
        DataSet<Integer> toMap = env.fromElements(1, 2, 3);

        Configuration config = new Configuration();
        config.setInteger("param", 99);

        toMap.map(new $3Map()).withParameters(config);
    }

    public static class $3Map<IN, OUT> extends RichMapFunction<IN, OUT> {

        private String dc;

        @Override
        public void open(Configuration parameters) throws Exception {
            dc = parameters.getString("param", "");
        }

        @Override
        public OUT map(IN value) throws Exception {
            return null;
        }
    }

    /**
     * 方法四，广播变量，broadcast; 适用需要传递一定数据量的方式
     */


}
