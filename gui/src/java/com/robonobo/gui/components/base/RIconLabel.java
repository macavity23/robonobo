package com.robonobo.gui.components.base;

import java.awt.Font;

import javax.swing.Icon;

public class RIconLabel extends RLabel {

	public RIconLabel() {
		super();
	}

	public RIconLabel(Icon image, int horizontalAlignment) {
		super(image, horizontalAlignment);
		// TODO Auto-generated constructor stub
	}

	public RIconLabel(Icon image) {
		super(image);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected Font getRFont() {
		return null;
	}
}
