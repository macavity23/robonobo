package com.robonobo.gui.components;

import static com.robonobo.common.util.TextUtil.*;

import java.util.HashSet;
import java.util.Set;

import javax.swing.Icon;
import javax.swing.SwingUtilities;

import com.robonobo.common.concurrent.CatchingRunnable;
import com.robonobo.core.api.Task;
import com.robonobo.core.api.TaskListener;
import com.robonobo.gui.GUIUtil;
import com.robonobo.gui.RoboColor;
import com.robonobo.gui.frames.RobonoboFrame;
import com.robonobo.gui.panels.LeftSidebar;

@SuppressWarnings("serial")
public class TaskListSelector extends LeftSidebarSelector implements TaskListener {
	static Icon runningIcon = new SpinnerIcon(16, 2, RoboColor.BLUE_GRAY);

	Set<Task> tasks = new HashSet<Task>();

	public TaskListSelector(LeftSidebar sideBar, RobonoboFrame frame) {
		super(sideBar, frame, "0 tasks running", true, runningIcon, "tasklist");
		frame.getController().addTaskListener(this);
	}

	@Override
	public void taskUpdated(final Task t) {
		SwingUtilities.invokeLater(new CatchingRunnable() {
			public void doRun() throws Exception {
				synchronized (TaskListSelector.this) {
					if (t.isCancelled() || (t.getCompletion() - 1f) == 0f)
						tasks.remove(t);
					else
						tasks.add(t);
					setText(numItems(tasks, "task") + " running");
					sideBar.showTaskList(tasks.size() > 0);
				}
			}
		});
	}
}
