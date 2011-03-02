package com.robonobo.common.concurrent;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;


/**
 * For tasks that might be run one-at-a-time or many-after-each-other, when you
 * want to batch up the many- case into a single process or object. Waits an
 * amount of time, then calls the runBatch method.
 * 
 * To implement, subclass with the generic class T and implement the runBatch()
 * method
 * 
 * @author macavity
 * 
 * @param <T> The things that are being batched
 */
public abstract class Batcher<T> extends CatchingRunnable {
	protected long timespan;
	protected ScheduledThreadPoolExecutor executor;
	protected ScheduledFuture<?> task;
	protected List<T> queuedObjs = new ArrayList<T>();
	protected Lock lock = new ReentrantLock();

	/**
	 * @param timespan (millisecs)
	 */
	public Batcher(long timespan, ScheduledThreadPoolExecutor executor) {
		this.timespan = timespan;
		this.executor = executor;
	}

	public void add(T obj) {
		lock.lock();
		queuedObjs.add(obj);
		if (task == null)
			task = executor.schedule(this, timespan, TimeUnit.MILLISECONDS);
		lock.unlock();
	}

	public void addAll(Collection<? extends T> objs) {
		lock.lock();
		try {
			queuedObjs.addAll(objs);
			if (task == null)
				task = executor.schedule(this, timespan, TimeUnit.MILLISECONDS);
		} finally {
			lock.unlock();
		}
	}
	
	/** If our batch is scheduled to run, stop it */
	public void cancel() {
		lock.lock();
		if(task != null) {
			task.cancel(false);
			task = null;
		}
		lock.unlock();
	}

	@Override
	public void doRun() throws Exception {
		lock.lock();
		task = null;
		List<T> runObjs = queuedObjs;
		queuedObjs = new ArrayList<T>();
		lock.unlock();
		runBatch(runObjs);
	}

	/**
	 * Called when it's time to batch up the objects.
	 */
	protected abstract void runBatch(Collection<T> objs) throws Exception;

}
