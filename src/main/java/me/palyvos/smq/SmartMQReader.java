package me.palyvos.smq;

public interface SmartMQReader {

  <T> T poll(int queueIndex);

  void notifyWrite(int queueIndex);

  void waitRead(int queueIndex);
}
