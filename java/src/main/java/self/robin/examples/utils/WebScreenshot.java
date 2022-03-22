package self.robin.examples.utils;

import com.ruiyun.jvppeteer.core.Puppeteer;
import com.ruiyun.jvppeteer.core.browser.Browser;
import com.ruiyun.jvppeteer.core.browser.BrowserFetcher;
import com.ruiyun.jvppeteer.core.page.Page;
import com.ruiyun.jvppeteer.options.Clip;
import com.ruiyun.jvppeteer.options.LaunchOptions;
import com.ruiyun.jvppeteer.options.LaunchOptionsBuilder;
import com.ruiyun.jvppeteer.options.ScreenshotOptions;
import org.apache.commons.io.FileUtils;
import org.openqa.selenium.Point;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;

import javax.imageio.ImageIO;
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.awt.image.RasterFormatException;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

/**
 * https://www.baeldung.com/java-selenium-screenshots
 *
 * https://grabz.it/api/java/image-capture-options/
 *
 * https://www.tutorialspoint.com/how-to-get-screenshot-of-full-webpage-using-selenium-and-java
 *
 *  Selenium / phantomjs / HtmlImageGenerator
 *
 *  https://zhuanlan.zhihu.com/p/145054936
 *
 *  https://www.cnblogs.com/firstdream/p/5123004.html
 *
 *  https://github.com/fanyong920/jvppeteer
 *
 *  https://blog.csdn.net/u014086054/article/details/89851898
 *
 *  https://blog.csdn.net/FG24151110876/article/details/88373332
 *
 *  仅封装了phantomjs， 为了方便操作， 没有实质性的东西
 *        <dependency>
 *             <groupId>com.github.jarlakxen</groupId>
 *             <artifactId>embedphantomjs</artifactId>
 *             <version>3.0</version>
 *         </dependency>
 *
 * @Description: ...
 * @Author: Robin-Li
 * @DateTime: 2021-05-12 23:46
 */
public class WebScreenshot {

    public static void main(String[] args) throws Exception {

        BrowserFetcher.downloadIfNotExist(null);
        ArrayList<String> arrayList = new ArrayList<>();
        LaunchOptions options = new LaunchOptionsBuilder().withArgs(arrayList).withHeadless(true).withExecutablePath("C:\\Program Files (x86)\\Google\\Chrome\\Application\\chrome.exe").build();
        arrayList.add("--no-sandbox");
        arrayList.add("--disable-setuid-sandbox");
        Browser browser = Puppeteer.launch(options);
        Page page = browser.newPage();
        page.goTo("https://www.baidu.com/?tn=98012088_10_dg&ch=3");
        ScreenshotOptions screenshotOptions = new ScreenshotOptions();
        //设置截图范围
        Clip clip = new Clip(1.0,1.56,400,400);
        screenshotOptions.setClip(clip);
        //设置存放的路径
        screenshotOptions.setPath("D:/tmp/test.png");
        page.screenshot(screenshotOptions);


//        GrabzItClient grabzIt = new GrabzItClient("Sign in to view your Application Key", "Sign in to view your Application Secret");
//
//        ImageOptions options = new ImageOptions();
//        options.setCustomId("123456");
//
//        grabzIt.URLToImage("https://www.tesla.com", options);
//        //Then call the Save method
//        grabzIt.Save("http://www.example.com/handler");

        //设置必要参数
        //ssl证书支持
//        DesiredCapabilities dcaps = DesiredCapabilities.chrome();
//        dcaps   .setCapability("acceptSslCerts", true);
//        //截屏支持
//        dcaps.setCapability("takesScreenshot", true);
//        //css搜索支持
//        dcaps.setCapability("cssSelectorsEnabled", true);
//        //js支持
//        dcaps.setJavascriptEnabled(true);
//        //驱动支持（第二参数表明的是你的phantomjs引擎所在的路径）
//        dcaps.setCapability(PhantomJSDriverService.PHANTOMJS_EXECUTABLE_PATH_PROPERTY,
//                "D:\\ProgramLocation\\phantomjs-2.1.1-windows\\bin\\phantomjs.exe");
//        RemoteWebDriverBuilder driverBuilder = PhantomJSDriver.builder().withDriverService(PhantomJSDriverService.createDefaultService(dcaps)).url("https://www.baidu.com");
//        WebDriver driver = driverBuilder.build();
//
//        WebElement element = driver.findElement(By.id("head_wrapper"));
//        File file = captureElement(element);
//        FileUtils.copyFile(file, new File("D:/driver.png"));

        //puppeteer

       // m3();
    }

    //根据元素截取图片
    public static File captureElement(WebElement element) throws RasterFormatException, IOException {
        // TODO Auto-generated method stub
        WrapsDriver wrapsDriver = (WrapsDriver) element;
        // 截图整个页面
        File screen = ((RemoteWebDriver) wrapsDriver.getWrappedDriver()).getScreenshotAs(OutputType.FILE);
        int width = element.getSize().getWidth();
        int height = element.getSize().getHeight();
        // 创建一个矩形使用上面的高度，和宽度
        java.awt.Rectangle rect = new java.awt.Rectangle(width, height);
        // 得到元素的坐标
        Point p = element.getLocation();
        try {
            BufferedImage img = ImageIO.read(screen);
            // 获得元素的高度和宽度
            BufferedImage dest = img.getSubimage(p.getX(), p.getY(), rect.width, rect.height);
            // 存为png格式
            ImageIO.write(dest, "png", screen);
        }catch (RasterFormatException e){
            throw new RasterFormatException(e.getMessage());
        }
        catch (IOException e) {
            throw  new IOException(e.getMessage());
        }
        return screen;
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
