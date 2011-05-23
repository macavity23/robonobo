package com.robonobo.gui.sheets;

import info.clearthought.layout.TableLayout;

import javax.swing.JButton;

import com.robonobo.gui.components.base.RLabel14B;
import com.robonobo.gui.frames.RobonoboFrame;

@SuppressWarnings("serial")
public class LibraryLoadingSheet extends Sheet {
	public LibraryLoadingSheet(RobonoboFrame frame) {
		super(frame);
		double[][] cellSizen = { {10, 300, 10}, {10, 50, 10} };
		setLayout(new TableLayout(cellSizen));
		setName("playback.background.panel");
		add(new RLabel14B("Please wait while the library loads..."), "1,1,CENTER,CENTER");
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
