package com.robonobo.gui.components.base;

import java.awt.Font;

import javax.swing.Action;
import javax.swing.Icon;

import com.robonobo.gui.RoboFont;

public class RBoldMenuItem extends RMenuItem {

	public RBoldMenuItem() {
		super();
		// TODO Auto-generated constructor stub
	}

	public RBoldMenuItem(Action a) {
		super(a);
		// TODO Auto-generated constructor stub
	}

	public RBoldMenuItem(Icon icon) {
		super(icon);
		// TODO Auto-generated constructor stub
	}

	public RBoldMenuItem(String text, Icon icon) {
		super(text, icon);
		// TODO Auto-generated constructor stub
	}

	public RBoldMenuItem(String text, int mnemonic) {
		super(text, mnemonic);
		// TODO Auto-generated constructor stub
	}

	public RBoldMenuItem(String text) {
		super(text);
		// TODO Auto-generated constructor stub
	}
	
	@Override
	protected Font getRFont() {
		return RoboFont.getFont(super.getRFont().getSize(), true);
	}
}
