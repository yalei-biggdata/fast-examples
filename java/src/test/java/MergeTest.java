import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * ...
 *
 * @author Robin-Li
 * @since: 2022-11-22 17:54
 */
public class MergeTest {

    public static void main(String[] args) {

        Map<Long, Map<String, Integer>> m1 = new HashMap<>();
        m1.put(1l, Collections.singletonMap("a", 2));
        m1.put(2l, Collections.singletonMap("b", 3));
        m1.put(3l, Collections.singletonMap("c", 4));

        Map<Long, Map<String, Integer>> m2 = new HashMap<>();
        m2.put(1l, Collections.singletonMap("a", 5));
        m2.put(2l, Collections.singletonMap("c", 6));
        m2.put(5l, Collections.singletonMap("d", 7));

        Map<Long, Map<String, Integer>> result = merge(m1, m2);
        System.out.println(result);
    }

    public static <T> T merge(T... maps) {
        if (maps == null || maps.length == 0) {
            return null;
        }
        if (maps[0] instanceof Map) {
            Map retMap = new HashMap();
            for (T map : maps) {
                ((Map) map).forEach((k, v) -> {
                    if (!retMap.containsKey(k)) {
                        retMap.put(k, v);
                        return;
                    }
                    Object v1 = retMap.get(k);
                    retMap.put(k, merge(v, v1));
                });
            }
            return (T) retMap;
        }
        if (maps[0] instanceof Integer) {
            return (T) Stream.of(maps).filter(Objects::nonNull)
                    .map(i -> ((Integer) i)).reduce((v1, v2) -> v1 + v2).orElse(0);
        }
        throw new UnsupportedOperationException("not implement");
    }

}
