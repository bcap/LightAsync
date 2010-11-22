package com.github.bcap.lightasync.impl;

public class QueueMessage<T> {

	private T content;
	private int retries;
	private int retryDelay;
	
	public QueueMessage(T content) {
		this.content = content;
	}

	public T getContent() {
		return content;
	}

	public int getRetries() {
		return retries;
	}

	public int incrementeRetries() {
		return ++retries;
	}
	
	public int getRetryDelay() {
		return retryDelay;
	}

	public void setRetryDelay(int retryDelay) {
		this.retryDelay = retryDelay;
	}

}
