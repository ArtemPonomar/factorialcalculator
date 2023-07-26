import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;

public class FactorialCalculator extends Thread {
    private final ThreadPoolExecutor executorService;
    private final BlockingQueue<Integer> inputQueue;
    private final BlockingQueue<String> outputQueue;
    private final RateLimiter rateLimiter;

    public FactorialCalculator(ThreadPoolExecutor executorService, BlockingQueue<Integer> inputQueue, BlockingQueue<String> outputQueue, RateLimiter rateLimiter) {
        this.executorService = executorService;
        this.inputQueue = inputQueue;
        this.outputQueue = outputQueue;
        this.rateLimiter = rateLimiter;
    }

    @Override
    public void run() {
        while (true) {
            try {
                int currentNumber = inputQueue.take();
                int bucketSize = calculateBucketSize(currentNumber);
                List<Callable<BigInteger>> tasks = splitTasks(currentNumber, bucketSize);
                List<Future<BigInteger>> futures = invokeTasksWithRateLimit(tasks);
                BigInteger calculationResult = calculateResult(futures);
                outputQueue.add(currentNumber + " = " + calculationResult);
                System.out.println(currentNumber + " = " + calculationResult); //todo remove debugging line
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }

    private int calculateBucketSize(int currentNumber) {
        int bucketSize = currentNumber / executorService.getCorePoolSize();
        if (bucketSize < 1) {
            bucketSize = 1;
        }
        return bucketSize;
    }

    private List<Callable<BigInteger>> splitTasks(int currentNumber, int bucketSize) {
        List<Callable<BigInteger>> tasks = new ArrayList<>();
        while (currentNumber >= 0) {
            int to = currentNumber;
            int from = currentNumber - (bucketSize - 1);
            if (from < 0) from = 0;
            currentNumber -= bucketSize;
            tasks.add(new Worker(from, to));
        }
        return tasks;
    }

    private List<Future<BigInteger>> invokeTasksWithRateLimit(List<Callable<BigInteger>> tasks) {
        List<Future<BigInteger>> taskResults = new ArrayList<>();
        tasks.forEach(t -> {
            rateLimiter.tryAcquire();
            taskResults.add(executorService.submit(t));
        });
        return taskResults;
    }

    private BigInteger calculateResult(List<Future<BigInteger>> futures) {
        BigInteger result = BigInteger.ONE;
        for (Future<BigInteger> future : futures) {
            try {
                result = result.multiply(future.get());
            } catch (InterruptedException | ExecutionException e) {
                throw new RuntimeException(e);
            }
        }
        return result;
    }

    private record Worker(int from, int to) implements Callable<BigInteger> {

        @Override
        public BigInteger call() {
            return calculateIntegral(from, to);
        }

        private BigInteger calculateIntegral(Integer from, Integer to) {
            if (Objects.equals(from, to)) {
                if (from == 0) return BigInteger.ONE;
                return BigInteger.valueOf(from);
            }
            return calculateIntegral(from, to - 1).multiply(BigInteger.valueOf(to));
        }
    }
}
