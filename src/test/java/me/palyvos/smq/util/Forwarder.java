package me.palyvos.smq.util;

import java.util.Queue;

public class Forwarder<T> implements Runnable {
  private final Queue<T> input;
  private final Queue<T> output;
  private final long sleep;
  private final String name;

  public Forwarder(Queue<T> input, Queue<T> output, long sleep, String name) {
    this.input = input;
    this.output = output;
    this.sleep = sleep;
    this.name = name;
  }

  @Override
  public void run() {
    while (true) {
      TestUtil.sleep(sleep);
      T value = input.poll();
      if (value != null) {
//        System.out.format("%s forwarding %s%n", name, value);
        output.offer(value);
      }
    }
  }
}
