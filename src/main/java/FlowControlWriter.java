import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicIntegerArray;

public class FlowControlWriter<T> {

  private final int BLOCKED = 1;
  private final int FREE = 0;
  private final List<Queue<T>> queues;
  private final List<Queue<T>> buffers = new ArrayList<>();
  private final List<ExponentialBackoff> backoffs = new ArrayList<>();
  private final Semaphore semaphore;
  private final AtomicIntegerArray blocked;

  public FlowControlWriter(Queue<T>... queues) {
    //FIXME: Validate
    this.queues = Arrays.asList(queues);
    this.semaphore = new Semaphore(queues.length - 1);
    this.blocked = new AtomicIntegerArray(queues.length);
    initBuffers();
  }

  public Queue<T> getQueue(int index) {
    return new FlowControlWriterQueueDecorator<>(queues.get(index), index, this);
  }

  private void initBuffers() {
    for (int i = 0; i < queues.size(); i++) {
      buffers.add(new ConcurrentLinkedQueue<>());
      backoffs.add(new ExponentialBackoff(500, 15));
    }
  }

  public void write(int queueIndex, T value) throws InterruptedException {
    Queue<T> buffer = buffers.get(queueIndex);
    buffer.add(value);
    if (!fullCopyBuffer(queueIndex)) {
      waitWrite(queueIndex);
      // TODO: Maybe optional backoff here since the buffer must be unbounded
    }
  }

  public void writeAll(T value) throws InterruptedException {
    for (int i = 0; i < queues.size(); i++) {
      write(i, value);
    }
  }

  void notifyRead(int queueIndex) {
    // If blocked status for this queue set from true to false
    if (blocked.compareAndSet(queueIndex, BLOCKED, FREE)) {
      // Up the semaphore
      semaphore.release();
    }
    backoffs.get(queueIndex).relax();
  }

  private void waitWrite(int queueIndex) throws InterruptedException {
    // If blocked status for this queue set from false to true
    if (blocked.compareAndSet(queueIndex, FREE, BLOCKED)) {
      // Down the semaphore
      semaphore.acquire();
    }
    backoffs.get(queueIndex).backoff();
  }

  public List<Queue<T>> getBuffers() {
    return buffers;
  }

  private boolean fullCopyBuffer(int queueIndex) {
    // This shoud be made thread safe per index maybe?
    Queue<T> buffer = buffers.get(queueIndex);
    Queue<T> destination = queues.get(queueIndex);
    while (!buffer.isEmpty()) {
      T value = buffer.peek();
      if (!destination.offer(value)) {
        return false;
      }
      buffer.remove();
    }
    return true;
  }

}
