package com.robonobo.gui.components.base;

import java.awt.Font;

import javax.swing.*;

public abstract class RButton extends JButton {

	public RButton() {
		super();
		setupFont();
	}

	public RButton(Action a) {
		super(a);
		setupFont();
	}

	public RButton(Icon icon) {
		super(icon);
		setupFont();
	}

	public RButton(String text, Icon icon) {
		super(text, icon);
		setupFont();
	}

	public RButton(String text) {
		super(text);
		setupFont();
	}

	private void setupFont() {
		Font font = getRFont();
		if(font != null)
			setFont(font);
	}
	
	protected abstract Font getRFont();

}
