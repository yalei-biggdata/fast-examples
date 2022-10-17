import org.junit.Test;
import self.robin.examples.utils.QpsMeter;
import self.robin.examples.utils.thread.DynamicThreadPoolExecutor;

import java.util.concurrent.Callable;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicLong;

/**
 * ...
 *
 * @author Robin-Li
 * @since: 2022-09-29 10:25
 */
public class ThreadTests {

    @Test
    public void test() {
        DynamicThreadPoolExecutor executor = new DynamicThreadPoolExecutor();
        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(60);
        executor.setQueueCapacity(12);
        executor.setKeepAliveSeconds(1);
        executor.setExpectMaxWaitTime(10);
        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.DiscardOldestPolicy());
//        executor.setRejectedExecutionHandler(new ThreadPoolExecutor.AbortPolicy());
        executor.initialize();

        int maxParallel = 10;

        QpsMeter qpsMeter = new QpsMeter();

        int index = 0;
        AtomicLong sumTake = new AtomicLong();
        while (index < 1200) {
            int parallel = (int) (Math.random() * maxParallel) + 1;

            for (int i = 0; i < parallel; i++) {
                executor.submit(new Task(index++, sumTake));
                qpsMeter.increase();
            }
            int sleep;
            if (index < 500) {
                sleep = 150;
            } else if (index > 500 && index < 800) {
                sleep = 80;
            } else if (index > 800 && index < 1000) {
                sleep = 25;
            } else {
                sleep = 800;
            }

            try {
                System.out.println(index + ", ac: " + executor.getActiveCount() + ", ps: " + executor.getPoolSize()
                        + ", avg: " + (sumTake.get() / parallel) + ", capacity: " + executor.getCurrentQueueCapacity()
                        + ", core: " + executor.getCorePoolSize() + ", qps: " + qpsMeter.currentQps());
                sleep += (int) (10 * Math.random());
                Thread.sleep(sleep);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        System.out.println("sumTake=" + sumTake.get());
    }

    class Task implements Callable<String> {

        int index;

        long timestamp;

        AtomicLong sum;

        Task(int index, AtomicLong sum) {
            this.index = index;
            this.timestamp = System.currentTimeMillis();
            this.sum = sum;
        }

        @Override
        public String call() {
            try {
                long diff = System.currentTimeMillis() - timestamp;
                if (diff > 10) {
//                    System.out.println("index: " + index + ", diff: " + diff);
                }
                sum.addAndGet(diff);
                Thread.sleep(200);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            return null;
        }
    }
}
