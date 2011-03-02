package com.robonobo.core;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.robonobo.common.concurrent.CatchingRunnable;

/**
 * Runs jobs (runnables) one at a time.  If a job starts executing, it is guaranteed to finish exiting.
 * @author macavity
 */
public class JobRunner {
	private Log log;
	private List<Runnable> jobs = new ArrayList<Runnable>();
	private boolean stopped = false;
	private Thread thread;
	
	public JobRunner() {	
	}
	
	public void start() {
		log = LogFactory.getLog(getClass());
		stopped = false;
		thread = new Thread(new CatchingRunnable() {
			@Override
			public void doRun() throws Exception {
				while(true) {
					try {
						Runnable job;
						synchronized (JobRunner.this) {
							while(jobs.size() == 0) {
								JobRunner.this.wait();
							}
							job = jobs.remove(0);
						}
						try {
							job.run();
						} catch(Exception e) {
							log.error("Running job", e);
						}
						if(stopped)
							return;
					} catch(InterruptedException e) {
						if(stopped)
							return;
					}
				}
			}
		});
		thread.start();
	}
	
	/**
	 * This won't return until the currently-executing job (if any) has finished
	 */
	public void stop() {
		stopped = true;
		if(thread != null) {
			thread.interrupt();
			try {
				thread.join();
			} catch (InterruptedException ignore) {
			}
		}
	}
	
	public synchronized void addJob(Runnable job) {
		jobs.add(job);
		notify();
	}
}
