package self.robin.examples.spark.sources.excel;

import scala.Option;
import scala.collection.immutable.Map;

import java.io.Serializable;

/**
 * @Description: ...
 * @Author: Li Yalei - Robin
 * @Date: 2020/12/23 14:05
 */
public class ExcelOptions implements Serializable {

    private Map<String, String> options;

    public ExcelOptions(Map<String, String> options) {
        this.options = options;
    }

    public String getString(String key){
        return getString(key, null);
    }

    public String getString(String key, String defaultValue){
        Option<String> value = this.options.get(key);
        if(value.isDefined()){
            return value.get();
        }
        return defaultValue;
    }

    public Integer getInteger(String key){
        Option<String> value = this.options.get(key);
        if(value.isDefined()){
            try{
                return Integer.parseInt(value.get());
            }catch (NumberFormatException exception){
                throw new RuntimeException(key+" should be an integer. Found "+value.get());
            }
        }
        return null;
    }

    public Boolean getBoolean(String key){
        Option<String> value = this.options.get(key);
        if(value.isDefined()){
            try{
                return Boolean.valueOf(value.get());
            }catch (NumberFormatException exception){
                throw new RuntimeException(key+" should be an boolean. Found "+value.get());
            }
        }
        return null;
    }

}
