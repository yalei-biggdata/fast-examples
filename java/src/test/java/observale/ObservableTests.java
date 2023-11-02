package observale;

import com.couchbase.client.core.ClusterFacade;
import com.couchbase.client.core.message.kv.GetRequest;
import com.couchbase.client.core.message.kv.GetResponse;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.bucket.api.Get;
import com.couchbase.client.java.document.Document;
import com.couchbase.client.java.document.StringDocument;
import com.couchbase.client.java.env.CouchbaseEnvironment;
import com.couchbase.client.java.env.DefaultCouchbaseEnvironment;
import com.couchbase.client.java.transcoder.Transcoder;
import io.reactivex.Observable;
import io.reactivex.schedulers.Schedulers;
import org.HdrHistogram.Histogram;
import org.apache.commons.lang3.tuple.Pair;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import rx.Subscriber;
import rx.functions.Func0;
import rx.functions.Func1;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

import static com.couchbase.client.java.bucket.api.Utils.addRequestSpan;
import static com.couchbase.client.java.bucket.api.Utils.applyTimeout;
import static com.couchbase.client.java.util.OnSubscribeDeferAndWatch.deferAndWatch;

/**
 * ...
 *
 * @author Robin-Li
 * @since: 2023-06-19 15:33
 */
public class ObservableTests {

    Bucket bucket;

    @Before
    public void before() {
        DefaultCouchbaseEnvironment env = DefaultCouchbaseEnvironment.builder().computationPoolSize(20).build();
        final Cluster cluster =
                CouchbaseCluster.create("bjzyx.public391-bjzyx.1.qiyi.cb:8091", "bjzyx.public391-bjzyx.2.qiyi.cb:8091");
        bucket = cluster.openBucket("grp_common_light_sharing_lib", "Ijd6j2RR", 60000L, TimeUnit.MILLISECONDS);
    }

    @After
    public void clean() {
        bucket.close();
    }

    @Test
    public void test10() throws Exception {
        String testID = "test_id";
        bucket.upsert(StringDocument.create(testID, 60, "115eb886b8df102a59adb52a407ae1cf%2C1687144465%234771783817550500%3Anull%3A100%3A12%2C1878371603232501%3Anull%3A100%3A3%2C12144984400%3Anull%3A37%3A3%2C7973227715695700%3Anull%3A100%3A12%2C200919601%3Anull%3A100%3A12%2C3886192721520300%3Anull%3A100%3A3%2C6632006418310000%3Anull%3A37%3A3%2C3955178982927801%3Anull%3A100%3A12"));
        int size = bucket.async().core().toBlocking().single().ctx().environment().computationPoolSize();
        System.out.println("size=" + size);

        for (int i = 0; i < 5; i++) {
            bucket.async().get(StringDocument.create(testID)).map(x -> {
                System.out.println("========" + System.currentTimeMillis());
                return x;
            }).subscribe();
        }

        System.out.println("done " + System.currentTimeMillis());
        Thread.sleep(2000);
    }

    public static <D extends Document<?>> rx.Observable<D> get(final String id, final Class<D> target,
                                                               final CouchbaseEnvironment environment, final String bucket, final ClusterFacade core,
                                                               final Map<Class<? extends Document>, Transcoder<? extends Document, ?>> transcoders,
                                                               final long timeout, final TimeUnit timeUnit) {
        return rx.Observable.defer(() -> {
            final GetRequest request = new GetRequest(id, bucket);
            addRequestSpan(environment, request, "get");

            rx.Observable<D> map = deferAndWatch(
                    (Func1<Subscriber, rx.Observable<GetResponse>>) s -> {
                        request.subscriber(s);
                        return core.send(request);
                    }
            ).filter(new Get.GetFilter(environment)).map(new Get.GetMap<D>(environment, transcoders, target, id));

            return applyTimeout(map, request, environment, timeout, timeUnit);
        });
    }

