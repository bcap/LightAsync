package com.github.bcap.lightasync;

import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import com.github.bcap.lightasync.impl.OneShotQueue;
import com.github.bcap.lightasync.impl.UnboundProducer;

public class UnboundProducerTest {
	
	@Test
	public void testAddition() throws Exception {
		final List<String> consumed = new ArrayList<String>();
		final int size = 1000;
		final String prefix1 = "polaco";
		final String prefix2 = "lol";
		
		UnboundProducer<String> producer1 = new UnboundProducer<String>();
		UnboundProducer<String> producer2 = new UnboundProducer<String>();
		
		OneShotQueue<String> queue = new OneShotQueue<String>();
		Consumer<String> consumer = new Consumer<String>() {
			public void finished() {
			}
			public void consume(String obj) {
				consumed.add(obj);
			}
		};
		queue.attachConsumer(consumer);
		queue.attachProducer(producer1);
		queue.attachProducer(producer2);
		queue.start();
		
		for(int i = 0; i < size; i++) {
			producer1.addToQueue(prefix1 + i);
			producer2.addToQueue(prefix2 + i);
		}
		
		producer1.finish();
		producer2.finish();
		
		queue.waitExecution();
		
		assertEquals(size * 2, consumed.size());
		for(int i = 0; i < size; i++) {
			assertTrue("Consumed list does not contains element " + prefix1 + i, consumed.contains(prefix1 + i));
			assertTrue("Consumed list does not contains element " + prefix2 + i, consumed.contains(prefix2 + i));
		}
	}
	
}
