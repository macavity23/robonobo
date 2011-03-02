package com.robonobo.gui.components.base;

import java.awt.Font;

import javax.swing.Action;
import javax.swing.Icon;

import com.robonobo.gui.RoboFont;

public class RRadioButton11 extends RRadioButton {

	public RRadioButton11() {
		super();
		// TODO Auto-generated constructor stub
	}

	public RRadioButton11(Action a) {
		super(a);
		// TODO Auto-generated constructor stub
	}

	public RRadioButton11(Icon icon, boolean selected) {
		super(icon, selected);
		// TODO Auto-generated constructor stub
	}

	public RRadioButton11(Icon icon) {
		super(icon);
		// TODO Auto-generated constructor stub
	}

	public RRadioButton11(String text, boolean selected) {
		super(text, selected);
		// TODO Auto-generated constructor stub
	}

	public RRadioButton11(String text, Icon icon, boolean selected) {
		super(text, icon, selected);
		// TODO Auto-generated constructor stub
	}

	public RRadioButton11(String text, Icon icon) {
		super(text, icon);
		// TODO Auto-generated constructor stub
	}

	public RRadioButton11(String text) {
		super(text);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected Font getRFont() {
		return RoboFont.getFont(11, false);
	}
}
