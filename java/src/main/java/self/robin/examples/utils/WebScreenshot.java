package self.robin.examples.utils;

import it.grabz.grabzit.GrabzItClient;
import it.grabz.grabzit.parameters.ImageOptions;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.remote.DesiredCapabilities;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

/**
 * https://www.baeldung.com/java-selenium-screenshots
 *
 * https://grabz.it/api/java/image-capture-options/
 *
 * https://www.tutorialspoint.com/how-to-get-screenshot-of-full-webpage-using-selenium-and-java
 *
 *  Selenium / phantomjs / HtmlImageGenerator
 * @Description: ...
 * @Author: Robin-Li
 * @DateTime: 2021-05-12 23:46
 */
public class WebScreenshot {

    public static void main(String[] args) throws Exception {

        GrabzItClient grabzIt = new GrabzItClient("Sign in to view your Application Key", "Sign in to view your Application Secret");

        ImageOptions options = new ImageOptions();
        options.setCustomId("123456");

        grabzIt.URLToImage("https://www.tesla.com", options);
        //Then call the Save method
        grabzIt.Save("http://www.example.com/handler");

        m2();
    }



    public static void m2() throws Exception{
        System.setProperty("webdriver.chrome.driver", "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe");

        Capabilities capabilities = DesiredCapabilities.chrome();
        ChromeDriver driver = new ChromeDriver(capabilities);
        driver.manage()
                .timeouts()
                .implicitlyWait(5, TimeUnit.SECONDS);

        driver.get("https://www.baidu.com/");

        File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
        FileUtils.copyFile(src, new File("D:/test.jpg"));
    }

    public static void localShot() throws Exception{
        System.out.println(Desktop.isDesktopSupported());
        Desktop.getDesktop().browse(new URL("https://www.baidu.com").toURI());

        Robot robot = new Robot();
        robot.delay(10000);
        Dimension d = new Dimension(Toolkit.getDefaultToolkit().getScreenSize());
        int width = (int) d.getWidth();
        int height = (int) d.getHeight();
        //最大化浏览器
        robot.keyRelease(KeyEvent.VK_F11);
        robot.delay(2000);
        Image image = robot.createScreenCapture(new Rectangle(0, 0, width,
                height));
        BufferedImage bi = new BufferedImage(width, height,
                BufferedImage.TYPE_INT_RGB);
        Graphics g = bi.createGraphics();
        g.drawImage(image, 0, 0, width, height, null);
        //保存图片
        ImageIO.write(bi, "jpg", new File("D:/google.jpg"));
    }
}
