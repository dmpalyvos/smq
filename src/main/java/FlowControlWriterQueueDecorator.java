import java.util.Collection;
import java.util.Iterator;
import java.util.Queue;
import java.util.Spliterator;
import java.util.function.Consumer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class FlowControlWriterQueueDecorator<T> implements Queue<T> {

  private final Queue<T> decorated;
  private final FlowControlWriter<T> flowControl;
  private final int queueIndex;

  public FlowControlWriterQueueDecorator(Queue<T> decorated, int queueIndex, FlowControlWriter flowControl) {
    this.decorated = decorated;
    this.flowControl = flowControl;
    this.queueIndex = queueIndex;
  }

  @Override
  public boolean add(T t) {
    return decorated.add(t);
  }

  @Override
  public boolean offer(T t) {
    return decorated.offer(t);
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
