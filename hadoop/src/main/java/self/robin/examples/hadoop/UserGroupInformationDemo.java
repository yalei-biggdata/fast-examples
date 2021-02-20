package self.robin.examples.hadoop;

import lombok.extern.slf4j.Slf4j;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.fs.FileSystem;
import org.apache.hadoop.security.UserGroupInformation;
import java.security.PrivilegedExceptionAction;
import java.util.function.BiFunction;

/**
 * @Description: ...
 * @Author: Li Yalei - Robin
 * @Date: 2021/2/5 10:55
 */
@Slf4j
public class UserGroupInformationDemo implements KeytabPathProvider {

    public <R> R doAs(String user, BiFunction<Configuration, FileSystem, R> function) throws Exception {

        Configuration hadoopConf = new Configuration();
        hadoopConf.set("hadoop.security.authentication", "kerberos");
        hadoopConf.setBoolean("fs.hdfs.impl.disable.cache", true);

        try {
            String keytabFilePath = getKeytabPath(user);

            UserGroupInformation.setConfiguration(hadoopConf);
            UserGroupInformation ugi = UserGroupInformation.loginUserFromKeytabAndReturnUGI(user + "@HADOOP.QIYI.COM", keytabFilePath);
            R r = ugi.doAs(new PrivilegedExceptionAction<R>() {

                @Override
                public R run() throws Exception {
                    try {
                        FileSystem fs = FileSystem.get(hadoopConf);
                        return function.apply(hadoopConf, fs);
                    } catch (Exception ex) {
                        log.error("init file system failed because of " + ex.toString());
                        throw ex;
                    }
                }
            });
            return r;
        } catch (Exception ex) {
            log.error("init hdfs instance failed with exception " + ex.getMessage());
            throw ex;
        }
    }

    public static void main(String[] args) throws Exception{
        UserGroupInformation.loginUserFromKeytab("principal", "keytab");

    }
}
