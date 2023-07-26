import java.io.BufferedWriter;
import java.io.IOException;
import java.util.concurrent.BlockingQueue;

public class OutputWriter extends Thread{
    private BlockingQueue<String> outputQueue;
    private BufferedWriter outputWriter;

    public OutputWriter(BlockingQueue<String> outputQueue, BufferedWriter outputWriter) {
        this.outputQueue = outputQueue;
        this.outputWriter = outputWriter;
    }

    @Override
    public void run() {
        String line;
        while (true) {
            try {
                line = outputQueue.poll();
                if (line == null) continue;
                outputWriter.write(line);
                outputWriter.newLine();
                outputWriter.flush();
                System.out.println("line added to output file"); //todo remove debugging line
            } catch (IOException | NumberFormatException e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
