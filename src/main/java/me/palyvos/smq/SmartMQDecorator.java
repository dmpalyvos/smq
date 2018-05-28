package me.palyvos.smq;

import java.util.Collection;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class SmartMQDecorator<T> implements BlockingQueue<T> {

  private final BlockingQueue<T> decorated;
  private final SmartMQWriterBase flowControl;
  private final int queueIndex;

  public SmartMQDecorator(BlockingQueue<T> decorated, int queueIndex, SmartMQWriterBase flowControl) {
    this.decorated = decorated;
    this.flowControl = flowControl;
    this.queueIndex = queueIndex;
  }

  @Override
  public boolean add(T t) {
    try {
      flowControl.write(queueIndex, t);
    } catch (InterruptedException e) {
      //FIXME
      e.printStackTrace();
    }
    return true;
  }

  @Override
  public boolean offer(T t) {
    try {
      flowControl.write(queueIndex, t);
    } catch (InterruptedException e) {
      //FIXME
      e.printStackTrace();
    }
    return true;
  }

  @Override
  public void put(T t) throws InterruptedException {
    add(t);
  }

  @Override
  public boolean offer(T t, long timeout, TimeUnit unit) throws InterruptedException {
    return offer(t);
  }

  @Override
  public T take() throws InterruptedException {
    return poll();
  }

  @Override
  public T poll(long timeout, TimeUnit unit) throws InterruptedException {
    return poll();
  }

  @Override
  public int remainingCapacity() {
    //FIXME
    return 1000;
  }

  @Override
  public T remove() {
    T value = decorated.remove();
    flowControl.notifyRead(queueIndex);
    return value;
  }

  @Override
  public T poll() {
    T value = decorated.poll();
    flowControl.notifyRead(queueIndex);
    return value;
  }

  @Override
  public T element() {
    return decorated.element();
  }

  @Override
  public T peek() {
    return decorated.peek();
  }

  @Override
  public int size() {
    return decorated.size();
  }

  @Override
  public boolean isEmpty() {
    return decorated.isEmpty();
  }

  @Override
  public boolean contains(Object o) {
    return decorated.contains(o);
  }

  @Override
  public int drainTo(Collection<? super T> c) {
    return 0;
  }

  @Override
  public int drainTo(Collection<? super T> c, int maxElements) {
    return 0;
  }

  @Override
  public Iterator<T> iterator() {
    return decorated.iterator();
  }

  @Override
  public Object[] toArray() {
    return decorated.toArray();
  }

  @Override
  public <T1> T1[] toArray(T1[] a) {
    return decorated.toArray(a);
  }

  @Override
  public boolean remove(Object o) {
    return decorated.remove(o);
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    return decorated.containsAll(c);
  }

  @Override
  public boolean addAll(Collection<? extends T> c) {
    return decorated.addAll(c);
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    return decorated.removeAll(c);
  }

  @Override
  public boolean removeIf(Predicate<? super T> filter) {
    return decorated.removeIf(filter);
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    return decorated.retainAll(c);
  }

  @Override
  public void clear() {
    decorated.clear();
  }

  @Override
  public boolean equals(Object o) {
    return decorated.equals(o);
  }

  @Override
  public int hashCode() {
    return decorated.hashCode();
  }

  @Override
  public Spliterator<T> spliterator() {
    return decorated.spliterator();
  }

  @Override
  public Stream<T> stream() {
    return decorated.stream();
  }

  @Override
  public Stream<T> parallelStream() {
    return decorated.parallelStream();
  }

  @Override
  public void forEach(Consumer<? super T> action) {
    decorated.forEach(action);
  }
}
