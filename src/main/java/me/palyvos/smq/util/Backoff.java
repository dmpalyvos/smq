package me.palyvos.smq.util;

public interface Backoff {

  Backoff newInstance();

  void backoff();

  void relax();
}
