package self.robin.examples.hadoop;

import org.apache.commons.lang3.StringUtils;
import org.apache.hadoop.util.Shell;

/**
 * @Description: ...
 * @Author: Li Yalei - Robin
 * @Date: 2021/1/25 11:17
 */
public interface KeytabPathProvider {

    /**
     * 统一处理keytab文件路径
     * @param keytabUser
     * @return
     */
    default String getKeytabPath(String keytabUser) {
        if(StringUtils.isBlank(keytabUser)){
            throw new RuntimeException("keytabUser 不能为空");
        }
        if(Shell.WINDOWS){
            return  "file://" + this.getClass().getResource("/security").getPath() + "/" + keytabUser + ".keytab";
        }else {
            return "/" + keytabUser + ".keytab";
        }
    }
}
