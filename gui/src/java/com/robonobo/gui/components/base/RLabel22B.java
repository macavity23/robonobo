package com.robonobo.gui.components.base;

import java.awt.Font;

import javax.swing.Icon;

import com.robonobo.gui.RoboFont;

public class RLabel22B extends RLabel {

	public RLabel22B() {
		super();
		// TODO Auto-generated constructor stub
	}

	public RLabel22B(Icon image, int horizontalAlignment) {
		super(image, horizontalAlignment);
		// TODO Auto-generated constructor stub
	}

	public RLabel22B(Icon image) {
		super(image);
		// TODO Auto-generated constructor stub
	}

	public RLabel22B(String text, Icon icon, int horizontalAlignment) {
		super(text, icon, horizontalAlignment);
		// TODO Auto-generated constructor stub
	}

	public RLabel22B(String text, int horizontalAlignment) {
		super(text, horizontalAlignment);
		// TODO Auto-generated constructor stub
	}

	public RLabel22B(String text) {
		super(text);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected Font getRFont() {
		return RoboFont.getFont(22, true);
	}

}
