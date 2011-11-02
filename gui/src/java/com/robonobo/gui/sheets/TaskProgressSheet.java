package com.robonobo.gui.sheets;

import static com.robonobo.gui.GuiUtil.*;
import info.clearthought.layout.TableLayout;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JProgressBar;

import com.robonobo.common.concurrent.CatchingRunnable;
import com.robonobo.gui.components.base.RGlassButton;
import com.robonobo.gui.components.base.RLabel16B;
import com.robonobo.gui.frames.RobonoboFrame;

@SuppressWarnings("serial")
public class TaskProgressSheet extends Sheet {
	private String verb;
	public int total;
	public JProgressBar progressBar;
	private RGlassButton feckOffBtn;

	public TaskProgressSheet(RobonoboFrame frame, String title, String verb, int total, boolean canBackground) {
		super(frame);
		this.verb = verb;
		this.total = total;
		setPreferredSize(new Dimension(500, 120));
		double[][] cells = { { 20, TableLayout.FILL, 160, 20 }, { 10, 20, 10, 30, 10, 30, 10 } };
		setLayout(new TableLayout(cells));
		setName("playback.background.panel");
		add(new RLabel16B(title), "1,1,2,1");
		progressBar = new JProgressBar();
		add(progressBar, "1,3,2,3");
		progressBar.setMinimum(0);
		progressBar.setMaximum(total);
		progressBar.setStringPainted(true);
		setProgress(0);
		if(canBackground) {
			feckOffBtn = new RGlassButton("Run in background");
			feckOffBtn.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					setVisible(false);
				}
			});
			add(feckOffBtn, "2,5");
		}
	}

	public void setProgress(final int i) {
		if (isVisible()) {
			runOnUiThread(new CatchingRunnable() {
				public void doRun() throws Exception {
					progressBar.setString(verb + " " + i + " of " + total);
					progressBar.setValue(i);
				}
			});
		}
	}

	@Override
	public void onShow() {
		feckOffBtn.requestFocusInWindow();
	}

	@Override
	public JButton defaultButton() {
		return feckOffBtn;
	}
}
