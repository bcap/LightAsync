package com.github.bcap.lightasync;

import java.util.List;

import com.github.bcap.lightasync.misc.SimpleLifeCycle;

public interface Queue<T> extends SimpleLifeCycle {
	
	public void attachProducer(Producer<T> producer);
	
	public void attachConsumer(Consumer<T> consumer);
	
	public void detachProducer(Producer<T> producer);
	
	public void detachConsumer(Consumer<T> consumer);
	
	public List<Consumer<T>> getConsumers();
	
	public List<Producer<T>> getProducers();
	
	public int size();
}
