package com.github.bcap.lightasync.impl;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.bcap.lightasync.Producer;

public class UnboundProducer<T> implements Producer<T> {

	private static final Logger logger = LoggerFactory.getLogger(UnboundProducer.class);

	public static final int DEFAULT_PREQUEUE_SIZE = 100;
	
	private boolean finished = false;
	
	private LinkedBlockingQueue<T> preQueue;
	
	public UnboundProducer() {
		this(DEFAULT_PREQUEUE_SIZE);
	}

	public UnboundProducer(int preQueueMaxSize) {
		this.preQueue = new LinkedBlockingQueue<T>(preQueueMaxSize);
	}

	public void addToQueue(T obj) {
		try {
			logger.debug("Adding obj " + obj + " to the pre queue. Pre queue size: " + preQueue.size());
			preQueue.put(obj);
		} catch (InterruptedException e) {
			logger.error("InterruptedException occured while trying to add obj " + obj + " to the producer preQueue. Object will not be added to the queue", e);
		}
	}
	
	public void finish() {
		finished = true;
	}
	
	public T produce() {
		try {
			while(!isFinished()) {
				T obj = preQueue.poll(50, TimeUnit.MILLISECONDS);
				if(obj != null)
					return obj;
			}
			throw new IllegalStateException("producer already finished");
		} catch (InterruptedException e) {
			throw new RuntimeException(e);
		}
	}
	
	public boolean isFinished() {
		return finished && preQueue.size() == 0;
	}
}
