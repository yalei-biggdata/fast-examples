package proxy;

import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.bytecode.Proxy;
import org.apache.dubbo.common.extension.ExtensionLoader;
import org.apache.dubbo.rpc.*;
import org.apache.dubbo.rpc.proxy.InvokerInvocationHandler;
import org.apache.ibatis.executor.loader.javassist.JavassistProxyFactory;
import org.junit.Test;
import org.springframework.cglib.core.DebuggingClassWriter;
import org.springframework.cglib.proxy.Enhancer;
import org.springframework.cglib.proxy.MethodInterceptor;
import org.springframework.cglib.proxy.MethodProxy;

import java.lang.reflect.Method;

/**
 * ...
 *
 * @author Robin-Li
 * @since: 2023-11-01 17:55
 */
public class DynamicProxyTest {

    @Test
    public void test() {
        //在指定目录下生成动态代理类，我们可以反编译看一下里面到底是一些什么东西
        System.setProperty(DebuggingClassWriter.DEBUG_LOCATION_PROPERTY, "D:\\java\\java_workapace");

        //创建Enhancer对象，类似于JDK动态代理的Proxy类，下一步就是设置几个参数
        Enhancer enhancer = new Enhancer();
        //设置目标类的字节码文件
        enhancer.setSuperclass(Dog.class);
        //设置回调函数
        enhancer.setCallback(new MyMethodInterceptor());

        //这里的creat方法就是正式创建代理类
        Dog proxyDog = (Dog) enhancer.create();
        //调用代理类的eat方法
        proxyDog.eat();

    }

    @Test
    public void testInterface() {
        Invoker<MyInterface> invoker = new Invoker<MyInterface>() {
            @Override
            public Class<MyInterface> getInterface() {
                return MyInterface.class;
            }

            @Override
            public Result invoke(Invocation invocation) throws RpcException {
                return new AppResponse("hello");
            }

            @Override
            public URL getUrl() {
                return URL.valueOf("http://fakedomain");
            }

            @Override
            public boolean isAvailable() {
                return true;
            }

            @Override
            public void destroy() {

            }
        };
        // method 1
        Proxy proxy = Proxy.getProxy(MyInterface.class);
        MyInterface t0 = (MyInterface) proxy.newInstance(new InvokerInvocationHandler(invoker));
        // method 2
        ProxyFactory proxyFactory = ExtensionLoader.getExtensionLoader(ProxyFactory.class).getAdaptiveExtension();
        MyInterface t = proxyFactory.getProxy(invoker);
        System.out.println(t0.eat());
        System.out.println(t.eat());
    }

    public class MyMethodInterceptor implements MethodInterceptor {

        @Override
        public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
            System.out.println("这里是对目标类进行增强！！！");
            //注意这里的方法调用，不是用反射哦！！！
            Object object = proxy.invokeSuper(obj, args);
            return object;
        }
    }

}
