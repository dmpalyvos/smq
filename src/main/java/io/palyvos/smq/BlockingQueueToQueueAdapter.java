package io.palyvos.smq;

import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;
import java.util.Spliterator;
import java.util.concurrent.BlockingQueue;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class BlockingQueueToQueueAdapter<T> implements Queue<T> {

  private final BlockingQueue<T> apdatee;

  public BlockingQueueToQueueAdapter(BlockingQueue<T> apdatee) {
    this.apdatee = apdatee;
  }

  @Override
  public boolean add(T t) {
    return apdatee.add(t);
  }

  @Override
  public boolean offer(T t) {
    return apdatee.offer(t);
  }

  @Override
  public boolean remove(Object o) {
    return apdatee.remove(o);
  }

  @Override
  public boolean contains(Object o) {
    return apdatee.contains(o);
  }

  @Override
  public T remove() {
    return apdatee.remove();
  }

  @Override
  public T poll() {
    return apdatee.poll();
  }

  @Override
  public T element() {
    return apdatee.element();
  }

  @Override
  public T peek() {
    return apdatee.peek();
  }

  @Override
  public int size() {
    return apdatee.size();
  }

  @Override
  public boolean isEmpty() {
    return apdatee.isEmpty();
  }

  @Override
  public Iterator<T> iterator() {
    return apdatee.iterator();
  }

  @Override
  public Object[] toArray() {
    return apdatee.toArray();
  }

  @Override
  public <T1> T1[] toArray(T1[] a) {
    return apdatee.toArray(a);
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    return apdatee.containsAll(c);
  }

  @Override
  public boolean addAll(Collection<? extends T> c) {
    return apdatee.addAll(c);
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    return apdatee.removeAll(c);
  }

  @Override
  public boolean removeIf(Predicate<? super T> filter) {
    return apdatee.removeIf(filter);
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    return apdatee.retainAll(c);
  }

  @Override
  public void clear() {
    apdatee.clear();
  }

  @Override
  public boolean equals(Object o) {
    return apdatee.equals(o);
  }

  @Override
  public int hashCode() {
    return apdatee.hashCode();
  }

  @Override
  public Spliterator<T> spliterator() {
    return apdatee.spliterator();
  }

  @Override
  public Stream<T> stream() {
    return apdatee.stream();
  }

  @Override
  public Stream<T> parallelStream() {
    return apdatee.parallelStream();
  }

  @Override
  public void forEach(Consumer<? super T> action) {
    apdatee.forEach(action);
  }
}
