package com.github.bcap.lightasync;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.github.bcap.lightasync.impl.OneShotQueue;

public class OneShotQueueTest {

	@Test
	public void testObjectInheritanceProduction() throws InterruptedException {
		final int amount = 200;
		
		TestProducer<TestObj> producer = new TestProducer<TestObj>("producer", amount) {
			public TestObj produce() {
				TestObj obj = new TestObj();
				obj.stringValue = super.produce().stringValue;
				return obj;
			}
		};
		
		TestProducer<TestObj1> producer1 = new TestProducer<TestObj1>("producer1", amount) {
			public TestObj1 produce() {
				TestObj1 obj = new TestObj1();
				obj.stringValue = ((TestObj)super.produce()).stringValue;
				return obj;
			}
		};
		
		TestProducer<TestObj2> producer2 = new TestProducer<TestObj2>("producer2", amount) {
			public TestObj2 produce() {
				TestObj2 obj = new TestObj2();
				obj.stringValue = ((TestObj)super.produce()).stringValue;
				return obj;
			}
		};
		
		TestProducer<TestObj3> producer3 = new TestProducer<TestObj3>("producer3", amount) {
			public TestObj3 produce() {
				TestObj3 obj = new TestObj3();
				obj.stringValue = ((TestObj)super.produce()).stringValue;
				return obj;
			}
		};
		
		TestConsumer consumer = new TestConsumer();
		
		OneShotQueue<TestObj> queue = new OneShotQueue<TestObj>();
		queue.attachProducer(producer);
		queue.attachProducer(producer1);
		queue.attachProducer(producer2);
		queue.attachProducer(producer3);
		queue.attachConsumer(consumer);
		
		queue.start();
		queue.waitExecution();
		
		List<String> produced = new ArrayList<String>();
		for (TestObj producedObj : producer.produced)
			produced.add(producedObj.stringValue);
		for (TestObj producedObj : producer1.produced)
			produced.add(producedObj.stringValue);
		for (TestObj producedObj : producer2.produced)
			produced.add(producedObj.stringValue);
		for (TestObj producedObj : producer3.produced)
			produced.add(producedObj.stringValue);
		
		List<String> consumed = new ArrayList<String>();
		for (TestObj consumedObj : consumer.consumed)
			consumed.add(consumedObj.stringValue);
		
		assertNotNull(queue.getProducers());
		assertEquals(4, queue.getProducers().size());

		assertNotNull(queue.getConsumers());
		assertEquals(1, queue.getConsumers().size());

		assertEquals(amount * 4, produced.size());
		assertEquals(amount * 4, consumer.consumed.size());

		for (String consumedElement : consumed)
			assertTrue(produced.contains(consumedElement));

		for (String producedElement : produced)
			assertTrue(consumed.contains(producedElement));
	}

	@Test
	public void testHeavyConcurrency() throws InterruptedException {

		final int amount = 300;
		final int producerAmount = 30;
		final int consumerAmount = 20;

		OneShotQueue<TestObj> queue = new OneShotQueue<TestObj>();
		List<TestProducer<TestObj>> producers = new ArrayList<TestProducer<TestObj>>();
		List<TestConsumer> consumers = new ArrayList<TestConsumer>();

		for (int i = 0; i < producerAmount; i++) {
			TestProducer<TestObj> producer = new TestProducer<TestObj>("producer" + i, amount);
			producers.add(producer);
			queue.attachProducer(producer);
		}

		for (int i = 0; i < consumerAmount; i++) {
			TestConsumer consumer = new TestConsumer();
			consumers.add(consumer);
			queue.attachConsumer(consumer);
		}

		queue.start();
		queue.waitExecution();

		List<String> consumed = new ArrayList<String>();
		for (TestConsumer consumer : consumers)
			for (TestObj consumedObj : consumer.consumed)
				consumed.add(consumedObj.stringValue);
			
		List<String> produced = new ArrayList<String>();
		for (TestProducer<TestObj> producer : producers)
			for (TestObj producedObj : producer.produced)
				produced.add(producedObj.stringValue);

		assertNotNull(queue.getProducers());
		assertEquals(producerAmount, queue.getProducers().size());

		assertNotNull(queue.getConsumers());
		assertEquals(consumerAmount, queue.getConsumers().size());

		assertEquals(amount * producerAmount, produced.size());
		assertEquals(amount * producerAmount, consumed.size());

		for (String consumedElement : consumed)
			assertTrue(produced.contains(consumedElement));

		for (String producedElement : produced)
			assertTrue(consumed.contains(producedElement));
	}

}

class TestProducer<T extends TestObj> implements Producer<T> {

	protected final int amount;

	protected final String name;

	protected List<T> produced = new ArrayList<T>();

	public TestProducer(String name, int amount) {
		this.name = name;
		this.amount = amount;
	}

	public boolean isFinished() {
		return produced.size() >= amount;
	}

	public T produce() {
		T element = (T) new TestObj();
		element.stringValue = "Element " + produced.size() + " of producer " + name;
		produced.add(element);
		return element;
	}
}

class TestConsumer implements Consumer<TestObj> {

	protected boolean finished = false;

	protected List<TestObj> consumed = new ArrayList<TestObj>();

	public void consume(TestObj testObj) {
		consumed.add(testObj);
	}

	public void finished() {
		this.finished = true;
	}
}

class TestObj {
	protected String stringValue;
}

class TestObj1 extends TestObj {
	protected boolean booleanValue;
}

class TestObj2 extends TestObj {
	protected int intValue;
}

class TestObj3 extends TestObj1 {
	protected byte byteValue;
}
