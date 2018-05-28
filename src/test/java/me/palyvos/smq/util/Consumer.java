package me.palyvos.smq.util;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;

public class Consumer<T> implements Runnable {

  private final List<BlockingQueue<T>> inputs;
  private final Queue<T> output;
  private final long sleep;
  private final String name;

  public Consumer(List<BlockingQueue<T>> inputs, Queue<T> output, long sleep, String name) {
    this.inputs = inputs;
    this.output = output;
    this.sleep = sleep;
    this.name = name;
  }

  @Override
  public void run() {
    while (true) {
      TestUtil.sleep(sleep);
      for (BlockingQueue<T> q : inputs) {
        T value = null;
        try {
          value = q.take();
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
        if (value != null) {
         output.offer(value);
       }
      }
    }
  }
}
