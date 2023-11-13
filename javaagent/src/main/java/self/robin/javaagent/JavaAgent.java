package self.robin.javaagent;

import self.robin.javaagent.domain.Demo;
import self.robin.javaagent.transformer.DemoTransformer;

import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;

/**
 * ...
 *
 * @author Robin-Li
 * @since: 2023-11-08 18:21
 */
public class JavaAgent {

    /**
     * 以 vm 参数的方式载入，在 java 程序的 main 方法执行之前执行
     *
     * @param agentArgs
     * @param inst      Agent技术主要使用的 api，我们可以使用它来改变和重新定义类的行为
     */
    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println("premain start");

        inst.addTransformer(new DemoTransformer());
        System.out.println(agentArgs);
    }

    /**
     * 以 Attach 的方式载入，在 Java 程序启动后执行
     */
    public static void agentmain(String agentArgs, Instrumentation inst) {
        System.out.println("agentmain start");
        DemoTransformer demoTransformer = new DemoTransformer();
        inst.addTransformer(demoTransformer, true);
        Class[] allLoadedClasses = inst.getAllLoadedClasses();
        try {
            inst.retransformClasses(Demo.class);
            /*for (int i = 0; i < allLoadedClasses.length; i++) {
                inst.retransformClasses(allLoadedClasses[i]);
            }*/
        } catch (UnmodifiableClassException e) {
            e.printStackTrace();
        }
        System.out.println(agentArgs);
    }
}
