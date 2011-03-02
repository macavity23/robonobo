package com.robonobo.core.service;

import java.util.HashMap;
import java.util.Map;

import com.robonobo.core.api.TaskListener;
import com.robonobo.core.api.Task;

public class TaskService extends AbstractService implements TaskListener {
	Map<Integer, Task> runningTasks = new HashMap<Integer, Task>();
	int nextTaskId = 1;

	public TaskService() {
	}

	@Override
	public String getName() {
		return "Task Service";
	}

	@Override
	public String getProvides() {
		return "core.tasks";
	}

	@Override
	public void startup() throws Exception {
		// Do nothing
	}

	@Override
	public synchronized void shutdown() throws Exception {
		log.info("Cancelling all tasks");
		for (Task t : runningTasks.values()) {
			t.cancel();
		}
	}

	public void runTask(Task t) {
		int taskId;
		synchronized (this) {
			taskId = nextTaskId++;
			if (nextTaskId == Integer.MAX_VALUE)
				nextTaskId = 1;
			t.setId(taskId);
			runningTasks.put(taskId, t);
		}
		t.addListener(this);
		t.setExecutor(getRobonobo().getExecutor());
		getRobonobo().getExecutor().execute(t);
	}

	@Override
	public void taskUpdated(Task t) {
		if((t.getCompletion() - 1f) == 0) {
			synchronized (this) {
				runningTasks.remove(t.getId());
			}
		}
		getRobonobo().getEventService().fireTaskUpdated(t);
	}
}
