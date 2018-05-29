package me.palyvos.smq;

import java.util.Collection;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.concurrent.BlockingQueue;
import java.util.function.Predicate;
import java.util.stream.Stream;

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
    T value = reader.poll(readerIndex);
    if (value != null) {
      writer.notifyRead(writerIndex);
    }
    return value;
  }

  @Override
  public boolean add(T value) {
    return offer(value);
  }

  @Override
  public boolean offer(T value) {
    writer.offer(writerIndex, value);
    reader.notifyWrite(readerIndex);
    return true;
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

  public static class Builder<T> {

    private final BlockingQueue<T> decorated;

    private int readerIndex;
    private SmartMQReader reader;
    private int writerIndex;
    private SmartMQWriter writer;

    public Builder(BlockingQueue<T> decorated) {
      this.decorated = decorated;
      this.reader = new SMQReaderNoop<>(decorated);
      this.writer = new SMQWriterNoop<>(decorated);
    }

    public Builder reader(SmartMQReaderImpl reader, int index) {
      this.reader = reader;
      this.readerIndex = index;
      return this;
    }

    public Builder writer(SmartMQWriter writer, int index) {
      this.writer = writer;
      this.writerIndex = index;
      return this;
    }

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
    public <T> void offer(int queueIndex, T value) {
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
    public <T> T poll(int queueIndex) {
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
