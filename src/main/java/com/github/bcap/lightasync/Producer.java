package com.github.bcap.lightasync;

public interface Producer<T> {
	
	public boolean finished();
	
	public T produce();
	
}
