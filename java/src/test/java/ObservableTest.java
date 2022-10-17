import io.reactivex.Observable;
import org.junit.Test;
import sun.misc.FileURLMapper;

import java.net.URL;
import java.util.concurrent.TimeUnit;

/**
 * ...
 *
 * @author Robin-Li
 * @since: 2022-06-23 10:17
 */
public class ObservableTest {

    @Test
    public void test() throws Exception{
        String url = "file:/C:/Users/PC/.m2/ab.jar";
        FileURLMapper file = new FileURLMapper(new URL(url));
        System.out.println(file.exists());
    }
}
