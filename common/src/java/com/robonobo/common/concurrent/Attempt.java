package com.robonobo.common.concurrent;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;


public abstract class Attempt extends CatchingRunnable {
	private ScheduledThreadPoolExecutor exec;
	private int timeoutMs;
	private ScheduledFuture<?> timeoutFuture;
	protected List<Attempt> contingentAttempts;

	public Attempt(ScheduledThreadPoolExecutor executor, int timeoutMs, String name) {
		this.exec = executor;
		this.timeoutMs = timeoutMs;
		contingentAttempts = new ArrayList<Attempt>();
	}

	public synchronized void start() {
		timeoutFuture = exec.schedule(this, timeoutMs, TimeUnit.MILLISECONDS);
	}

	public synchronized void cancel() {
		if (timeoutFuture.isDone())
			return;
		timeoutFuture.cancel(false);
		for (Attempt a : contingentAttempts) {
			a.cancel();
		}
	}

	public synchronized void succeeded() {
		if (timeoutFuture != null) {
			if (timeoutFuture.isDone())
				return;
			timeoutFuture.cancel(false);
		}
		exec.execute(new CatchingRunnable() {
			public void doRun() throws Exception {
				onSuccess();
				for (Attempt a : contingentAttempts) {
					a.succeeded();
				}
			}
		});
	}

	public synchronized void failed() {
		if (timeoutFuture.isDone())
			return;

		timeoutFuture.cancel(false);
		exec.execute(new CatchingRunnable() {
			public void doRun() throws Exception {
				onFail();
			}
		});

		for (Attempt a : contingentAttempts) {
			a.failed();
		}
	}

	public void doRun() {
		timedOut();
	}

	public void addContingentAttempt(Attempt a) {
		contingentAttempts.add(a);
	}

	protected void timedOut() {
		exec.execute(new CatchingRunnable() {
			public void doRun() throws Exception {
				onTimeout();
			}
		});

		for (Attempt a : contingentAttempts) {
			a.failed();
		}
	}

	protected void onTimeout() {
	}

	protected void onFail() {
	}

	protected void onSuccess() {
	}
}
