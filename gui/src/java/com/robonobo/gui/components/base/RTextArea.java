package com.robonobo.gui.components.base;

import java.awt.*;

import javax.swing.JTextArea;
import javax.swing.text.Document;

import com.robonobo.gui.GuiUtil;
import com.robonobo.gui.RoboFont;

public class RTextArea extends JTextArea {
	private Color bgColor = null;

	public RTextArea() {
		super();
		setupFont();
	}

	public RTextArea(Document doc, String text, int rows, int columns) {
		super(doc, text, rows, columns);
		setupFont();
	}

	public RTextArea(Document doc) {
		super(doc);
		setupFont();
	}

	public RTextArea(int rows, int columns) {
		super(rows, columns);
		setupFont();
	}

	public RTextArea(String text, int rows, int columns) {
		super(text, rows, columns);
		setupFont();
	}

	public RTextArea(String text) {
		super(text);
		setupFont();
	}

	public void setBGColor(Color bgColor) {
		this.bgColor = bgColor;
	}

	protected void setupFont() {
		Font font = getRFont();
		if (font != null)
			setFont(font);
	}

	protected Font getRFont() {
		return RoboFont.getFont(12, false);
	}

	@Override
	protected void paintComponent(Graphics g) {
		GuiUtil.makeTextLookLessRubbish(g);
		if (bgColor != null) {
			g.setColor(bgColor);
			g.fillRect(0, 0, getWidth(), getHeight());
		}
		super.paintComponent(g);
	}
}
