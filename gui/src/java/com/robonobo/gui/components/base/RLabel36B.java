package com.robonobo.gui.components.base;

import java.awt.Font;

import com.robonobo.gui.RoboFont;

public class RLabel36B extends RLabel {
	
	public RLabel36B(String text) {
		super(text);
	}

	@Override
	protected Font getRFont() {
		return RoboFont.getFont(36, true);
	}
}
