package com.robonobo.gui.components.base;

import java.awt.*;

import javax.swing.JTextPane;
import javax.swing.text.StyledDocument;

import com.robonobo.gui.GUIUtil;
import com.robonobo.gui.RoboFont;

public class RTextPane extends JTextPane {
	private Color bgColor = null;

	public RTextPane() {
		super();
		setupFont();
	}

	public RTextPane(StyledDocument doc) {
		super(doc);
		setupFont();
	}

	protected void setupFont() {
		Font font = getRFont();
		if(font != null)	
			setFont(font);
	}

	protected Font getRFont() {
		return RoboFont.getFont(12, false);
	}

	public void setBGColor(Color bgColor) {
		this.bgColor = bgColor;
	}

	@Override
	protected void paintComponent(Graphics g) {
		GUIUtil.makeTextLookLessRubbish(g);
		if (bgColor != null) {
			g.setColor(bgColor);
			g.fillRect(0, 0, getWidth(), getHeight());
		}
		super.paintComponent(g);
	}
}
