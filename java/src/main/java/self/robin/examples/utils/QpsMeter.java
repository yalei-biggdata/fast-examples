package self.robin.examples.utils;

import java.util.concurrent.atomic.AtomicLong;

/**
 * ...
 *
 * @author Robin-Li
 * @since: 2022-10-08 14:11
 */
public class QpsMeter {

    private AtomicLong count = new AtomicLong();

    private long timestamp;

    public void increase() {
        this.count.incrementAndGet();
        long now = System.currentTimeMillis();
        if (now - timestamp > 1000) {
            this.count.set(0);
            this.timestamp = now;
        }
    }

    public long currentQps() {
        double diff = (System.currentTimeMillis() - timestamp) * 1.0 / 1000;
        return (long) (this.count.incrementAndGet() / (diff == 0 ? 1 : diff));
    }
}
