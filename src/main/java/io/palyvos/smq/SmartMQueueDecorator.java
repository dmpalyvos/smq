package io.palyvos.smq;

import io.palyvos.smq.util.Backoff;
import java.util.Collection;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.concurrent.BlockingQueue;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * The decorator that converts a regular {@link BlockingQueue} to a SmartMultiQueue. To decorate an
 * object, use the provided {@link Builder}, which allows the decorated to use only a {@link
 * SmartMQReader}, a {@link SmartMQWriter} or both, depending on your needs.
 *
 * @param <T> The type of the queue contents
 * @author palivosd
 */
public class SmartMQueueDecorator<T> extends BlockingQueueToQueueAdapter<T> {

  private final int readerIndex;
  private final SmartMQReader reader;
  private final int writerIndex;
  private final SmartMQWriter writer;

  protected SmartMQueueDecorator(Builder<T> builder) {
    super(builder.decorated);
    this.reader = builder.reader;
    this.readerIndex = builder.readerIndex;
    this.writer = builder.writer;
    this.writerIndex = builder.writerIndex;
  }


  @Override
  public T poll() {
    try {
      T value = reader.take(readerIndex);
      if (value != null) {
        writer.notifyRead(writerIndex);
      }
      return value;
    } catch (InterruptedException e) {
      System.out.format("poll() interrupted: %s%n", e.getStackTrace()[2]);
      Thread.currentThread().interrupt();
      return null;
    }
  }

  @Override
  public boolean add(T value) {
    return offer(value);
  }

  @Override
  public boolean offer(T value) {
    try {
      writer.put(writerIndex, value);
      reader.notifyWrite(readerIndex);
      return true;
    } catch (InterruptedException e) {
      System.out.format("offer() interrupted: %s%n", e.getStackTrace()[2]);
      Thread.currentThread().interrupt();
      return false;
    }
  }

  @Override
  public boolean remove(Object o) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean addAll(Collection<? extends T> collection) {
    for (T t : collection) {
      offer(t);
    }
    return true;
  }

  @Override
  public T remove() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Iterator<T> iterator() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean removeIf(Predicate<? super T> filter) {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Spliterator<T> spliterator() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Stream<T> stream() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Stream<T> parallelStream() {
    throw new UnsupportedOperationException();
  }

  /**
   * Builder for {@link SmartMQueueDecorator}s. Allows to selectively enable {@link SmartMQReader},
   * {@link SmartMQWriter}, or both. If an optional parameter is not given before {@link #build()},
   * a default noop value will be used for it.
   *
   * @param <T> The type of the queue contents.
   */
  public static class Builder<T> {

    private final BlockingQueue<T> decorated;

    private int readerIndex;
    private SmartMQReader reader;
    private int writerIndex;
    private SmartMQWriter writer;

    /**
     * Construct.
     *
     * @param decorated The {@link BlockingQueue} that will be converted to a SmartMultiQueue.
     */
    public Builder(BlockingQueue<T> decorated) {
      this.decorated = decorated;
      // Default NOOP writer and reader
      this.reader = new SMQReaderNoop<>(decorated);
      this.writer = new SMQWriterNoop<>(decorated);
    }

    /**
     * Set the {@link SmartMQReader} that will be used for this queue.
     *
     * @param reader The reader that will be used.
     * @param index The index of the queue, as as returned by {@link SmartMQController#register(BlockingQueue,
     * Backoff)}.
     */
    public Builder reader(SmartMQReader reader, int index) {
      this.reader = reader;
      this.readerIndex = index;
      return this;
    }

    /**
     * Set the {@link SmartMQWriter} that will be used for this queue.
     *
     * @param writer The writer that will be used
     * @param index The index of the queue, as as returned by {@link SmartMQController#register(BlockingQueue,
     * Backoff)}.
     */
    public Builder writer(SmartMQWriter writer, int index) {
      this.writer = writer;
      this.writerIndex = index;
      return this;
    }

    /**
     * Generate the decorated queue with the parameters of the builder.
     *
     * @return A SmartMultiQueue object.
     */
    public SmartMQueueDecorator<T> build() {
      return new SmartMQueueDecorator<>(this);
    }

  }

  private static final class SMQWriterNoop<R> implements SmartMQWriter {

    private final BlockingQueue<R> decorated;

    public SMQWriterNoop(BlockingQueue<R> decorated) {
      this.decorated = decorated;
    }


    @Override
    public <T> void put(int queueIndex, T value) {
      decorated.offer((R) value);
    }

    @Override
    public void notifyRead(int queueIndex) {
    }

    @Override
    public void waitWrite(int queueIndex) {
    }

  }


  private static final class SMQReaderNoop<R> implements SmartMQReader {

    private final BlockingQueue<R> decorated;

    public SMQReaderNoop(BlockingQueue<R> decorated) {
      this.decorated = decorated;
    }

    @Override
    public <T> T take(int queueIndex) {
      return (T) decorated.poll();
    }

    @Override
    public void notifyWrite(int queueIndex) {
    }

    @Override
    public void waitRead(int queueIndex) {
    }
  }
}
