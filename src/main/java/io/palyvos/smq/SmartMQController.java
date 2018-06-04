package io.palyvos.smq;

import java.util.concurrent.BlockingQueue;
import io.palyvos.smq.util.Backoff;

public interface SmartMQController {

  <T> int register(BlockingQueue<T> queue, Backoff backoff);

  <T> int register(BlockingQueue<T> queue);

  void init();
}
