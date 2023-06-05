package self.robin.examples.utils.thread2;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.task.AsyncListenableTaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.util.concurrent.ListenableFuture;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * ...
 *
 * @author Robin-Li
 * @since: 2022-09-29 17:04
 */
public class DynamicThreadPoolExecutor extends ThreadPoolTaskExecutor
    implements AsyncListenableTaskExecutor {

    private static final Logger log = LoggerFactory.getLogger(DynamicThreadPoolExecutor.class);

    private LinkedBlockingQueue2 blockingQueue;

    private int initialCoreSize;

    private int initialQueueSize;

    private double hungerFaction = 0.2;

    /**
     * 由于机器性能不稳定，判断缩容时，为了减少误判的情况
     * 如果连续n次发现资源可以缩容，才缩小核心线程数,
     * 这只是个优化，所以对此变量的修改不需要保证线程安全
     */
    private MultipleConfirmator confirmator = new MultipleConfirmator(2);

    /**
     * 扩容的灵敏度，1秒以内，走的是线程池原始逻辑；只有超过1s才触发主动扩core size
     * 扩容 checker
     */
    private Checker expendChecker = new Checker(1000);

    /**
     * 扩容的灵敏度，10秒以内，走的是线程池原始逻辑；只有超过1s才触发主动扩core size
     * 缩容 checker
     */
    private Checker shrinkChecker = new Checker(10_000);

    /**
     * 期待队列中任务的最大排队时间
     * <p>
     * 单位：毫秒
     */
    private long expectMaxWaitTime = 30;

    public void setHungerFaction(int faction) {
        if (faction < 0) {
            throw new IllegalArgumentException("faction: " + faction);
        }
        this.hungerFaction = faction;
    }

    @Override
    public void initialize() {
        this.initialCoreSize = this.getCorePoolSize();
        super.initialize();
        this.initialQueueSize = this.blockingQueue.getCapacity();
    }

    @Override
    protected BlockingQueue<Runnable> createQueue(int queueCapacity) {
        if (queueCapacity <= 0) {
            queueCapacity = this.getCorePoolSize();
        }
        this.blockingQueue = new LinkedBlockingQueue2<>(queueCapacity);
        return blockingQueue;
    }

    @Override
    public ListenableFuture<?> submitListenable(Runnable task) {
        autoAdjustResourceSettings();
        return super.submitListenable(task);
    }

    @Override
    public <T> ListenableFuture<T> submitListenable(Callable<T> task) {
        autoAdjustResourceSettings();
        return super.submitListenable(task);
    }

    @Override
    public Future<?> submit(Runnable task) {
        autoAdjustResourceSettings();
        return super.submit(task);
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        autoAdjustResourceSettings();
        return super.submit(task);
    }

    @Override
    public void execute(Runnable task) {
        autoAdjustResourceSettings();
        super.execute(task);
    }

    private void autoAdjustResourceSettings() {
        // 扩容
        boolean expand = expendChecker.check() && expendInternal();
        // 缩容
        if (!expand && shrinkChecker.check()) {
            shrinkInternal();
            // 缩容后一段时间内不进行扩容check
            // todo why expendChecker.check();
        } else {
            confirmator.reset();
        }
    }

    private boolean expendInternal() {
        int poolSize = this.getPoolSize();
        // 队列排队时间超过期待阈值，且工作线程不小于核心线程
        int maxPoolSize = this.getMaxPoolSize();
        log.info("maxWaitTime= " + this.blockingQueue.getMaxWaitTime());
        if (this.blockingQueue.getMaxWaitTime() > expectMaxWaitTime
            && this.getCorePoolSize() <= poolSize && poolSize <= maxPoolSize) {
            int coreSize = Math.min((int) Math.ceil(poolSize * (1 + hungerFaction)), maxPoolSize);
            this.setCorePoolSize(coreSize);
            log.info("Adjust: core-up-to " + coreSize);
            // match queue size
            matchQueueSize(poolSize);
            return true;
        }
        return false;
    }

    private void matchQueueSize(int poolSize) {
        // poolSize = this.getPoolSize();
        int capacity = this.blockingQueue.getCapacity();
        // 觉得queue capacity应该略大于worker数
        if ((double) (poolSize - capacity) > capacity * hungerFaction) {
            int queueSize = poolSize + (int) (poolSize * hungerFaction);
            this.blockingQueue.adjustQueueSize(queueSize);
            log.info("Adjust: queue-match-to " + queueSize);
        }
    }

    private boolean shrinkInternal() {
        // worker 数达到给定的coreSize后, 恢复初始值
        int coreSize = this.getCorePoolSize();
        int activeCount = this.getActiveCount();
        // worker数大于核心线程数，且活动线程不到一半
        boolean shouldShrink = (activeCount < coreSize / 2 && coreSize > initialCoreSize);
        // 多次确认是否需要缩容
        if (confirmator.confirm(shouldShrink)) {
            int aimCoreSize = Math.max((int) (coreSize * (1 - hungerFaction)), initialCoreSize);
            this.setCorePoolSize(aimCoreSize);
            log.info("Adjust: core-down-to " + aimCoreSize);
            // match queue size
            matchQueueSize(aimCoreSize);
            return true;
        }
        return false;
    }

    public int getCurrentQueueCapacity() {
        return this.blockingQueue.getCapacity();
    }

    class Checker {

        private long timestamp;

        private final long internal;

        public Checker(long internal) {
            this.internal = internal;
        }

        private final boolean check() {
            long curTime = System.currentTimeMillis();
            if (curTime - timestamp < internal) {
                return false;
            }
            this.timestamp = curTime;
            return true;
        }
    }

    class MultipleConfirmator {

        private final int num;

        private AtomicInteger curNum = new AtomicInteger();

        public MultipleConfirmator(int num) {
            if (num <= 0) {
                throw new IllegalArgumentException();
            }
            this.num = num;
        }

        /**
         * 连续确认指定次数后，返回true
         *
         * @param confirm 本次结果
         */
        private final boolean confirm(boolean confirm) {
            if (!confirm) {
                curNum.set(0);
                return false;
            }
            if (curNum.incrementAndGet() >= num) {
                curNum.set(0);
                return true;
            }
            return false;
        }

        public void reset() {
            this.curNum.set(0);
        }
    }

    public void setExpectMaxWaitTime(long expectMaxWaitTime) {
        this.expectMaxWaitTime = expectMaxWaitTime;
    }
}
