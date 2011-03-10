package com.robonobo.gui.panels;

import info.clearthought.layout.TableLayout;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.swing.*;

import com.robonobo.common.concurrent.CatchingRunnable;
import com.robonobo.core.api.Task;
import com.robonobo.core.api.TaskListener;
import com.robonobo.gui.RoboColor;
import com.robonobo.gui.components.base.*;
import com.robonobo.gui.frames.RobonoboFrame;

@SuppressWarnings("serial")
public class TaskListContentPanel extends ContentPanel implements TaskListener {
	private Map<Integer, TaskPanel> tasks = new HashMap<Integer, TaskPanel>();
	private JPanel taskListPanel;

	public TaskListContentPanel(RobonoboFrame frame) {
		this.frame = frame;
		double[][] cellSizen = { { 1, TableLayout.FILL, 1 }, { 0, TableLayout.FILL, 0 } };
		setLayout(new TableLayout(cellSizen));

		taskListPanel = new JPanel();
		taskListPanel.setLayout(new BoxLayout(taskListPanel, BoxLayout.Y_AXIS));
		taskListPanel.setBackground(RoboColor.MID_GRAY);
		taskListPanel.setOpaque(true);
		add(new JScrollPane(taskListPanel, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_NEVER), "1,1");
		frame.getController().addTaskListener(this);
	}

	@Override
	public void taskUpdated(final Task t) {
		if(t.isCancelled())
			return;
		SwingUtilities.invokeLater(new CatchingRunnable() {
			public void doRun() throws Exception {
				if (tasks.containsKey(t.getId()))
					tasks.get(t.getId()).taskUpdated(t);
				else {
					TaskPanel p = new TaskPanel(t);
					tasks.put(t.getId(), p);
					taskListPanel.add(p);
				}
			}
		});
	}

	public void removeTask(final Task t) {
		TaskPanel p = tasks.remove(t.getId());
		if (p != null) {	
			taskListPanel.remove(p);
			taskListPanel.revalidate();
			// For some reason revalidate doesn't cause the panel to repaint...?
			RepaintManager.currentManager(taskListPanel).markCompletelyDirty(taskListPanel);
		}
	}

	class TaskPanel extends JPanel {
		Task t;
		RLabel titleLbl, statusLbl;
		RProgressBar progBar;
		private RButton cancelBtn;

		public TaskPanel(Task task) {
			this.t = task;

			double[][] cellSizen = { { 10, 200, 10, TableLayout.FILL, 10, 80, 10 }, { 10, 25, 10, 25, 10, 1 } };
			setLayout(new TableLayout(cellSizen));

			titleLbl = new RLabel18B(t.getTitle());
			add(titleLbl, "1,1,3,1,l,c");

			statusLbl = new RLabel12(t.getStatusText());
			add(statusLbl, "1,3");

			progBar = new RProgressBar(0, 100);
			progBar.setStringPainted(true);
			int pcnt = (int) (100 * t.getCompletion());
			progBar.setValue(pcnt);
			progBar.setString(pcnt + "%");
			add(progBar, "3,3");

			cancelBtn = new RRedGlassButton("Cancel");
			cancelBtn.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if(t.getCompletion() < 1f)
						t.cancel();
					removeTask(t);
					if(tasks.size() == 0)
						frame.getLeftSidebar().selectMyMusic();
				}
			});
			add(cancelBtn, "5,3");
			
			JSeparator sep = new JSeparator(JSeparator.HORIZONTAL);
			sep.setBackground(RoboColor.DARKISH_GRAY);
			add(sep, "1,5,5,5");
			// Fill width, but not height
			Dimension maxSz = new Dimension(Integer.MAX_VALUE, getPreferredSize().height);
			setMaximumSize(maxSz);
		}

		void taskUpdated(final Task t) {
			this.t = t;
			SwingUtilities.invokeLater(new CatchingRunnable() {
				public void doRun() throws Exception {
					titleLbl.setText(t.getTitle());
					statusLbl.setText(t.getStatusText());
					int pcnt = (int) (100 * t.getCompletion());
					progBar.setValue(pcnt);
					progBar.setString(pcnt + "%");
					if (pcnt == 100) {
						cancelBtn.setText("Clear");
						// Start a timer to nuke this task
						frame.getController().getExecutor().schedule(new CatchingRunnable() {
							public void doRun() throws Exception {
								removeTask(t);
							}
						}, frame.getGuiConfig().getZombieTaskLifetime(), TimeUnit.SECONDS);
					}									
				}
			});
		}
	}

}
