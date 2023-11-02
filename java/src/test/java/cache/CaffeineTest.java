package cache;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.google.common.util.concurrent.ThreadFactoryBuilder;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * ...
 *
 * @author Robin-Li
 * @since: 2023-10-20 10:51
 */
public class CaffeineTest {

    int cacheSize = 10;

    LoadingCache<Long, Object> cache;

    @Before
    public void init() {
        final BlockingQueue reloadTaskQueue = new LinkedBlockingDeque<>(cacheSize);
        final ThreadPoolExecutor reloadThreadPool = new ThreadPoolExecutor(3, 10,
                30, TimeUnit.SECONDS, reloadTaskQueue, new ThreadFactoryBuilder().setNameFormat("res-reload-%d").build());

        cache = Caffeine.newBuilder().maximumSize(cacheSize).softValues().executor(reloadThreadPool)
                .refreshAfterWrite(6, TimeUnit.SECONDS).expireAfterWrite(24, TimeUnit.HOURS)
                .recordStats().build(new CacheLoader<Long, Object>() {

                    @Override
                    public Object load(Long key) throws Exception {
                        return key + "-value";
                    }

                    @Override
                    public Map<Long, Object> loadAll(Iterable<? extends Long> keys) throws Exception {
                        Map<Long, Object> dataMap = new HashMap<>();
                        for (Long key : keys) {
                            dataMap.put(key, key + "-value");
                        }
                        return dataMap;
                    }
                });
    }

    @Test
    public void testRefreshAfterWrite() throws InterruptedException {
        new Thread(() -> {
            while (true) {
                cache.get(1l);
                try {
                    Thread.sleep(50);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }).start();
        synchronized (this){
            this.wait();
        }
    }
}
