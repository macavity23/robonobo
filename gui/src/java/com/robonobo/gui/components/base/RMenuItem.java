package com.robonobo.gui.components.base;

import java.awt.Font;

import javax.swing.*;

import com.robonobo.gui.RoboFont;

public class RMenuItem extends JMenuItem {
	public RMenuItem() {
		super();
		setupFont();
	}

	public RMenuItem(Action a) {
		super(a);
		setupFont();
	}

	public RMenuItem(Icon icon) {
		super(icon);
		setupFont();
	}

	public RMenuItem(String text, Icon icon) {
		super(text, icon);
		setupFont();
	}

	public RMenuItem(String text, int mnemonic) {
		super(text, mnemonic);
		setupFont();
	}

	public RMenuItem(String text) {
		super(text);
		setupFont();
	}

	private void setupFont() {
		setFont(getRFont());
	}
	
	protected Font getRFont() {
		return RoboFont.getFont(13, false);
	}

}
