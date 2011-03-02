package com.robonobo.gui.components.base;

import java.awt.Font;

import javax.swing.JPasswordField;
import javax.swing.text.Document;

import com.robonobo.gui.RoboFont;

public class RPasswordField extends JPasswordField {

	public RPasswordField() {
		super();
		setupFont();
	}

	public RPasswordField(Document doc, String text, int columns) {
		super(doc, text, columns);
		setupFont();
	}

	public RPasswordField(int columns) {
		super(columns);
		setupFont();
	}

	public RPasswordField(String text, int columns) {
		super(text, columns);
		setupFont();
	}

	public RPasswordField(String text) {
		super(text);
		setupFont();
	}
	
	protected void setupFont() {
		Font font = getRFont();
		if(font != null)	
			setFont(font);
	}

	protected Font getRFont() {
		return RoboFont.getFont(11, false);
	}

}
