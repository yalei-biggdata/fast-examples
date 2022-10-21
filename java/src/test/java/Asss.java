import com.alibaba.druid.sql.parser.SQLParserUtils;
import com.couchbase.client.core.env.SearchServiceConfig;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.env.DefaultCouchbaseEnvironment;
import com.couchbase.client.java.search.SearchQuery;
import com.couchbase.client.java.search.result.SearchQueryResult;
import com.couchbase.client.java.search.result.SearchQueryRow;
import com.couchbase.client.java.view.ViewQuery;
import com.couchbase.client.java.view.ViewResult;
import com.couchbase.client.java.view.ViewRow;
import lombok.AllArgsConstructor;
import lombok.Data;
import org.apache.commons.exec.util.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.junit.Test;

import javax.naming.ldap.HasControls;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * ...
 *
 * @author Robin-Li
 * @since: 2022-03-16 18:10
 */
public class Asss {

    @Test
    public void test() {

        Like like = new Like();
        And and = new And();

        List<BinaryExpression> list = Arrays.asList(like, and);

        ExpressionVisitor visitor = new ExpressionVisitor();
        for (BinaryExpression binaryExpression : list) {
            binaryExpression.accept(visitor);
        }
    }

    interface Expression {

        <R> R accept(Visitor<R> visitor);

        String asString();
    }

    abstract class BinaryExpression implements Expression {

        @Override
        public <R> R accept(Visitor<R> visitor) {
            return visitor.visit(this);
        }


    }

    class Like extends BinaryExpression {

        String str = "like";

        @Override
        public String asString() {
            return str;
        }
    }

    class And extends BinaryExpression {

        String str = "and";

        @Override
        public String asString() {
            return str;
        }
    }

    interface Visitor<R> {

        R visit(BinaryExpression binaryExpression);
    }

    class ExpressionVisitor implements Visitor<String> {

        @Override
        public String visit(BinaryExpression binaryExpression) {
            System.out.println("==>" + binaryExpression.asString());
            return null;
        }
    }

}
