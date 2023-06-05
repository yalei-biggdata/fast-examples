package anno;

import org.junit.Test;
import self.robin.examples.asm.MyData;

import java.lang.reflect.Method;

/**
 * ...
 *
 * @author Robin-Li
 * @since: 2023-05-25 11:35
 */
public class MyDataTest {

    @MyData
    private String hello;

    @Test
    public void test() {
        MyDataTest md = new MyDataTest();
        for (Method method : md.getClass().getMethods()) {
            System.out.println(method);
        }
    }

}
