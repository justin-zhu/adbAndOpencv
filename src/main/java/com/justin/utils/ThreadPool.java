package com.justin.utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

//线程池
public class ThreadPool {
	private static ExecutorService executor;
	private static ScheduledExecutorService scheduledThreadPool;

	private ThreadPool() {
	}

	public static ExecutorService getCachedThreadPool() {
		if (executor == null) {
			executor = Executors.newFixedThreadPool(5);
		}
		return executor;
	}

	public static ScheduledExecutorService getScheduledThreadPool() {
		if (scheduledThreadPool == null) {
			scheduledThreadPool = Executors.newScheduledThreadPool(1);
		}
		return scheduledThreadPool;
	}

}
