package me.palyvos.smq;

import java.util.Collection;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Spliterator;
import java.util.concurrent.BlockingQueue;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class SmartMQDecorator<T> implements Queue<T> {

  private final BlockingQueue<T> decorated;
  private final SmartMQController<T> controller;
  private final int index;

  public SmartMQDecorator(BlockingQueue<T> decorated, int index,
      SmartMQController<T> controller) {
    this.decorated = decorated;
    this.controller = controller;
    this.index = index;
  }

  @Override
  public boolean add(T t) {
    controller.offer(index, t);
    return true;
  }

  @Override
  public boolean offer(T t) {
    return add(t);
  }

  @Override
  public boolean remove(Object o) {
    boolean removed = decorated.remove(o);
    if (removed) {
      controller.notifyRead(index);
    }
    return removed;
  }

  @Override
  public boolean contains(Object o) {
    return decorated.contains(o);
  }

  @Override
  public T remove() {
    T value = controller.poll(index);
    if (value == null) {
      throw new NoSuchElementException();
    }
    return value;
  }

  @Override
  public T poll() {
    return controller.poll(index);
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
  public Iterator<T> iterator() {
    throw new UnsupportedOperationException();
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
  public boolean containsAll(Collection<?> c) {
    return decorated.containsAll(c);
  }

  @Override
  public boolean addAll(Collection<? extends T> c) {
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

  @Override
  public void forEach(Consumer<? super T> action) {
    decorated.forEach(action);
  }
}
