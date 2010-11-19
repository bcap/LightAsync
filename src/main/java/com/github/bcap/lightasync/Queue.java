package com.github.bcap.lightasync;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import com.github.bcap.lightasync.misc.SimpleLifeCycle;

public abstract class Queue<T> implements SimpleLifeCycle {

	private List<Producer<T>> producers = new ArrayList<Producer<T>>();
	private List<Consumer<T>> consumers = new ArrayList<Consumer<T>>();

	private ReentrantLock producersLock = new ReentrantLock();
	private ReentrantLock consumersLock = new ReentrantLock();

	private boolean started = false;
	private boolean dead = false;

	public abstract int size();

	public abstract Integer maxSize();

	public void attachProducer(Producer<T> producer) {
		if (this.isAlive())
			this.startProducer(producer);

		producersLock.lock();
		try {
			this.producers.add(producer);
		} finally {
			producersLock.unlock();
		}
	}

	public void attachConsumer(Consumer<T> consumer) {
		if (this.isAlive())
			this.startConsumer(consumer);

		consumersLock.lock();
		try {
			this.consumers.add(consumer);
		} finally {
			consumersLock.unlock();
		}
	}

	public void detachProducer(Producer<T> producer) {
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
			List<Consumer<T>> result = new ArrayList<Consumer<T>>(consumers.size());
			Collections.copy(result, consumers);
			return result;
		} finally {
			consumersLock.unlock();
		}
	}

	public List<Producer<T>> getProducers() {
		producersLock.lock();
		try {
			List<Producer<T>> result = new ArrayList<Producer<T>>(producers.size());
			Collections.copy(result, producers);
			return result;
		} finally {
			producersLock.unlock();
		}
	}

	public void start() {
		try {
			producersLock.lock();
			try {
				consumersLock.lock();

				if (!started) {
					this.preStart();

					for (Producer<T> producer : producers)
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
	}

	public void shutdown() {
		try {
			producersLock.lock();
			try {
				consumersLock.lock();

				if (!dead) {
					this.preShutdown();

					for (Producer<T> producer : producers)
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

	protected abstract void startProducer(Producer<T> producer);

	protected abstract void startConsumer(Consumer<T> consumer);

	protected abstract void stopProducer(Producer<T> producer);

	protected abstract void stopConsumer(Consumer<T> consumer);

}
