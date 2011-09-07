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

public class TaskProgressSheet extends Sheet {
	private String verb;
	private int total;
	private JProgressBar progressBar;
	private RGlassButton feckOffBtn;

	public TaskProgressSheet(RobonoboFrame frame, String title, String verb, int total) {
		super(frame);
		this.verb = verb;
		this.total = total;
		setPreferredSize(new Dimension(300, 100));
		double[][] cells = { { 20, TableLayout.FILL, 50, 20 }, { 10, 20, 10, 30, 5, 30, 10 } };
		setLayout(new TableLayout(cells));
		add(new RLabel16B(title), "1,1,2,1");
		progressBar = new JProgressBar();
		add(progressBar, "1,3,2,3");
		setProgress(0);
		feckOffBtn = new RGlassButton("Run in background");
		feckOffBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				setVisible(false);
			}
		});
	}

	public void setProgress(final int i) {
		runOnUiThread(new CatchingRunnable() {
			public void doRun() throws Exception {
				progressBar.setString(verb + " " + i + " of " + total);
				progressBar.setMinimum(i);
				progressBar.setMaximum(total);
			}
		});
	}

	@Override
	public void onShow() {
	}

	@Override
	public JButton defaultButton() {
		return null;
	}
}
