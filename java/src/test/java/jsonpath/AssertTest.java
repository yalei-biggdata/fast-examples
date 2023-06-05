package jsonpath;

import com.jayway.jsonpath.JsonPath;
import net.minidev.json.JSONArray;
import org.apache.commons.io.IOUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;

/**
 * ...
 *
 * @author Robin-Li
 * @since: 2023-05-06 13:37
 */
public class AssertTest {

    String jsonDocument;

    @After
    public void done() throws IOException {
    }

    @Before
    public void init() throws IOException {
        List<String> lines = IOUtils.readLines(getClass().getResource("/data.json").openStream(), StandardCharsets.UTF_8);
        jsonDocument = String.join("", lines);
    }

    @Test
    public void test1() throws IOException {
        JSONArray object = JsonPath.read(jsonDocument, "$..rec_items[?(@.pingback.r_source=~/.*103.*/i)]");
        System.out.println(object.size());
    }

}
