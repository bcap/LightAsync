package com.github.bcap.lightasync;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.github.bcap.lightasync.impl.OneShotQueue;

public class OneShotQueueTest {

	@Test
	public void simpleTest() throws InterruptedException {
		
		final int amount = 200;
		final int producerAmount = 20;
		final int consumerAmount = 15;
		
		OneShotQueue<String> queue = new OneShotQueue<String>();
		List<TestProducer> producers = new ArrayList<TestProducer>();
		List<TestConsumer> consumers = new ArrayList<TestConsumer>();
		
		for(int i = 0; i < producerAmount; i++) {
			TestProducer producer = new TestProducer("producer" + i, amount);
			producers.add(producer);
			queue.attachProducer(producer);
		}
		
		for(int i = 0; i < consumerAmount; i++) {
			TestConsumer consumer = new TestConsumer();
			consumers.add(consumer);
			queue.attachConsumer(consumer);
		}
		
		queue.start();
		queue.waitExecution();
		
		List<String> consumed = new ArrayList<String>();
		for (TestConsumer consumer : consumers) 
			consumed.addAll(consumer.consumed);
		
		List<String> produced = new ArrayList<String>();
		for (TestProducer producer : producers) 
			produced.addAll(producer.produced);

		assertEquals(amount * producerAmount, produced.size());
		assertEquals(amount * producerAmount, consumed.size());

		for(String consumedElement : consumed)
			assertTrue(produced.contains(consumedElement));
		
		for(String producedElement : produced)
			assertTrue(consumed.contains(producedElement));
	}
}

class TestProducer implements Producer<String> {

	protected final int amount;
	
	protected final String name;
	
	protected List<String> produced = new ArrayList<String>();

	public TestProducer(String name, int amount) {
		this.name = name;
		this.amount = amount;
	}
	
	public boolean isFinished() {
		return produced.size() >= amount;
	}

	public String produce() {
		String element = "Element " + produced.size() + " of producer " + name;
		produced.add(element);
		return element;
	}
}

class TestConsumer implements Consumer<String> {

	protected boolean finished = false;
	
	protected List<String> consumed = new ArrayList<String>();
	
	public void consume(String string) {
		consumed.add(string);
	}

	public void finished() {
		this.finished = true;
	}

}