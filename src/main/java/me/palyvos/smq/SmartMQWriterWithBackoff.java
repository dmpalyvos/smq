package me.palyvos.smq;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;

public class SmartMQWriterWithBackoff<T> extends SmartMQWriterBase<T> {

  private final List<ExponentialBackoff> backoffs = new ArrayList<>();
  private final long initialSleepMs;
  private final int maxShift;

  public SmartMQWriterWithBackoff(long initialSleepMs, int maxShift, BlockingQueue<T>... queues) {
    super(queues);
    this.initialSleepMs = initialSleepMs;
    this.maxShift = maxShift;
  }

  private void initBackoffs() {
    for (int i = 0; i < queues.size(); i++) {
      backoffs.add(new ExponentialBackoff(500, 15));
    }
  }

  @Override
  protected void waitWrite(int queueIndex) throws InterruptedException {
    super.waitWrite(queueIndex);
    backoffs.get(queueIndex).backoff();
  }

  @Override
  protected void notifyRead(int queueIndex) {
    super.notifyRead(queueIndex);
    backoffs.get(queueIndex).relax();
  }
}
