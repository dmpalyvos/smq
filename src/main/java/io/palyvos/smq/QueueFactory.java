package io.palyvos.smq;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import io.palyvos.smq.util.Backoff;
import io.palyvos.smq.util.NoopBackoff;

/**
 * Factory to enable easy generation of SmartMultiQueue Objects with various parameters.
 *
 * @author palivosd
 */
public enum QueueFactory {
  INSTANCE;

  public <T> List<Queue<T>> newArraySmartMQs(int number, int capacity, SmartMQReaderImpl reader,
      Backoff readerBackoff, SmartMQWriterImpl writer, Backoff writerBackoff) {
    if (number <= 0) {
      throw new IllegalArgumentException("number");
    }
    List<Queue<T>> queues = new ArrayList<>();
    for (int i = 0; i < number; i++) {
      BlockingQueue<T> q = new ArrayBlockingQueue<>(capacity);
      SmartMQueueDecorator.Builder<T> builder = new SmartMQueueDecorator.Builder<>(q);
      if (reader != null) {
        int readerIndex = reader.register(q, readerBackoff.newInstance());
        builder.reader(reader, readerIndex);
      }
      if (writer != null) {
        int writerIndex = writer.register(q, writerBackoff.newInstance());
        builder.writer(writer, writerIndex);
      }
      queues.add(builder.build());
    }
    return queues;
  }

  public <T> List<Queue<T>> newArraySmartMQs(int number, int capacity, Backoff readerBackoff,
      Backoff writerBackoff) {
    SmartMQWriterImpl writer = new SmartMQWriterImpl();
    SmartMQReaderImpl reader = new SmartMQReaderImpl();
    List<Queue<T>> queues = newArraySmartMQs(number, capacity, reader, readerBackoff, writer,
        writerBackoff);
    writer.init();
    reader.init();
    return queues;
  }

  public <T> List<Queue<T>> newArraySmartMQs(int number, int capacity) {
    return newArraySmartMQs(number, capacity, NoopBackoff.INSTANCE, NoopBackoff.INSTANCE);
  }

  public <T> List<Queue<T>> newArraySmartMQsWriterOnly(int number, int capacity,
      SmartMQWriterImpl writer, Backoff writerBackoff) {
    return newArraySmartMQs(number, capacity, null, null, writer, writerBackoff);
  }

  public <T> List<Queue<T>> newArraySmartMQsWriterOnly(int number, int capacity,
      Backoff writerBackoff) {
    SmartMQWriterImpl writer = new SmartMQWriterImpl();
    List<Queue<T>> queues = newArraySmartMQsWriterOnly(number, capacity, writer, writerBackoff);
    writer.init();
    return queues;
  }

  public <T> List<Queue<T>> newArraySmartMQsWriterOnly(int number, int capacity) {
    return newArraySmartMQsWriterOnly(number, capacity, NoopBackoff.INSTANCE);
  }

  public <T> List<Queue<T>> newArraySmartMQsReaderOnly(int number, int capacity,
      SmartMQReaderImpl reader, Backoff readerBackoff) {
    return newArraySmartMQs(number, capacity, reader, readerBackoff, null, null);
  }

  public <T> List<Queue<T>> newArraySmartMQsReaderOnly(int number, int capacity,
      Backoff readerBackoff) {
    SmartMQReaderImpl reader = new SmartMQReaderImpl();
    List<Queue<T>> queues = newArraySmartMQs(number, capacity, reader, readerBackoff, null, null);
    reader.init();
    return queues;
  }

  public <T> List<Queue<T>> newArraySmartMQsReaderOnly(int number, int capacity) {
    return newArraySmartMQsReaderOnly(number, capacity, NoopBackoff.INSTANCE);
  }

  public <T> List<BlockingQueue<T>> newArrayQueues(int number, int capacity) {
    List<BlockingQueue<T>> queues = new ArrayList<>();
    for (int i = 0; i < number; i++) {
      queues.add(new ArrayBlockingQueue<>(capacity));
    }
    return queues;
  }

}
