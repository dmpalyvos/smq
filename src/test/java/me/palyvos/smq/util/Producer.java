package me.palyvos.smq.util;

import java.util.List;
import java.util.concurrent.BlockingQueue;

public class Producer implements Runnable {

  private final List<BlockingQueue<String>> queues;
  private final long sleep;
  private final String name;
  private final int repetitions;

  public Producer(List<BlockingQueue<String>> queues, long sleep, String name, int repetitions) {
    this.queues = queues;
    this.sleep = sleep;
    this.name = name;
    this.repetitions = repetitions;
  }

  @Override
  public void run() {
    for (int i = 0; i < repetitions; i++) {
      TestUtil.sleep(sleep);
      for (BlockingQueue<String> q : queues) {
        try {
          q.put(String.valueOf(i));
          q.put(String.valueOf(i));
        } catch (InterruptedException e) {
          e.printStackTrace();
        }
//        System.out.format("%s offered %d%n", name, i);
      }
    }
  }
}
