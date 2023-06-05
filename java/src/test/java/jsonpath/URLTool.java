package jsonpath;

import com.sun.deploy.util.URLUtil;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * ...
 *
 * @author Robin-Li
 * @since: 2023-05-15 18:03
 */
public class URLTool {

    public static String replaceDomain(String domain, String url) {
        if (StringUtils.isBlank(domain) || url == null) {
            return url;
        }
        url = url.trim();
        if ("".equals(url)) {
            return url;
        }
        int left = url.indexOf("://") + 3;
        int right = url.indexOf("/", left);
        if (left == 2 || right == -1) {
            return url;
        }
        return url.substring(0, left) + domain + url.substring(right);
    }

    public static Map<String, String> parseParams(String url) {
        int delimiter = url.indexOf("?");
        if (delimiter == -1) {
            return Collections.emptyMap();
        }
        String paramStr = url.substring(delimiter + 1);
        Map<String, String> stringMap = new HashMap<>();
        for (String pairStr : paramStr.split("&")) {
            String[] pair = pairStr.split("=");
            stringMap.put(pair[0], pair.length >= 2 ? pair[1] : null);
        }
        return stringMap;
    }
}
