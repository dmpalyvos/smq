package io.palyvos.smq;

/**
 * Entity responsible for handling the writes to a SmartMultiQueue.
 */
public interface SmartMQWriter {

  /**
   * Write a value to the queue with the given index. The write will always succeed, however if all
   * the queues that belong to this writer are full when this write occurs, the call will block.
   *
   * @param queueIndex The unique index of the queue that will receive the write.
   * @param value The value to be written.
   * @throws InterruptedException if the blocking call is interrupted
   */
  <T> void put(int queueIndex, T value) throws InterruptedException;

  /**
   * Notify this writer that a read happened to one of its queues.
   *
   * @param queueIndex The index of the queue where the read happened
   */
  void notifyRead(int queueIndex);

  /**
   * Notify the writer that one if its queues is full. If all the other queues belonging to this
   * writer are also full, this call will block.
   *
   * @param queueIndex The index of the queue that became full.
   * @throws InterruptedException If the blocking call is interrupted
   */
  void waitWrite(int queueIndex) throws InterruptedException;
}
