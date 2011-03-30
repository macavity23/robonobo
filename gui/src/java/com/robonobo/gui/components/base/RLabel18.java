package com.robonobo.gui.components.base;

import java.awt.Font;

import javax.swing.Icon;

import com.robonobo.gui.RoboFont;

public class RLabel18 extends RLabel {

	public RLabel18() {
		// TODO Auto-generated constructor stub
	}

	public RLabel18(Icon image, int horizontalAlignment) {
		super(image, horizontalAlignment);
		// TODO Auto-generated constructor stub
	}

	public RLabel18(Icon image) {
		super(image);
		// TODO Auto-generated constructor stub
	}

	public RLabel18(String text, Icon icon, int horizontalAlignment) {
		super(text, icon, horizontalAlignment);
		// TODO Auto-generated constructor stub
	}

	public RLabel18(String text, int horizontalAlignment) {
		super(text, horizontalAlignment);
		// TODO Auto-generated constructor stub
	}

	public RLabel18(String text) {
		super(text);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected Font getRFont() {
		return RoboFont.getFont(18, false);
	}

}
