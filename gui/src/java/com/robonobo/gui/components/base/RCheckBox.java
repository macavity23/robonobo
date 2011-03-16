package com.robonobo.gui.components.base;

import java.awt.Font;
import java.awt.Graphics;

import javax.swing.*;

import com.robonobo.gui.GUIUtil;
import com.robonobo.gui.RoboFont;

public class RCheckBox extends JCheckBox {
	
	public RCheckBox() {
		super();
		setupFont();
	}

	public RCheckBox(Action a) {
		super(a);
		setupFont();
	}

	public RCheckBox(Icon icon, boolean selected) {
		super(icon, selected);
		setupFont();
	}

	public RCheckBox(Icon icon) {
		super(icon);
		setupFont();
	}

	public RCheckBox(String text, boolean selected) {
		super(text, selected);
		setupFont();
	}

	public RCheckBox(String text, Icon icon, boolean selected) {
		super(text, icon, selected);
		setupFont();
	}

	public RCheckBox(String text, Icon icon) {
		super(text, icon);
		setupFont();
	}

	public RCheckBox(String text) {
		super(text);
		setupFont();
	}

	protected void setupFont() {
		Font font = getRFont();
		if(font != null)	
			setFont(font);
	}

	@Override
	protected void paintComponent(Graphics g) {
		GUIUtil.makeTextLookLessRubbish(g);
		super.paintComponent(g);
	}
	protected Font getRFont() {
		return RoboFont.getFont(12, true);
	}
}
