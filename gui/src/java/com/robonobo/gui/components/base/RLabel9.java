package com.robonobo.gui.components.base;

import java.awt.Font;

import javax.swing.Icon;

import com.robonobo.gui.RoboFont;

public class RLabel9 extends RLabel {

	public RLabel9() {
		super();
		// TODO Auto-generated constructor stub
	}

	public RLabel9(Icon image, int horizontalAlignment) {
		super(image, horizontalAlignment);
		// TODO Auto-generated constructor stub
	}

	public RLabel9(Icon image) {
		super(image);
		// TODO Auto-generated constructor stub
	}

	public RLabel9(String text, Icon icon, int horizontalAlignment) {
		super(text, icon, horizontalAlignment);
		// TODO Auto-generated constructor stub
	}

	public RLabel9(String text, int horizontalAlignment) {
		super(text, horizontalAlignment);
		// TODO Auto-generated constructor stub
	}

	public RLabel9(String text) {
		super(text);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected Font getRFont() {
		return RoboFont.getFont(9, false);
	}

}
