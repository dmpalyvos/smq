package io.palyvos.smq.util;

/**
 * {@link Backoff} implementation that does nothing. Usefull when no backoff is desired.
 *
 * @author palivosd
 */
public enum NoopBackoff implements Backoff {
  INSTANCE;

  @Override
  public Backoff newInstance() {
    return INSTANCE;
  }

  @Override
  public void backoff() {

  }

  @Override
  public void relax() {

  }
}
