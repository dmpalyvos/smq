package me.palyvos.smq;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class SmartMQControllerWithBackoff<T> extends SmartMQController<T> {

  private final List<ExponentialBackoff> backoffs = new ArrayList<>();
  private final long initialSleepMs;
  private final int maxShift;

  public SmartMQControllerWithBackoff(long initialSleepMs, int maxShift, BlockingQueue<T>... queues) {
    super(queues);
    this.initialSleepMs = initialSleepMs;
    this.maxShift = maxShift;
    initBackoffs();
  }

  private void initBackoffs() {
    for (int i = 0; i < queues.size(); i++) {
      backoffs.add(new ExponentialBackoff(initialSleepMs, maxShift));
    }
  }

  @Override
  protected void waitWrite(int queueIndex) {
    super.waitWrite(queueIndex);
    backoffs.get(queueIndex).backoff();
  }

  @Override
  protected void notifyRead(int queueIndex) {
    super.notifyRead(queueIndex);
    backoffs.get(queueIndex).relax();
  }

  @Override
  protected void waitRead(int queueIndex) {
    backoffs.get(queueIndex).backoff();
  }

  @Override
  protected void notifyWrite(int queueIndex) {
    backoffs.get(queueIndex).relax();
  }
}
