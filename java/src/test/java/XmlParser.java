import org.apache.ibatis.mapping.SqlSource;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.scripting.xmltags.XMLScriptBuilder;
import org.apache.ibatis.session.Configuration;
import self.robin.examples.utils.ibatis.MappersParser;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Li Yalei - Robin
 * @since: 2021/9/17 18:29
 */
public class XmlParser {

    public static void main(String[] args) {
        MappersParser parser = new MappersParser("mapper/**.xml");
        Map<String, Object> param = new HashMap<>();
        param.put("name", "xx");
        MappersParser.SqlModel sql = parser.getAndFlatParam("selecc", param);

        Configuration config = parser.getConfiguration();
        Map<String, XNode> frags = config.getSqlFragments();
        XNode sqlScp = frags.get("createTbl");
        XMLScriptBuilder builder = new XMLScriptBuilder(config, sqlScp, Object.class);
        SqlSource ds = builder.parseScriptNode();

        ds.getBoundSql(null);
        System.out.println();
    }
}
