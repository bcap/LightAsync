package com.github.bcap.lightasync.impl;

import java.util.concurrent.LinkedBlockingQueue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.bcap.lightasync.Consumer;
import com.github.bcap.lightasync.Producer;
import com.github.bcap.lightasync.Queue;

public class QueueImpl<T> extends Queue<T> {

	private static final Logger logger = LoggerFactory.getLogger(QueueImpl.class);

	private LinkedBlockingQueue<T> queue;
	
	private Integer maxSize;

	public QueueImpl() {
		this(null);
	}

	public QueueImpl(Integer maxSize) {
		this.maxSize = maxSize;
		if (maxSize != null) {
			if (maxSize <= 0)
				throw new IllegalArgumentException("Max size cannot be 0 or negative");
			this.queue = new LinkedBlockingQueue<T>(maxSize);
		} else {
			this.queue = new LinkedBlockingQueue<T>();
		}
	}

	protected void startConsumer(Consumer<T> consumer) {

	}

	protected void startProducer(Producer<T> producer) {

	}

	protected void stopConsumer(Consumer<T> consumer) {

	}

	protected void stopProducer(Producer<T> producer) {

	}

	protected void preStart() {

	}

	protected void postStart() {

	}

	protected void preShutdown() {

	}

	protected void postShutdown() {

	}

	public int size() {
		return queue.size();
	}

	public Integer maxSize() {
		return maxSize;
	}

	private class BaseThread<T> extends Thread {
		protected LinkedBlockingQueue<T> queue;
		
		public BaseThread(LinkedBlockingQueue<T> queue) {
			this.queue = queue;
		}
	}
	
	private class ConsumerThread<T> extends BaseThread<T> {
		public ConsumerThread(LinkedBlockingQueue<T> queue) {
			super(queue);
		}

		public void run() {
			
		}
	}

	private class ProducerThread<T> extends BaseThread<T> {
		public ProducerThread(LinkedBlockingQueue<T> queue) {
			super(queue);
		}
		
		public void run() {
			
		}
	}
}
