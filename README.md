# SmartMultiQueues

----

## Overview

SmartMultiQueues provides an abstraction that changes the blocking behavior
of a number of `BlockingQueue`s so that it mimics that of one single shared blocking queue.
This means that insertions only block only when all queues are full and
removals only block when all queues are empty.

Such a data structure can be useful in use-cases that use the split-join paradigm
like in the image below.

[img]

In domains such as stream processing, depending on the semantics of the Producers 
and Consumers, it might be desirable to keep the blocking behavior but only when
there is nothing to be done, i.e. all queues are full or empty.

Additionally, using blocking queues would lead to a deadlock in the situation depicted above. 
SmartMultiQueues prevent this issue altogether.

Several extensions are possible, such as not blocking but notifying an observer instead but this project
provides a basic, bare-bones implementation of the data-structure. Additionally, providing the queues as
an interface of type `Queue` is a bit misleading because `offer()` and `poll()` might block,
but it is easy enough to use the basic data structure (`SmartMQReader` and `SmartMQWriter`) in another queue implementation.


## Usage

The easiest way to create these queues is through `QueueFactory`:

```java
// Create two 'connected' smqs with capacity 1
List<Queue<Integer>> queues = QueueFactory.INSTANCE.newArraySmartMQs(2, 1);

Queue<Integer> q1 = queues.get(1)
Queue<Integer> q2 = queues.get(2)

// Use them as normal queues

// Thread 1
q1.offer(1)
q1.offer(2)
q2.offer(3)
q2.offer(4) // Blocks

// Thread 2
q1.poll() // Blocks, until q1.offer(1) is called

```
