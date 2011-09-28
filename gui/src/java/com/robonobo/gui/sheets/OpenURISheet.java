package com.robonobo.gui.sheets;

import info.clearthought.layout.TableLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import com.robonobo.common.concurrent.CatchingRunnable;
import com.robonobo.gui.components.base.*;
import com.robonobo.gui.frames.RobonoboFrame;

public class OpenURISheet extends Sheet {
	private RButton openBtn;
	private RTextField uriField;

	public OpenURISheet(RobonoboFrame f) {
		super(f);
		double[][] cellSizen = { { 10, 160, 5, 80, 10 }, { 10, 20, 5, 30, 5, 30, 10 } };
		setLayout(new TableLayout(cellSizen));
		setName("playback.background.panel");
		add(new RLabel14B("Open URI"), "1,1,3,1");
		uriField = new RTextField();
		add(uriField, "1,3,3,3");
		openBtn = new RGlassButton("Open");
		add(openBtn, "3,5");
		openBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				frame.ctrl.getExecutor().execute(new CatchingRunnable() {
					public void doRun() throws Exception {
						frame.openRbnbUri(uriField.getText());
					}
				});
				OpenURISheet.this.setVisible(false);
			}
		});
	}
	
	@Override
	public void onShow() {
		uriField.requestFocusInWindow();
	}
	
	@Override
	public JButton defaultButton() {
		return openBtn;
	}
}
