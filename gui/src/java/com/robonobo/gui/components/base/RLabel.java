package com.robonobo.gui.components.base;

import java.awt.Font;
import java.awt.Graphics;

import javax.swing.Icon;
import javax.swing.JLabel;

import com.robonobo.gui.GUIUtil;

public abstract class RLabel extends JLabel {

	public RLabel() {
		super();
		setupFont();
	}

	public RLabel(Icon image, int horizontalAlignment) {
		super(image, horizontalAlignment);
		setupFont();
	}

	public RLabel(Icon image) {
		super(image);
		setupFont();
	}

	public RLabel(String text, Icon icon, int horizontalAlignment) {
		super(text, icon, horizontalAlignment);
		setupFont();
	}

	public RLabel(String text, int horizontalAlignment) {
		super(text, horizontalAlignment);
		setupFont();
	}

	public RLabel(String text) {
		super(text);
		setupFont();
	}

	private void setupFont() {
		Font font = getRFont();
		if(font != null)
			setFont(font);
	}
	
	protected abstract Font getRFont();
	
	@Override
	protected void paintComponent(Graphics g) {
		GUIUtil.makeTextLookLessRubbish(g);
		super.paintComponent(g);
	}
}
