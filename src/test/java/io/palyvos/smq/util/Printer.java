package io.palyvos.smq.util;

import java.util.List;
import java.util.Queue;

public class Printer<T> implements Runnable {

  private final List<Queue<T>> queues;

  public Printer(List<Queue<T>> queues) {
    this.queues = queues;
  }


  @Override
  public void run() {
    while (true) {
      TestUtil.sleep(5000);
      for (int i = 0; i < queues.size(); i++) {
        System.out.format("Q%d: %d%n", i, queues.get(i).size());
      }
    }
  }
}
