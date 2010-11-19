package com.github.bcap.lightasync;

public interface Consumer<T> {
	
	public void consume(T obj);
	
	public void finished();
	
}
