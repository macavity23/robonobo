package com.robonobo.gui.components.base;

import java.awt.Font;

import javax.swing.Action;
import javax.swing.Icon;

import com.robonobo.gui.RoboFont;

public class RRadioButton11B extends RRadioButton {

	public RRadioButton11B() {
		super();
		// TODO Auto-generated constructor stub
	}

	public RRadioButton11B(Action a) {
		super(a);
		// TODO Auto-generated constructor stub
	}

	public RRadioButton11B(Icon icon, boolean selected) {
		super(icon, selected);
		// TODO Auto-generated constructor stub
	}

	public RRadioButton11B(Icon icon) {
		super(icon);
		// TODO Auto-generated constructor stub
	}

	public RRadioButton11B(String text, boolean selected) {
		super(text, selected);
		// TODO Auto-generated constructor stub
	}

	public RRadioButton11B(String text, Icon icon, boolean selected) {
		super(text, icon, selected);
		// TODO Auto-generated constructor stub
	}

	public RRadioButton11B(String text, Icon icon) {
		super(text, icon);
		// TODO Auto-generated constructor stub
	}

	public RRadioButton11B(String text) {
		super(text);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected Font getRFont() {
		return RoboFont.getFont(11, true);
	}
}
