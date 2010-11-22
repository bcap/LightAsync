package com.github.bcap.lightasync;

import org.junit.Test;

public class OneShotQueueTest {

	@Test
	public void simpleTest() {
		Queue<String> queue = Queue.createQueue();
		Producer<String> producer = new TestProducer();
		Consumer<String> consumer1 = new TestConsumer();
		Consumer<String> consumer2 = new TestConsumer();

		queue.attachProducer(producer);
		queue.attachConsumer(consumer1);
		queue.attachConsumer(consumer2);
		queue.start();
	}
}

class TestProducer implements Producer<String> {

	private int produced = 0;

	public boolean finished() {
		return produced >= 100;
	}

	public String produce() {
		String element = "Element " + produced;
		produced++;
		return element;
	}
}

class TestConsumer implements Consumer<String> {

	public void consume(String obj) {
	}

	public void finished() {
	}

}