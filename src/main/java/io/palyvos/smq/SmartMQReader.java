package io.palyvos.smq;

/**
 * Entity responsible for reads in a SmartMultiQueue.
 */
public interface SmartMQReader {

  /**
   * Read a value to the queue with the given index. If the queue with the given index is empty,
   * null will be returned. However, if all the queues managed by this reader are empty, this call
   * will also block. Depending on the implementation, after unblocking the function might return an
   * actual value or null.
   *
   * @param queueIndex The unique index of the queue that will receive the write.
   * @throws InterruptedException if the blocking call is interrupted
   * @returns the value at the head of the queue
   */
  <T> T take(int queueIndex) throws InterruptedException;

  /**
   * Notify the reader that the queue with the given index received a new value (and thus a read
   * might proceed for it.
   *
   * @param queueIndex The index of the queue that the notification is about.
   */
  void notifyWrite(int queueIndex);

  /**
   * Notify the reader that the queue with the given index is empty. If all other queues are also
   * empty, this call will block.
   *
   * @param queueIndex The index of the queue that the notification is about.
   * @throws InterruptedException if the blocking call is interrupted
   */
  void waitRead(int queueIndex) throws InterruptedException;
}
