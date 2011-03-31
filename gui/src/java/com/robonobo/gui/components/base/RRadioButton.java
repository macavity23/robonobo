package com.robonobo.gui.components.base;

import java.awt.Font;
import java.awt.Graphics;

import javax.swing.*;

import com.robonobo.gui.GuiUtil;
import com.robonobo.gui.RoboFont;

public class RRadioButton extends JRadioButton {
	public RRadioButton() {
		super();
		setupFont();
	}

	public RRadioButton(Action a) {
		super(a);
		setupFont();
	}

	public RRadioButton(Icon icon, boolean selected) {
		super(icon, selected);
		setupFont();
	}

	public RRadioButton(Icon icon) {
		super(icon);
		setupFont();
	}

	public RRadioButton(String text, boolean selected) {
		super(text, selected);
		setupFont();
	}

	public RRadioButton(String text, Icon icon, boolean selected) {
		super(text, icon, selected);
		setupFont();
	}

	public RRadioButton(String text, Icon icon) {
		super(text, icon);
		setupFont();
	}

	public RRadioButton(String text) {
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
		GuiUtil.makeTextLookLessRubbish(g);
		super.paintComponent(g);
	}
	protected Font getRFont() {
		return RoboFont.getFont(13, true);
	}

}
