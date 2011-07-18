package com.robonobo.gui.sheets;

import info.clearthought.layout.TableLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import com.robonobo.gui.components.base.*;
import com.robonobo.gui.frames.RobonoboFrame;

public class ConfirmCloseSheet extends ConfirmWithFeckOffSheet {
	public ConfirmCloseSheet(RobonoboFrame frame) {
		super(frame, "Please confirm exit",
				"Your friends will not be able to download the tracks on your playlists if nobody is sharing them. Are you sure you want to close robonobo?",
				"Show this screen on exit", true, "exit");
	}

	@Override
	protected void confirmed(boolean feckOffSelected) {
		if (!feckOffSelected) {
			frame.getGuiConfig().setConfirmExit(false);
			frame.getController().saveConfig();
		}
		frame.shutdown();
	}
}
