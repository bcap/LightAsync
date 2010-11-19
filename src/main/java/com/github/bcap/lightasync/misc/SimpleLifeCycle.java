package com.github.bcap.lightasync.misc;

public interface SimpleLifeCycle {

	public void start();

	public void shutdown();
	
	public boolean isStarted();
	
	public boolean isShuttedDown();
	
	public boolean isAlive();
}
