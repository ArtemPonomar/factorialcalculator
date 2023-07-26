import java.io.BufferedReader;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;

public class InputReader extends Thread {
    private BlockingQueue<Integer> inputQueue;
    private BufferedReader inputReader;

    public InputReader(BlockingQueue<Integer> inputQueue, BufferedReader inputReader) {
        this.inputQueue = inputQueue;
        this.inputReader = inputReader;
    }

    @Override
    public void run() {
        String line;
        while (true) {
            try {
                if ((line = inputReader.readLine()) == null) {
                    break;
                }
                inputQueue.add(Integer.valueOf(line));
            } catch (IOException | NumberFormatException e) {
                System.out.println("Skipped line wile reading, " + e.getMessage());
            }
        }
    }
}
