package io.palyvos.smq;

public interface SmartMQReader {

  <T> T take(int queueIndex) throws InterruptedException;

  void notifyWrite(int queueIndex);

  void waitRead(int queueIndex) throws InterruptedException;
}
