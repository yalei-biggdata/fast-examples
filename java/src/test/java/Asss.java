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

    public static void main(String[] args) {
        Action1 action1 = new Action1() {
            @Override
            public Object run1() throws Exception {
                return "111";
            }
        };
        new Dog1((Callable) action1);
    }

    @Data
    @AllArgsConstructor
    static class ObjX {

        private int id;

        private String name;

    }

    public void doMethod() {
        new Dog(this::process);
    }

    class Dog {
        public Dog(Action action) {
        }
    }

    static class Dog1<V>{
        public Dog1(Callable<V> callable){
            try {
                System.out.println(callable.call());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }



    public void process(Action.Controller c) {
    }

    public interface Action {

        void runDefaultAction(Controller controller) throws Exception;

        interface Controller {
        }
    }

    public interface Action1<V>{

        V run1() throws Exception;
    }
}
