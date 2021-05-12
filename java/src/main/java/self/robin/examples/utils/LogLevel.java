package self.robin.examples.utils;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.LoggerContext;
import lombok.extern.log4j.Log4j2;
import org.slf4j.LoggerFactory;

@Log4j2
public class LogLevel {

    public static String setKafkaLogLevel(Level newLevel) {
        return setLogLevel(newLevel,"org.apache.kafka");
    }

    public static String setSpringLogLevel(Level newLevel) {
        return setLogLevel(newLevel,"org.springframework");
    }

    public static String setMybatisLogLevel(Level newLevel) {
        return setLogLevel(newLevel,"org.mybatis");
    }

    public static String setLogLevel(Level newLevel, Class<?> clazz){
        return setLogLevel(newLevel, clazz.getName());
    }

    public static String setLogLevel(Level newLevel,String... packagePath){
        try {
            LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory();
            for (String pk : packagePath) {
                loggerContext.getLogger(pk).setLevel(newLevel);
            }
        } catch (Exception e) {
            System.out.println("动态修改日志级别出错");
            return "fail";
        }
        return "success";
    }
}
