package thread;

import lombok.Getter;
import org.HdrHistogram.Histogram;
import org.databene.contiperf.PerfTest;
import org.databene.contiperf.junit.ContiPerfRule;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import self.robin.examples.utils.thread.DynamicThreadPoolExecutor;

import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Collection;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ...
 *
 * @author Robin-Li
 * @since: 2023-05-02 11:51
 */
@RunWith(Parameterized.class)
public class ThreadTest {

    @Getter
    static class Param {

        int coreSize;

        int maxSize;

        int queueSize;

        public static Param of(int coreSize, int maxSize, int queueSize) {
            Param p = new Param();
            p.coreSize = coreSize;
            p.maxSize = maxSize;
            p.queueSize = queueSize;
            return p;
        }
    }


    // 引入 ContiPerf 进行性能测试
    @Rule
    public ContiPerfRule contiPerfRule = new ContiPerfRule();

    ThreadPoolTaskExecutor pool;

    SecureRandom random;

    Histogram histogramWaitInQueue;

    Histogram histogramTaskExecute;

    private AtomicInteger atomicInteger = new AtomicInteger();

    private void work(int border, boolean fixate) {
        try {
            Thread.sleep(fixate ? border : random.nextInt(border));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Param param;

    public ThreadTest(Param params) {
        this.param = params;
    }

    private ThreadPoolTaskExecutor commonPool() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(param.getCoreSize());
        executor.setMaxPoolSize(param.getMaxSize());
        executor.setQueueCapacity(param.getQueueSize());
        executor.setKeepAliveSeconds(30);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardOldestPolicy());
        executor.initialize();
        return executor;
    }

    private ThreadPoolTaskExecutor dynamicPool() {
        DynamicThreadPoolExecutor executor = new DynamicThreadPoolExecutor();
        executor.setCorePoolSize(param.getCoreSize());
        executor.setMaxPoolSize(param.getMaxSize());
        executor.setQueueCapacity(param.getQueueSize());
        executor.setKeepAliveSeconds(3);
        executor.setExpectMaxWaitTime(10);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        executor.initialize();
        return executor;
    }

    private ThreadPoolTaskExecutor dynamicPool2() {
        self.robin.examples.utils.thread2.DynamicThreadPoolExecutor executor = new self.robin.examples.utils.thread2.DynamicThreadPoolExecutor();
        executor.setCorePoolSize(param.getCoreSize());
        executor.setMaxPoolSize(param.getMaxSize());
        executor.setQueueCapacity(param.getQueueSize());
        executor.setKeepAliveSeconds(3);
        executor.setExpectMaxWaitTime(10);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        executor.initialize();
        return executor;
    }

    ScheduledExecutorService thread;

    @After
    public void after() {
        System.out.println("all_count=" + atomicInteger.get());
        histogramWaitInQueue.outputPercentileDistribution(System.out, 3, 1000000.0);
        histogramTaskExecute.outputPercentileDistribution(System.out, 3, 1000000.0);
        pool.shutdown();
        thread.shutdown();
    }

    @Before
    public void before() {
        pool = commonPool();
        thread = Executors.newSingleThreadScheduledExecutor();
        thread.scheduleAtFixedRate(() -> {
            System.out.println("==> poolSize= " + pool.getPoolSize() + ", coreSize=" + pool.getCorePoolSize() + ", active=" + pool.getActiveCount()
                    + ", queue=" + pool.getThreadPoolExecutor().getQueue().size());
        }, 1, 1, TimeUnit.SECONDS);
        random = new SecureRandom();
        histogramWaitInQueue = new Histogram(2000000000, 3);
        histogramTaskExecute = new Histogram(2000000000, 3);
    }

    @Test
    //@PerfTest(invocations = 5)
    public void demo() throws Exception {
    }


    @Parameterized.Parameters
    public static Collection parameters() {
        Param[][] params = new Param[][]{
                {Param.of(2, 100, 0)}
        };

        return Arrays.asList(params);
    }

    @Test
    //@PerfTest(threads = 10, duration = 60000, rampUp = 1000, warmUp = 9000)
    @PerfTest(threads = 30, duration = 20000, rampUp = 100, warmUp = 1000)
    public void test() throws ExecutionException, InterruptedException {
        int counter = atomicInteger.incrementAndGet();
        final long st = System.nanoTime();
        Future<?> future = pool.submit(() -> {
            histogramWaitInQueue.recordValue(System.nanoTime() - st);
            work(100, true);
        });
        try {
            future.get(1, TimeUnit.SECONDS);
        } catch (TimeoutException timeoutException) {
            timeoutException.printStackTrace();
        }
        histogramTaskExecute.recordValue(System.nanoTime() - st);
        if (counter % 100 == 0) {
            System.out.println("counter=" + counter);
        }
    }

}
