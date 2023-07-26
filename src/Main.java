import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadPoolExecutor;

public class Main {

    public static final String INPUT_FILE_NAME = "input.txt";
    public static final String OUTPUT_FILE_NAME = "output.txt";
    public static final int PERMITS_PER_SECOND = 100;

    public static void main(String[] args) {
        BlockingQueue<Integer> inputQueue = new LinkedBlockingDeque<>();
        BlockingQueue<String> outputQueue = new LinkedBlockingDeque<>();
        int threadCount = getThreadCountFromConsole();
        FactorialCalculator factorialCalculator = new FactorialCalculator(
                (ThreadPoolExecutor) Executors.newFixedThreadPool(threadCount),
                inputQueue,
                outputQueue,
                new RateLimiter(PERMITS_PER_SECOND));
        try {
            new InputReader(inputQueue, new BufferedReader(new FileReader(INPUT_FILE_NAME))).start();
            factorialCalculator.start();
            new OutputWriter(outputQueue, new BufferedWriter(new FileWriter(OUTPUT_FILE_NAME))).start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    private static int getThreadCountFromConsole() {
        Scanner scanner = new Scanner(System.in);
        int threadCount;
        do {
            System.out.print("Enter a thread count: ");
            while (!scanner.hasNextInt()) {
                System.out.print("Invalid input. Please enter a positive integer: ");
                scanner.next();
            }
            threadCount = scanner.nextInt();
        } while (threadCount <= 0);
        return threadCount;
    }
}