package self.robin.examples.histogram;


import org.HdrHistogram.Histogram;

import java.util.UUID;

/**
 * ...
 *
 * @author Robin-Li
 * @since: 2023-05-01 11:47
 */
public class HdrHistogram {

    public static void main(String[] args) {
        //TimeUnit.MINUTES.toNanos(1)
        // param2: 值精度，保留几位小数
        Histogram histogram = new Histogram(500000000, 3);

//        // record value
        long st = System.nanoTime();
        for (int i = 0; i < 10; i++) {
            UUID.randomUUID();
            System.out.println(System.nanoTime() - st);
            histogram.recordValue(System.nanoTime() - st);
            st = System.nanoTime();
        }
//        // output
        //
        histogram.outputPercentileDistribution(System.out, 3, 100000.0);
    }
}
