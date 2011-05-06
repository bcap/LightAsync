package com.github.bcap.lightasync;

public interface Producer<T> {
	
	public boolean isFinished();
	
	public T produce();
	
}
