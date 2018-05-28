package me.palyvos.smq;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import me.palyvos.smq.util.Consumer;
import me.palyvos.smq.util.Producer;
import me.palyvos.smq.util.TestUtil;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class SmartMQControllerTest {

  @DataProvider(name = "deadlock-test-data")
  public Object[][] deadlockTestData() {
    return new Object[][]{
        // Deadlock scenario: output queue size = 0
        {Arrays.asList(TestUtil.newQueue(), TestUtil.newQueue()), new ConcurrentLinkedQueue<String>(), 10, 5, 50, 0},
        // Correct scenario: output queue size = 2 queues * 2 writes per queue * 50 repetitions = 200
        {SmartMQController.withArrayBlockingQueues(2, 1).getQueues(), new ConcurrentLinkedQueue<String>(), 10, 5, 50, 200},
    };
  }

  @Test(dataProvider = "deadlock-test-data")
  public void deadlockTest(List<Queue<String>> inputs, Queue<String> output,
      long producerSleep, long consumerSleep, int producerRepetitions, int expectedSize) {

    Thread producerThread = new Thread(new Producer(inputs, producerSleep, "PRODUCER", producerRepetitions));

    // Consumer starts reading from the other queue to force deadlock
    List<Queue<String>> consumerInputs = new ArrayList<>(inputs);
    Collections.reverse(consumerInputs);
    Thread consumerThread = new Thread(new Consumer(consumerInputs, output, consumerSleep, "CONSUMER"));

    producerThread.start();
    consumerThread.start();

    TestUtil.sleep(producerRepetitions * Math.max(producerSleep, consumerSleep) * 2);
    Assert.assertEquals(output.size(), expectedSize);
  }


  @DataProvider(name = "speed-test-data")
  public Object[][] speedTestData() {
    final int capacity = 10000;
    return new Object[][]{
        {Arrays.asList(TestUtil.newQueue(capacity), TestUtil.newQueue()), new ConcurrentLinkedQueue<String>(), 1, 1, 1000},
        {SmartMQController.withArrayBlockingQueues(2, capacity).getQueues(), new ConcurrentLinkedQueue<String>(), 1, 1, 1000},
    };
  }

  @Test(dataProvider = "speed-test-data")
  public void speedTest(List<Queue<String>> inputs, Queue<String> output,
      long producerSleep, long consumerSleep, int producerRepetitions) {

  Thread producerThread = new Thread(new Producer(inputs, producerSleep, "PRODUCER", producerRepetitions));
  Thread consumerThread = new Thread(new Consumer(inputs, output, consumerSleep, "CONSUMER"));

    long start = System.currentTimeMillis();
    producerThread.start();
    consumerThread.start();

    try {
      producerThread.join();
      consumerThread.join();
    } catch (InterruptedException e) {
      e.printStackTrace();
    }

    System.out.format("Duration = %dms%n", System.currentTimeMillis() - start);


  }

}
