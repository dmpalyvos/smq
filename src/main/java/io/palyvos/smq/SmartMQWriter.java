package io.palyvos.smq;

public interface SmartMQWriter {

  <T> void add(int queueIndex, T value) throws InterruptedException;

  void notifyRead(int queueIndex);

  void waitWrite(int queueIndex) throws InterruptedException;
}