    @Test
    public void test1() throws Exception {
        ExecutorService pool = Executors.newFixedThreadPool(10);
        CountDownLatch latch = new CountDownLatch(1);
        Histogram histogram = new Histogram(500000000, 3);
        // 分组后，每组分配一个线程
        Observable.range(1, 15)
                .groupBy(idx -> idx % 3)
                // Schedulers.from(pool) 可替换成 Schedulers.io()
                .map(group -> group.subscribeOn(Schedulers.from(pool)).flatMap(x -> {
                    Math.pow(5, 5);
                    return Observable.just(x);
                }))
                .flatMap(x -> x)
                .doOnComplete(() -> latch.countDown())
                .subscribe(v -> {
                    System.out.println(v + " == " + Thread.currentThread().getName() + " " + System.currentTimeMillis());
//                    v.subscribe(idx -> {
//                        System.out.println(idx + " " + Thread.currentThread().getName() + " " + System.currentTimeMillis());
//                    });
                });
        latch.await(10, TimeUnit.MILLISECONDS);
        histogram.outputPercentileDistribution(System.out, 3, 100000.0);

        System.out.println("--------------------");
        pool.shutdown();
        Thread.sleep(5000);
    }

    @Test
    public void test2() throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        rx.Observable.range(1, 15)
                .groupBy(idx -> idx % 5)
                // Schedulers.from(pool) 可替换成 Schedulers.io()
                .map(group -> group.subscribeOn(rx.schedulers.Schedulers.from(pool))/*.flatMap(key -> {
                    return rx.Observable.just(key);
                })*/)
                .flatMap(x -> x)
                .doOnCompleted(() -> latch.countDown())
                .subscribe(v -> {
                    System.out.println(v + " == " + Thread.currentThread().getName() + " " + System.currentTimeMillis());
////                    v.subscribe(idx -> {
////                        System.out.println(idx + " " + Thread.currentThread().getName() + " " + System.currentTimeMillis());
//                    });
                });
        latch.await(10, TimeUnit.SECONDS);
    }

    @Test
    public void test4() throws Exception {
        bucket.async().get("").subscribe();
        String testID = "test_id";
        bucket.upsert(StringDocument.create(testID, 60, "115eb886b8df102a59adb52a407ae1cf%2C1687144465%234771783817550500%3Anull%3A100%3A12%2C1878371603232501%3Anull%3A100%3A3%2C12144984400%3Anull%3A37%3A3%2C7973227715695700%3Anull%3A100%3A12%2C200919601%3Anull%3A100%3A12%2C3886192721520300%3Anull%3A100%3A3%2C6632006418310000%3Anull%3A37%3A3%2C3955178982927801%3Anull%3A100%3A12"));
        // query
        Function<Integer, rx.Observable<StringDocument>> dataProvider = key -> {
            return bucket.async().get(testID, StringDocument.class);
        };
        //
        Histogram histogram = new Histogram(5000, 3);
        for (int i = 0; i < 20; i++) {
            long st = System.currentTimeMillis();
            //blocking(dataProvider, 100);
            poolPar(dataProvider, 100);
            long diff = System.currentTimeMillis() - st;
            histogram.recordValue(diff);
        }
        histogram.outputPercentileDistribution(System.out, 3, 1.0);
    }

    // CountDownLatch latch
    ExecutorService pool = Executors.newFixedThreadPool(10);

    private void poolPar(Function<Integer, rx.Observable<StringDocument>> dataProvider, int times) throws Exception {
        CountDownLatch latch = new CountDownLatch(1);
        rx.Observable.range(1, times)
                .groupBy(idx -> idx % 1)
                // Schedulers.from(pool) 可替换成 Schedulers.io()
                .map(group -> group.subscribeOn(rx.schedulers.Schedulers.from(pool)).flatMap(key -> {
                    return dataProvider.apply(key).map(t -> Pair.of(key, t));
                }))
                //.doOnCompleted(() -> latch.countDown())
                .subscribe(v -> {
                    v.doOnCompleted(() -> latch.countDown());
//                    System.out.println(v + " == " + Thread.currentThread().getName() + " " + System.currentTimeMillis());
//                    v.subscribe(idx -> {
//                        System.out.println(idx + " " + Thread.currentThread().getName() + " " + System.currentTimeMillis());
//                    });
                });
        latch.await(10, TimeUnit.SECONDS);
    }

    private void blocking(Function<Integer, rx.Observable<StringDocument>> dataProvider, int times) {
        List<Pair<Integer, StringDocument>> result = rx.Observable.range(1, times)
                .flatMap(key -> dataProvider.apply(key).map(t -> Pair.of(key, t)))
                .toList().toBlocking().single();
    }
}
