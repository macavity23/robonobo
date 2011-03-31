package com.robonobo.gui.components.base;

import java.awt.Font;
import java.awt.Graphics;

import javax.swing.JTextField;
import javax.swing.text.Document;

import com.robonobo.gui.GuiUtil;
import com.robonobo.gui.RoboFont;

public class RTextField extends JTextField {

	public RTextField() {
		super();
		setupFont();
	}

	public RTextField(Document doc, String text, int columns) {
		super(doc, text, columns);
		setupFont();
	}

	public RTextField(int columns) {
		super(columns);
		setupFont();
	}

	public RTextField(String text, int columns) {
		super(text, columns);
		setupFont();
	}

	public RTextField(String text) {
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
		return RoboFont.getFont(12, false);
	}
}
