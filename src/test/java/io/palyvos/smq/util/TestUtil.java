package io.palyvos.smq.util;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

public class TestUtil {

  public static void sleep(long millis) {
    try {
      Thread.sleep(millis);
    } catch (InterruptedException e) {
      Thread.currentThread().interrupt();
      System.out.println("Sleep interrupted");
    }
  }

  public static <T> BlockingQueue<T> newQueue(int capacity) {
    return new ArrayBlockingQueue<T>(capacity);
  }

  public static <T> BlockingQueue<T> newQueue() {
    return newQueue(1);
  }

}
