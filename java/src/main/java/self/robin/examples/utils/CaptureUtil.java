package self.robin.examples.utils;

/**
 * @Description: ...
 * @Author: Li Yalei - Robin
 * @Date: 2021/5/13 11:48
 */

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.*;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.openqa.selenium.phantomjs.PhantomJSDriverService;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.remote.RemoteWebDriver;
import sun.misc.BASE64Encoder;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.awt.image.RasterFormatException;
import java.text.SimpleDateFormat;
import java.io.*;
import java.util.*;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeUnit;

public class CaptureUtil {
    private final static ResourceBundle resourceBundle=ResourceBundle.getBundle("application");

    //需要截图的url地址
    private final static String captureUrl=resourceBundle.getString("capture.captureUrl");

    //需要截图元素className
    private final static String className=resourceBundle.getString("capture.className");

    //截图的存放路径
    private final static String tempImgPath=resourceBundle.getString("capture.tempImgPath");

    public static WebDriver getPhantomJsDriver(){
        String osname=resourceBundle.getString("capture.os");
        if(osname.equalsIgnoreCase("linux")){
            System.setProperty("phantomjs.binary.path", "/usr/bin/phantomjs");
        }else{
            System.setProperty("phantomjs.binary.path", "target/classes/driver/phantomjs/phantomjs.exe");
        }
        DesiredCapabilities desiredCapabilities=DesiredCapabilities.phantomjs();
        desiredCapabilities.setJavascriptEnabled(true);

        ArrayList cliArgsCap = new ArrayList();
        cliArgsCap.add("--load-images=no");//关闭图片加载
        cliArgsCap.add("--disk-cache=yes");//开启缓存
        cliArgsCap.add("--ignore-ssl-errors=true");//忽略https错误
        desiredCapabilities.setCapability(PhantomJSDriverService.PHANTOMJS_CLI_ARGS, cliArgsCap);

        //设置请求头参数
        desiredCapabilities.setCapability("phantomjs.page.settings.userAgent","Mozilla/5.0 (Windows NT 6.3; Win64; x64; rv:50.0) Gecko/20100101 Firefox/50.0");
        desiredCapabilities.setCapability("phantomjs.page.customHeaders.User-Agent", "Mozilla/5.0 (Windows NT 6.3; Win64; x64; rv:50.0) Gecko/20100101 　　Firefox/50.0");
        desiredCapabilities.setCapability("acceptSslCerts",true);//支持ssl
        desiredCapabilities.setCapability("takesScreenshot",true);//支持截屏
        //css搜索支持
        desiredCapabilities.setCapability("cssSelectorsEnabled", true);
        //js支持
        desiredCapabilities.setJavascriptEnabled(true);
        return new PhantomJSDriver(desiredCapabilities);
    }
    //截取单个图片
    public static File capture() throws Exception{
        WebDriver driver=setDriver(getPhantomJsDriver());
        driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
        driver.manage().window().maximize();
        driver.get(captureUrl);
        File srcFile=((TakesScreenshot)driver).getScreenshotAs(OutputType.FILE);
        WebElement webElement =driver.findElement(By.className(className));
        File file = null;
        try {
            file = saveFile(webElement, tempImgPath);
        } catch (Exception e) {
            throw  new Exception(e.getMessage());
        }
        return file;
    }


    //将图片保存到本地
    public static File saveFile(WebElement webElement,String tempImgPath) throws Exception{
        //根据当天的日期每天建立一个临时文件夹用来存放截取得图片
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyy-MM-dd");
        String path=tempImgPath+"\\"+simpleDateFormat.format(new Date());

        //截图文件存放路径
        File targetFile=new File(path+"\\"+ UUID.randomUUID()+".png");
        //截图文件的父级目录
        File filePath=new File(targetFile.getParent());
        if(!filePath.exists()){
            filePath.mkdirs();
        }
        //截图文件
        File srcFile = captureElement(webElement);
        FileUtils.copyFile(srcFile, targetFile);
        System.out.println("图片已经生成并保存完毕");
        return targetFile;
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

    //将图片转换为base64字符串
    public static String getImageStr(String imgFile){
        InputStream in = null;
        byte[] data = null;
        try {
            in = new FileInputStream(imgFile);
            data = new byte[in.available()];
            in.read(data);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        BASE64Encoder encoder = new BASE64Encoder();
        return encoder.encode(data);
    }

    //自动登录并打开指定页面  进行截图
    public static List<File> captures() throws Exception{
        WebDriver driver=setDriver(getPhantomJsDriver());
        List<File> files=new ArrayList<>();
        driver.get(captureUrl);
        //List<WebElement> webElements =driver.findElements(By.xpath("//a[starts-with(@id,'ht')]"));
        //获取页面上的tab标签
        List<WebElement> webElements=driver.findElements(By.cssSelector("ul.am-nav-tabs>li>a"));
        if(webElements==null){
            throw new NoSuchElementException("没有找到指定的元素,请检查url或者页面元素");
        }
        try {
            for (WebElement tempWebElement : webElements) {
                tempWebElement.click();//点击当前标签
                Thread.sleep(1000);//等待1秒 等待数据加载完成
                WebElement webElement = driver.findElement(By.xpath("//div[contains(@class,'am-tab-panel am-active')]"));
                //WebElement webElement = driver.findElement(By.cssSelector("div[class $='am-tab-panel am-active']"));
                File file = saveFile(webElement, tempImgPath);
                files.add(file);
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            driver.quit();
        }
        return files;
    }
    //设置驱动参数
    private static WebDriver setDriver(WebDriver driver){
        driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);//设置等待时间  等待页面加载完成
        driver.manage().window().maximize();//窗口最大化
        return driver;
    }

    public static void main(String[] args) throws Exception {
        List<File> capture = captures();

    }
}