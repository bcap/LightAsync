package com.github.bcap.lightasync;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.bcap.lightasync.impl.OneShotQueue;
import com.github.bcap.lightasync.misc.SimpleLifeCycle;

public abstract class Queue<T> implements SimpleLifeCycle {

	private static final Logger logger = LoggerFactory.getLogger(Queue.class);
	
	private List<Producer<? extends T>> producers = new ArrayList<Producer<? extends T>>();
	private List<Consumer<T>> consumers = new ArrayList<Consumer<T>>();

	private ReentrantLock producersLock = new ReentrantLock();
	private ReentrantLock consumersLock = new ReentrantLock();

	private boolean started = false;
	private boolean dead = false;

	public abstract int size();

	public abstract Integer maxSize();

	public void attachProducer(Producer<? extends T> producer) {
		
		logger.debug("Attaching producer " + producer);
		
		producersLock.lock();
		try {
			this.producers.add(producer);
			if (this.isAlive())
				this.startProducer(producer);
		} finally {
			producersLock.unlock();
		}
	}

	public void attachConsumer(Consumer<T> consumer) {
		
		logger.debug("Attaching consumer " + consumer);
		
		consumersLock.lock();
		try {
			this.consumers.add(consumer);
			if (this.isAlive())
				this.startConsumer(consumer);
		} finally {
			consumersLock.unlock();
		}
	}

	public void detachProducer(Producer<? extends T> producer) {
		
		logger.debug("Detaching producer " + producer);
		
		producersLock.lock();
		try {
			if (this.producers.contains(producer)) {
				if (this.isAlive())
					this.stopProducer(producer);

				producers.remove(producer);
			}
		} finally {
			producersLock.unlock();
		}
	}

	public void detachConsumer(Consumer<T> consumer) {
		
		logger.debug("Detaching consumer " + consumer);
		
		consumersLock.lock();
		try {
			if (this.consumers.contains(consumer)) {
				if (this.isAlive())
					this.stopConsumer(consumer);

				consumers.remove(consumer);
			}
		} finally {
			consumersLock.unlock();
		}

	}

	public List<Consumer<T>> getConsumers() {
		consumersLock.lock();
		try {
			return new ArrayList<Consumer<T>>(consumers);
		} finally {
			consumersLock.unlock();
		}
	}

	public List<Producer<? extends T>> getProducers() {
		producersLock.lock();
		try {
			return new ArrayList<Producer<? extends T>>(producers);
		} finally {
			producersLock.unlock();
		}
	}

	public void start() {
		logger.debug("Starting queue " + this);
		try {
			producersLock.lock();
			try {
				consumersLock.lock();

				if (!started) {
					this.preStart();

					for (Producer<? extends T> producer : producers)
						this.startProducer(producer);

					for (Consumer<T> consumer : consumers)
						this.startConsumer(consumer);

					this.postStart();

					this.started = true;
				}

			} finally {
				consumersLock.unlock();
			}
		} finally {
			producersLock.unlock();
		}
		logger.info("Queue " + this + " successfully started");
	}

	public void shutdown() {
		logger.info("Shutting down queue " + this);
		try {
			producersLock.lock();
			try {
				consumersLock.lock();

				if (!dead) {
					this.preShutdown();

					for (Producer<? extends T> producer : producers)
						this.stopProducer(producer);

					for (Consumer<T> consumer : consumers)
						this.stopConsumer(consumer);

					this.postShutdown();

					dead = true;
				}

			} finally {
				consumersLock.unlock();
			}
		} finally {
			producersLock.unlock();
		}
		logger.info("Queue " + this + " successfully shutted down");
	}

	public boolean isAlive() {
		return this.isStarted() && !this.isShuttedDown();
	}

	public boolean isShuttedDown() {
		return dead;
	}

	public boolean isStarted() {
		return started;
	}

	protected abstract void preStart();

	protected abstract void postStart();

	protected abstract void preShutdown();

	protected abstract void postShutdown();

	protected abstract void startProducer(Producer<? extends T> producer);

	protected abstract void startConsumer(Consumer<T> consumer);

	protected abstract void stopProducer(Producer<? extends T> producer);

	protected abstract void stopConsumer(Consumer<T> consumer);
	
	public static <T> Queue<T> createQueue() {
		return new OneShotQueue<T>();
	}
	
	public static <T> Queue<T> createQueue(int maxSize) {
		return new OneShotQueue<T>(maxSize);
	}

}
