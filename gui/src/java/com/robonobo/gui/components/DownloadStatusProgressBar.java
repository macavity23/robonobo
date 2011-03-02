package com.robonobo.gui.components;

import java.awt.Color;

import javax.swing.JProgressBar;

import com.robonobo.gui.RoboFont;

public class DownloadStatusProgressBar extends JProgressBar {
	static final Color TEXT_COLOR = new Color(0x3c, 0x3c, 0x3c);
	
	public DownloadStatusProgressBar() {
		super(JProgressBar.HORIZONTAL);
		setFont(RoboFont.getFont(10, false));
		setForeground(TEXT_COLOR);
		setStringPainted(true);
	}
}
