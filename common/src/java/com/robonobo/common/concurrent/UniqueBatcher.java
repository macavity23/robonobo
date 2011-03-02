package com.robonobo.common.concurrent;

import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;

/**
 * Like Batcher, but will silently drop duplicate objects (as determined by equals())
 */
public abstract class UniqueBatcher<T> extends Batcher<T> {
	public UniqueBatcher(long timespan, ScheduledThreadPoolExecutor executor) {
		super(timespan, executor);
	}

	@Override
	public void doRun() throws Exception {
		lock.lock();
		task = null;
		Set<T> runObjs = new HashSet<T>();
		runObjs.addAll(queuedObjs);
		queuedObjs.clear();
		lock.unlock();
		runBatch(runObjs);
	}
}
