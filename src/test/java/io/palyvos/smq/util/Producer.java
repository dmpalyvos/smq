package io.palyvos.smq.util;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;

public class Producer implements Runnable {

  private final List<Queue<String>> queues;
  private final long sleep;
  private final String name;
  private final int repetitions;

  public Producer(List<Queue<String>> queues, long sleep, String name, int repetitions) {
    this.queues = queues;
    this.sleep = sleep;
    this.name = name;
    this.repetitions = repetitions;
  }

  @Override
  public void run() {
    for (int i = 0; i < repetitions; i++) {
      TestUtil.sleep(sleep);
      for (Queue<String> q : queues) {
        doPut(String.valueOf(i), q);
        doPut(String.valueOf(i), q);
//        System.out.format("%s offered %d%n", name, i);
      }
    }
    for (Queue<String> q : queues) {
      doPut("STOP", q);
    }
  }

  private void doPut(String value, Queue<String> q) {
    if (q instanceof BlockingQueue) {
      try {
        ((BlockingQueue<String>) q).put(value);
      } catch (InterruptedException e) {
        e.printStackTrace();
      }
    }
    else {
      q.offer(value);
    }
  }
}
