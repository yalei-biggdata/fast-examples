package thread;

import java.util.concurrent.locks.ReentrantLock;

/**
 * ...
 *
 * @author Robin-Li
 * @since: 2023-09-07 9:52
 */
public class MultiThread {

    public static void main(String[] args) throws Exception {
        Demo demo = new Demo();

        System.out.println("main start");
        new Thread(() -> {
            System.out.println("child start");
            try {
                Thread.sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            demo.setValue("hello");
            System.out.println("child done");
        }).start();

        Object o = demo.get();
        System.out.println("main done " + o);
    }

    static class Demo {

        private Object value;

        Object lock = new Object();

        boolean isDone = false;

        public synchronized Object get() throws Exception {
            if (!isDone) {
                lock.wait();
            }
            return value;
        }

        public void setValue(Object o) {
            this.value = o;
            lock.notify();
            this.isDone = true;
        }
    }
}
