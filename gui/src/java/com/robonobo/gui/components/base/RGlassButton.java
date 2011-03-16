package com.robonobo.gui.components.base;

import java.awt.Font;

import javax.swing.Action;
import javax.swing.Icon;

import com.robonobo.gui.RoboFont;

public class RGlassButton extends RButton {

	public RGlassButton() {
		super();
		// TODO Auto-generated constructor stub
	}

	public RGlassButton(Action a) {
		super(a);
		// TODO Auto-generated constructor stub
	}

	public RGlassButton(Icon icon) {
		super(icon);
		// TODO Auto-generated constructor stub
	}

	public RGlassButton(String text, Icon icon) {
		super(text, icon);
		// TODO Auto-generated constructor stub
	}

	public RGlassButton(String text) {
		super(text);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected Font getRFont() {
		return RoboFont.getFont(13, true);
	}
}
