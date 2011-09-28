package com.robonobo.gui.sheets;

import com.robonobo.gui.frames.RobonoboFrame;

public class ConfirmCloseSheet extends ConfirmWithFeckOffSheet {
	public ConfirmCloseSheet(RobonoboFrame frame) {
		super(frame, "Please confirm exit",
				"Your friends will not be able to download the tracks on your playlists if nobody is sharing them. Are you sure you want to close robonobo?",
				"Show this screen on exit", true, "Exit");
	}

	@Override
	protected void confirmed(boolean feckOffSelected) {
		if (!feckOffSelected) {
			frame.guiCfg.setConfirmExit(false);
			frame.ctrl.saveConfig();
		}
		frame.shutdown();
	}
}
