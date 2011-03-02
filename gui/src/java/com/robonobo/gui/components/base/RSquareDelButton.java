package com.robonobo.gui.components.base;

import java.awt.Font;

import javax.swing.Action;
import javax.swing.Icon;

public class RSquareDelButton extends RButton {

	public RSquareDelButton() {
		super();
		// TODO Auto-generated constructor stub
	}

	public RSquareDelButton(Action a) {
		super(a);
		// TODO Auto-generated constructor stub
	}

	public RSquareDelButton(Icon icon) {
		super(icon);
		// TODO Auto-generated constructor stub
	}

	public RSquareDelButton(String text, Icon icon) {
		super(text, icon);
		// TODO Auto-generated constructor stub
	}

	public RSquareDelButton(String text) {
		super(text);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected Font getRFont() {
		setName("robonobo.exit.button");
		return null;
	}

}
