package self.robin.javaagent.domain;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * ...
 *
 * @author Robin-Li
 * @since: 2023-11-08 21:39
 */
public class Demo {

    private String name = "dog";

    public String say() {
        String tName = name;
        // ----------
        int startIdx = 0;
        for (; startIdx < 3; startIdx++) {
        }
        return tName;
    }

    public String run() {
        Collection<Integer> c1 = Arrays.asList(1);
        List<Integer> l1 = new LinkedList<>();
        Collection<Integer> c2 = Arrays.asList(1);
        Collection<String> c3 = Arrays.asList("1");
        return name;
    }
}
