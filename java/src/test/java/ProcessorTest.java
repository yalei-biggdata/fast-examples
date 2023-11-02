import org.apache.commons.lang3.StringUtils;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * ...
 *
 * @author Robin-Li
 * @since: 2022-04-27 17:01
 */
public class ProcessorTest {


    public static void main(String[] args) throws Exception {

        Map<String, List<Object>> store = new HashMap<>();
        Map<String, Object> extraInfo = new HashMap<>();
        extraInfo.put("abc_gdh", "hello");
        extraInfo.put("123_gdh", "hi");
        extraInfo.put("123_g333", "world");
        recordExtraInfo(extraInfo, store);
        Map<String, Object> extraInfo2 = new HashMap<>();
        extraInfo2.put("abc_gdh", "world!");
        recordExtraInfo(extraInfo2, store);
        System.out.println();
    }

    private static void recordExtraInfo(Map<String, Object> extraInfo, Map<String, List<Object>> store) {
        try {
            if (extraInfo == null || extraInfo.isEmpty()) {
                return;
            }
            extraInfo.forEach((key, value) -> {
                if (!StringUtils.endsWith(key, "_gdh")) {
                    return;
                }
                List<Object> list = store.get(key);
                if (list == null) {
                    list = new LinkedList<>();
                    store.put(key, list);
                }
                list.add(value);
            });
        } catch (Exception e) {
        }
    }


    //@MyData
    private String name;

}
