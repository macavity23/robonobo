package com.robonobo.gui.preferences;

import com.robonobo.gui.frames.RobonoboFrame;

public class UploadRatePrefPanel extends PrefPanel {
	private RobonoboFrame frame;
	
	
	public UploadRatePrefPanel(RobonoboFrame frame, RobonoboFrame frame2) {
		super(frame);
		frame = frame2;
	}

	@Override
	public boolean hasChanged() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void applyChanges() {
		// TODO Auto-generated method stub

	}

	@Override
	public void resetValue() {
		// TODO Auto-generated method stub

	}

}
