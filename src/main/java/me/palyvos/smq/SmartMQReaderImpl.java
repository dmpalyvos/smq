package me.palyvos.smq;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import me.palyvos.smq.util.Backoff;
import me.palyvos.smq.util.MultiSemaphore;
import me.palyvos.smq.util.NoopBackoff;

public final class SmartMQReaderImpl implements SmartMQReader, SmartMQController {

  private List<BlockingQueue<Object>> queues = new ArrayList<>();
  private List<Backoff> backoffs = new ArrayList<>();
  private MultiSemaphore readSemaphore;

  @Override
  public synchronized <T> int register(BlockingQueue<T> queue, Backoff backoff) {
    int index = queues.size();
    queues.add((BlockingQueue<Object>) queue);
    backoffs.add(backoff);
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
    this.readSemaphore = new MultiSemaphore(queues.size());
  }

  @Override
  public <T> T poll(int queueIndex) {
    T value = (T) queues.get(queueIndex).poll();
    if (value == null) {
      waitRead(queueIndex);
      return null;
    }
    return value;
  }

  @Override
  public void notifyWrite(int queueIndex) {
    readSemaphore.release(queueIndex);
    backoffs.get(queueIndex).relax();
  }

  @Override
  public void waitRead(int queueIndex) {
    try {
      readSemaphore.acquire(queueIndex);
      backoffs.get(queueIndex).backoff();
    } catch (InterruptedException e) {
      System.out.format("waitRead() interrupted: %s%n", e.getStackTrace()[2]);
      Thread.currentThread().interrupt();
    }
  }
}
