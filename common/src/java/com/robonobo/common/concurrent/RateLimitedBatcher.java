package com.robonobo.common.concurrent;

import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Like Batcher, but limits the number of items batched per run - if any items are left after a run, the batcher will
 * run again after the specified interval
 * 
 * @author macavity
 */
public abstract class RateLimitedBatcher<T> extends Batcher<T> {
	protected int rateLimit;

	public RateLimitedBatcher(long timespanMs, ScheduledThreadPoolExecutor executor, int rateLimit) {
		super(timespanMs, executor);
		this.rateLimit = rateLimit;
	}

	@Override
	public void doRun() throws Exception {
		List<T> runObjs;
		lock.lock();
		task = null;
		if (queuedObjs.size() <= rateLimit) {
			runObjs = queuedObjs;
			queuedObjs = new ArrayList<T>();
		} else {
			runObjs = new ArrayList<T>();
			Iterator<T> it = queuedObjs.iterator();
			for (int i = 0; i < rateLimit; i++) {
				runObjs.add(it.next());
				it.remove();
			}
			// Reschedule our task to pick up the remainder
			task = executor.schedule(this, timespanMs, TimeUnit.MILLISECONDS);
		}
		lock.unlock();
		runBatch(runObjs);
	}
}
