package com.robonobo.gui.components.base;

import java.awt.Font;

import javax.swing.Action;
import javax.swing.Icon;

public class RRedGlassButton extends RGlassButton {

	
	public RRedGlassButton() {
		super();
		// TODO Auto-generated constructor stub
	}

	public RRedGlassButton(Action a) {
		super(a);
		// TODO Auto-generated constructor stub
	}

	public RRedGlassButton(Icon icon) {
		super(icon);
		// TODO Auto-generated constructor stub
	}

	public RRedGlassButton(String text, Icon icon) {
		super(text, icon);
		// TODO Auto-generated constructor stub
	}

	public RRedGlassButton(String text) {
		super(text);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected Font getRFont() {
		setName("robonobo.red.button");
		return super.getRFont();
	}
}
