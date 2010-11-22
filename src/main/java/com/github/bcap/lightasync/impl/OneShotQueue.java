package com.github.bcap.lightasync.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.bcap.lightasync.Consumer;
import com.github.bcap.lightasync.Producer;
import com.github.bcap.lightasync.Queue;

public class OneShotQueue<T> extends Queue<T> {

	private static final Logger logger = LoggerFactory.getLogger(OneShotQueue.class);

	private LinkedBlockingQueue<QueueMessage<T>> queue;

	private Integer maxSize;

	private List<ConsumerThread> consumerThreads = new ArrayList<ConsumerThread>();
	private List<ProducerThread> producerThreads = new ArrayList<ProducerThread>();

	public OneShotQueue() {
		this(null);
	}

	public OneShotQueue(Integer maxSize) {
		this.maxSize = maxSize;
		if (maxSize != null) {
			if (maxSize <= 0)
				throw new IllegalArgumentException("Max size cannot be 0 or negative");
			this.queue = new LinkedBlockingQueue<QueueMessage<T>>(maxSize);
		} else {
			this.queue = new LinkedBlockingQueue<QueueMessage<T>>();
		}
	}

	protected void startConsumer(Consumer<T> consumer) {
		ConsumerThread thread = new ConsumerThread(consumer);
		thread.startThread();
		consumerThreads.add(thread);
	}

	protected void startProducer(Producer<T> producer) {
		ProducerThread thread = new ProducerThread(producer);
		thread.startThread();
		producerThreads.add(thread);
	}

	protected void stopConsumer(Consumer<T> consumer) {
		ConsumerThread thread = null;
		for (Iterator<ConsumerThread> it = consumerThreads.iterator(); it.hasNext() && thread == null;) {
			ConsumerThread itThread = it.next();
			if (itThread.consumer == consumer)
				thread = itThread;
		}
		thread.stopThread();
		consumerThreads.remove(thread);
	}

	protected void stopProducer(Producer<T> producer) {
		ProducerThread thread = null;
		for (Iterator<ProducerThread> it = producerThreads.iterator(); it.hasNext() && thread == null;) {
			ProducerThread itThread = it.next();
			if (itThread.producer == producer)
				thread = itThread;
		}
		thread.stopThread();
		producerThreads.remove(thread);
	}

	protected void preStart() {

	}

	protected void postStart() {

	}

	protected void preShutdown() {

	}

	protected void postShutdown() {

	}

	protected void producerFinished(ProducerThread producer) {
		boolean finished = true;
		for (Iterator<ProducerThread> it = producerThreads.iterator(); it.hasNext() && finished; finished = it.next().finished);

		if(finished) {
			for (ConsumerThread thread : consumerThreads)
				thread.producersFinished = true;
		}
	}

	public int size() {
		return queue.size();
	}

	public Integer maxSize() {
		return maxSize;
	}

	private abstract class BaseThread extends Thread {

		protected boolean running = false;

		public synchronized void startThread() {
			running = true;
			super.start();
		}

		public synchronized void stopThread() {
			running = false;
			this.interrupt();
		}
	}

	private class ConsumerThread extends BaseThread {

		protected Consumer<T> consumer;

		protected boolean producersFinished = false;

		public ConsumerThread(Consumer<T> consumer) {
			this.consumer = consumer;
		}

		public void run() {
			running = true;

			while (running) {
				try {
					QueueMessage<T> message = queue.poll(50, TimeUnit.MILLISECONDS);
					if (message != null) {
						try {
							consumer.consume(message.getContent());
						} catch (RuntimeException e) {
							logger.error("Consumer threw an Exception", e);
						}
					} else if (producersFinished) {
						running = false;
					}
				} catch (InterruptedException e) {
					logger.debug("Consumer thread interrupted");
				}
			}
			
			if(producersFinished)
				consumer.finished();

			running = false;
		}
	}

	private class ProducerThread extends BaseThread {

		protected Producer<T> producer;

		protected boolean finished = false;

		public ProducerThread(Producer<T> producer) {
			this.producer = producer;
		}

		public void run() {
			running = true;

			finished = producer.finished();

			while (running && !finished) {
				try {
					T obj = producer.produce();
					queue.put(new QueueMessage<T>(obj));
					finished = producer.finished();
				} catch (InterruptedException e) {
					logger.debug("Producer thread interrupted");
				} catch (RuntimeException e) {
					logger.error("Producer threw an Exception", e);
				}
			}

			running = false;

			if (finished)
				producerFinished(this);
		}
	}
}
