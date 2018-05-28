package me.palyvos.smq.util;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;

public class Consumer implements Runnable {

  private final List<Queue<String>> inputs;
  private final Queue<String> output;
  private final long sleep;
  private final String name;

  public Consumer(List<Queue<String>> inputs, Queue<String> output, long sleep, String name) {
    this.inputs = inputs;
    this.output = output;
    this.sleep = sleep;
    this.name = name;
  }

  @Override
  public void run() {
    while (true) {
      TestUtil.sleep(sleep);
      for (Queue<String> q : inputs) {
        String value = doTake(q);
        if (value != null) {
          if (value.equals("STOP")) {
            return;
          }
          output.offer(value);
        }
      }
    }
  }

  private String doTake(Queue<String> q) {
    if (q instanceof BlockingQueue) {
      try {
        return ((BlockingQueue<String>) q).take();
      } catch (InterruptedException e) {
        e.printStackTrace();
        return null;
      }
    } else {
      return q.poll();
    }
  }
}
