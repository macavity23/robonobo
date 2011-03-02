package com.robonobo.gui.components.base;

import java.awt.Font;

import javax.swing.Icon;
import javax.swing.JLabel;

import com.robonobo.gui.RoboFont;

public class RLabel11 extends RLabel {
	public RLabel11() {
		super();
	}

	public RLabel11(String text, Icon icon, int horizontalAlignment) {
		super(text, icon, horizontalAlignment);
	}

	public RLabel11(String text, int horizontalAlignment) {
		super(text, horizontalAlignment);
	}

	public RLabel11(String text) {
		super(text);
	}
	
	@Override
	protected Font getRFont() {
		return RoboFont.getFont(11, false);
	}
}
