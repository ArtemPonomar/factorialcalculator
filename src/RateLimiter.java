import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class RateLimiter {
    private final Lock lock;
    private final Condition condition;
    private volatile long lastRequestTime;
    private final long intervalMs;

    public RateLimiter(int permitsPerSecond) {
        this.intervalMs = 1000 / permitsPerSecond;
        this.lastRequestTime = System.currentTimeMillis();
        this.lock = new ReentrantLock();
        this.condition = lock.newCondition();
    }

    public boolean tryAcquire() {
        lock.lock();
        try {
            long currentTime = System.currentTimeMillis();
            long elapsedTime = currentTime - lastRequestTime;

            if (elapsedTime >= intervalMs) {
                lastRequestTime = currentTime;
                return true;
            }

            long remainingTime = intervalMs - elapsedTime;
            condition.await(remainingTime, TimeUnit.MILLISECONDS);
            lastRequestTime = System.currentTimeMillis();
            return true;
        } catch (InterruptedException e) {
            return false;
        } finally {
            lock.unlock();
        }
    }
}
