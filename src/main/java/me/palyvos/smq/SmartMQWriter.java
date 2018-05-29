package me.palyvos.smq;

public interface SmartMQWriter {

  <T> void offer(int queueIndex, T value);

  void notifyRead(int queueIndex);

  void waitWrite(int queueIndex);
}
