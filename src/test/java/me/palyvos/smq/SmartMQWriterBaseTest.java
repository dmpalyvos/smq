package me.palyvos.smq;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentLinkedQueue;
import me.palyvos.smq.util.Consumer;
import me.palyvos.smq.util.Producer;
import me.palyvos.smq.util.TestUtil;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class SmartMQWriterBaseTest {

  @DataProvider(name = "deadlock-test-data")
  public Object[][] deadlockTestData() {

    BlockingQueue<String> input1 = TestUtil.newQueue();
    BlockingQueue<String> input2 = TestUtil.newQueue();
    SmartMQWriterBase writer = new SmartMQWriterBase(input1, input2);

    return new Object[][]{
        // Deadlock scenario: output queue size = 0
        {Arrays.asList(TestUtil.newQueue(), TestUtil.newQueue()), new ConcurrentLinkedQueue<String>(), 10, 5, 50, 0},
        // Correct scenario: output queue size = 2 queues * 2 writes per queue * 50 repetitions = 200
        {writer.getQueues(), new ConcurrentLinkedQueue<String>(), 10, 5, 50, 200},
    };
  }

  @Test(dataProvider = "deadlock-test-data")
  public void deadlockTest(List<BlockingQueue<String>> inputs, Queue<String> output,
      long producerSleep, long consumerSleep, int producerRepetitions, int expectedSize) {

    Thread producerThread = new Thread(new Producer(inputs, producerSleep, "PRODUCER", producerRepetitions));

    // Consumer starts reading from the other queue to force deadlock
    List<BlockingQueue<String>> consumerInputs = new ArrayList<>(inputs);
    Collections.reverse(consumerInputs);
    Thread consumerThread = new Thread(new Consumer<>(consumerInputs, output, consumerSleep, "CONSUMER"));

    producerThread.start();
    consumerThread.start();

    TestUtil.sleep(producerRepetitions * Math.max(producerSleep, consumerSleep) * 2);
    Assert.assertEquals(output.size(), expectedSize);
  }


}
