package me.palyvos.smq;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicIntegerArray;

public class SmartMQController<T> {

  private static final int LOCKED = 1;
  private static final int FREE = 0;

  protected final List<BlockingQueue<T>> queues;
  protected final List<Queue<T>> buffers = new ArrayList<>();
  private final MultiSemaphore writeSemaphore;
  private final MultiSemaphore readSemaphore;
  private final AtomicIntegerArray bufferLocks;

  public static <T> SmartMQController<T> withArrayBlockingQueues(int number, int capacity) {
    BlockingQueue<T>[] queues = new BlockingQueue[number];
    for (int i = 0; i < number; i++) {
      queues[i] = new ArrayBlockingQueue<>(capacity);
    }
    return new SmartMQController<>(queues);
  }

  public SmartMQController(BlockingQueue<T>... queues) {
    if (queues == null || queues.length == 0) {
      throw new IllegalArgumentException("queues");
    }
    this.queues = Arrays.asList(queues);
    this.bufferLocks = new AtomicIntegerArray(queues.length);
    this.writeSemaphore = new MultiSemaphore(queues.length);
    this.readSemaphore = new MultiSemaphore(queues.length);
    initBuffers();
  }

  private void initBuffers() {
    for (int i = 0; i < queues.size(); i++) {
      buffers.add(new ConcurrentLinkedQueue<>());
    }
  }

  public Queue<T> getQueue(int index) {
    return new SmartMQDecorator(queues.get(index), index, this);
  }


  public void offer(int queueIndex, T value) {
    if (value == null) {
      throw new IllegalArgumentException("value");
    }
    Queue<T> buffer = buffers.get(queueIndex);
    buffer.add(value);
    boolean inputBufferFullyCopied = fullCopyInputBuffer(queueIndex);
    notifyWrite(queueIndex);
    if (!inputBufferFullyCopied) {
      waitWrite(queueIndex);
    }
  }

  protected void notifyWrite(int queueIndex) {
    readSemaphore.release(queueIndex);
  }

  public T poll(int queueIndex) {
    fullCopyInputBuffer(queueIndex);
    T value = queues.get(queueIndex).poll();
    if (value == null) {
      waitRead(queueIndex);
      return null;
    }
    notifyRead(queueIndex);
    return value;
  }

  protected void waitRead(int queueIndex) {
    try {
      readSemaphore.acquire(queueIndex);
    } catch (InterruptedException e) {
      e.printStackTrace();
      Thread.currentThread().interrupt();
    }
  }

  public void writeAll(T value) {
    for (int i = 0; i < queues.size(); i++) {
      offer(i, value);
    }
  }

  protected void notifyRead(int queueIndex) {
    writeSemaphore.release(queueIndex);
  }

  protected void waitWrite(int queueIndex) {
    try {
      writeSemaphore.acquire(queueIndex);
    } catch (InterruptedException e) {
      e.printStackTrace();
      Thread.currentThread().interrupt();
    }
  }

  public List<Queue<T>> getQueues() {
    List<Queue<T>> decorated = new ArrayList<>();
    for (int i = 0; i < queues.size(); i++) {
      decorated.add(getQueue(i));
    }
    return decorated;
  }

  private boolean fullCopyInputBuffer(int queueIndex) {
    Queue<T> buffer = buffers.get(queueIndex);
    if (buffer.isEmpty() || !bufferLocks.compareAndSet(queueIndex, FREE, LOCKED)) {
      // If buffer already locked, do nothing because somebody
      // is already copying it anyways
      return true;
    }
    // --- ENTER CRITICAL SECTION ---
    try {
      Queue<T> destination = queues.get(queueIndex);
      while (!buffer.isEmpty()) {
        T value = buffer.peek();
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
