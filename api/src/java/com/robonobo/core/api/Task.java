package com.robonobo.core.api;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.robonobo.common.concurrent.CatchingRunnable;

public abstract class Task extends CatchingRunnable {
	private int id;
	private ThreadPoolExecutor executor;
	private List<TaskListener> listeners = new ArrayList<TaskListener>();
	
	protected String title;
	protected String statusText;
	/** 0 - 1 */
	protected float completion;
	protected boolean cancelRequested;
	protected Log log = LogFactory.getLog(getClass());
	
	@Override
	public void doRun() throws Exception {
		try {
			runTask();
		} catch(Exception e) {
			log.error("Caught exception running task '"+title+"'", e);
			statusText = "An error occurred: "+e.getMessage();
			completion = 1f;
			fireUpdated();
		}
	}
	
	public abstract void runTask() throws Exception;
	
	public void cancel() {
		cancelRequested = true;
		fireUpdated();
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public float getCompletion() {
		return completion;
	}
	
	public void setCompletion(float completion) {
		this.completion = completion;
	}
	
	
	public ThreadPoolExecutor getExecutor() {
		return executor;
	}
	
	public void setExecutor(ThreadPoolExecutor executor) {
		this.executor = executor;
	}
	
	public void addListener(TaskListener l) {
		listeners.add(l);
	}
	
	public void removeListener(TaskListener l) {
		listeners.remove(l);
	}
	
	protected void fireUpdated() {
		for (TaskListener l : listeners) {
			l.taskUpdated(this);
		}
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getStatusText() {
		return statusText;
	}

	public void setStatusText(String statusText) {
		this.statusText = statusText;
	}

	protected void cancelConfirmed() {
		statusText = "Cancelled";
		completion = 1f;
		fireUpdated();
	}
	
	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof Task))
			return false;
		Task t = (Task) obj;
		return t.getId() == getId();
	}
	
	@Override
	public int hashCode() {
		return getClass().getName().hashCode() ^ getId();
	}
}
