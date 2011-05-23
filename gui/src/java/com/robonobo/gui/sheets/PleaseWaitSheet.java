package com.robonobo.gui.sheets;

import info.clearthought.layout.TableLayout;

import javax.swing.JButton;

import com.robonobo.gui.components.base.RLabel;
import com.robonobo.gui.components.base.RLabel14B;
import com.robonobo.gui.frames.RobonoboFrame;

@SuppressWarnings("serial")
public class PleaseWaitSheet extends Sheet {
	public PleaseWaitSheet(RobonoboFrame frame, String whatsHappening) {
		super(frame);
		RLabel lbl = new RLabel14B();
		int textWidth = getFontMetrics(lbl.getFont()).stringWidth(whatsHappening);
		double[][] cellSizen = { { 10, 250 + textWidth, 10 }, { 10, 50, 10 } };
		setLayout(new TableLayout(cellSizen));
		setName("playback.background.panel");
		lbl.setText("Please wait, " + whatsHappening + "...");
		add(lbl, "1,1,CENTER,CENTER");
	}

	@Override
	public void onShow() {
		// Do nothing
	}

	@Override
	public JButton defaultButton() {
		return null;
	}
}
