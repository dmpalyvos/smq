package io.palyvos.smq;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicIntegerArray;
import io.palyvos.smq.util.Backoff;
import io.palyvos.smq.util.MultiSemaphore;
import io.palyvos.smq.util.NoopBackoff;

public final class SmartMQWriterImpl implements SmartMQWriter, SmartMQController {

  private static final int LOCKED = 1;
  private static final int FREE = 0;

  private List<BlockingQueue<Object>> queues = new ArrayList<>();
  private List<Queue<Object>> buffers = new ArrayList<>();
  private List<Backoff> backoffs = new ArrayList<>();
  private MultiSemaphore writeSemaphore;
  private AtomicIntegerArray bufferLocks;

  @Override
  public synchronized <T> int register(BlockingQueue<T> queue, Backoff backoff) {
    int index = queues.size();
    queues.add((BlockingQueue<Object>) queue);
    backoffs.add(backoff);
    buffers.add(new ConcurrentLinkedQueue<>());
    return index;
  }

  @Override
  public synchronized <T> int register(BlockingQueue<T> queue) {
    return register(queue, NoopBackoff.INSTANCE);
  }

  @Override
  public void init() {
    if (queues == null || queues.size() == 0) {
      throw new IllegalStateException("queues");
    }
    this.queues = Collections.unmodifiableList(queues);
    this.buffers = Collections.unmodifiableList(buffers);
    this.bufferLocks = new AtomicIntegerArray(queues.size());
    this.writeSemaphore = new MultiSemaphore(queues.size());
  }


  @Override
  public <T> void put(int queueIndex, T value) throws InterruptedException {
    if (value == null) {
      throw new IllegalArgumentException("value");
    }
    Queue<Object> buffer = buffers.get(queueIndex);
    buffer.add(value);
    if (!fullCopyInputBuffer(queueIndex)) {
      waitWrite(queueIndex);
    }
  }

  @Override
  public void notifyRead(int queueIndex) {
    //FIXME: Revisit this, is it correct?
    if (fullCopyInputBuffer(queueIndex)) {
      writeSemaphore.release(queueIndex);
    }
    backoffs.get(queueIndex).relax();
  }

  @Override
  public void waitWrite(int queueIndex) throws InterruptedException {
    writeSemaphore.acquire(queueIndex);
    backoffs.get(queueIndex).backoff();
  }

  private boolean fullCopyInputBuffer(int queueIndex) {
    Queue<Object> buffer = buffers.get(queueIndex);
    if (buffer.isEmpty() || !bufferLocks.compareAndSet(queueIndex, FREE, LOCKED)) {
      // If buffer already locked, do nothing because somebody
      // is already copying it anyways
      return true;
    }
    // --- ENTER CRITICAL SECTION ---
    try {
      Queue<Object> destination = queues.get(queueIndex);
      while (!buffer.isEmpty()) {
        Object value = buffer.peek();
        if (!destination.offer(value)) {
          return false;
        }
        buffer.remove();
      }
      return true;
    } finally {
      // --- LEAVE CRITICAL SECTION ---
      bufferLocks.set(queueIndex, FREE);
    }
  }
}
