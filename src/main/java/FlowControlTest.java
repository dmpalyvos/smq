import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

public class FlowControlTest {

  private static class Reader<T> implements Runnable {
    private final Queue<T> queue;
    private final long sleep;
    private final String name;

    public Reader(Queue<T> queue, long sleep, String name) {
      this.queue = queue;
      this.sleep = sleep;
      this.name = name;
    }

    @Override
    public void run() {
      while (true) {
        sleep(sleep);
        T value = queue.poll();
        if (value != null) {
//          System.out.format("READER %s: %s%n", name, value);
        }
      }
    }
  }

  public static class Reporter<T> implements Runnable {
    private final List<Queue<T>> queues;

    public Reporter(List<Queue<T>> queues) {
      this.queues = queues;
    }


    @Override
    public void run() {
      while (true) {
        sleep(5000);
        for (int i = 0; i < queues.size(); i++) {
          System.out.format("Q%d: %d%n", i, queues.get(i).size());
        }
      }
    }
  }

  public static void main(String[] args) throws InterruptedException {
    Queue<Integer> q1 = new ArrayBlockingQueue<>(1);
    Queue<Integer> q2 = new ArrayBlockingQueue<>(1);

    FlowControlWriter<Integer> flowControl = new FlowControlWriter<>(q1, q2);
    Thread r1 = new Thread(new Reader<>(flowControl.getQueue(0), 100, "1"));
    Thread r2 = new Thread(new Reader<>(flowControl.getQueue(1), 1000, "2"));
    Thread reporter = new Thread(new Reporter(flowControl.getBuffers()));
    r1.start();
    r2.start();
    reporter.start();
    int i = 0;
    while (true) {
      long start = System.currentTimeMillis();
      sleep(50);
      flowControl.writeAll(i);
//      System.out.format("-- PRODUCER ROUND %d | SPEED = %dms --%n", i, System.currentTimeMillis() - start);
      i++;
    }

  }

  private static void sleep(long millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
  }

}
