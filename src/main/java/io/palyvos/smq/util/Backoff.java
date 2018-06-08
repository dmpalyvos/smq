package io.palyvos.smq.util;

/**
 * Backoff that slows down producers to match the speed of consumers and vice-versa. To avoid a
 * class hierarchy of factories, the method {@link #newInstance()} can be used to generate new
 * instances of the object, in a prototype-like fashion.
 *
 * @author palivosd
 */
public interface Backoff {

  Backoff newInstance();

  void backoff();

  void relax();
}
