package me.palyvos.smq;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicIntegerArray;

public class SmartMQWriterBase<T> {

  private final int BLOCKED = 1;
  private final int FREE = 0;
  protected final List<BlockingQueue<T>> queues;
  protected final List<Queue<T>> buffers = new ArrayList<>();

  private final Semaphore semaphore;
  private final AtomicIntegerArray blocked;
  private final AtomicIntegerArray bufferLocks;

  public SmartMQWriterBase(BlockingQueue<T>... queues) {
    if (queues == null || queues.length == 0) {
      throw new IllegalArgumentException("queues");
    }
    this.queues = Arrays.asList(queues);
    this.semaphore = new Semaphore(queues.length - 1);
    this.blocked = new AtomicIntegerArray(queues.length);
    this.bufferLocks = new AtomicIntegerArray(queues.length);
    initBuffers();
  }
  
  private void initBuffers() {
    for (int i = 0; i < queues.size(); i++) {
      buffers.add(new ConcurrentLinkedQueue<>()); //FIXME: ArrayList better?
    }
  }

  public BlockingQueue<T> getQueue(int index) {
    return new SmartMQDecorator(queues.get(index), index, this);
  }


  public void write(int queueIndex, T value) throws InterruptedException {
    Queue<T> buffer = buffers.get(queueIndex);
    buffer.add(value);
    if (!fullCopyBuffer(queueIndex)) {
      waitWrite(queueIndex);
    }
  }

  public void writeAll(T value) throws InterruptedException {
    for (int i = 0; i < queues.size(); i++) {
      write(i, value);
    }
  }

  protected void notifyRead(int queueIndex) {
    // If blocked status for this queue set from true to false
    if (blocked.compareAndSet(queueIndex, BLOCKED, FREE)) {
      // Up the semaphore
      semaphore.release();
    }
    fullCopyBuffer(queueIndex);
  }

  // This can be a template method so that when using scheduler it just changes a boolean
  protected void waitWrite(int queueIndex) throws InterruptedException {
    // If blocked status for this queue set from false to true
    if (blocked.compareAndSet(queueIndex, FREE, BLOCKED)) {
      // Down the semaphore
      semaphore.acquire();
    }
  }

  List<Queue<T>> getBuffers() {
    return buffers;
  }

  public List<BlockingQueue<T>> getQueues() {
    List<BlockingQueue<T>> decorated = new ArrayList<>();
    for (int i = 0; i < queues.size(); i++) {
      decorated.add(getQueue(i));
    }
    return decorated;
  }

  private boolean fullCopyBuffer(int queueIndex) {
    if (!bufferLocks.compareAndSet(queueIndex, FREE, BLOCKED)) {
      // If buffer already locked, do nothing because somebody
      // is already copying the buffer
      return true;
    }
    // --- ENTER CRITICAL SECTION ---
    try {
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
    } finally {
      // --- LEAVE CRITICAL SECTION ---
      bufferLocks.set(queueIndex, FREE);
    }
  }
}
