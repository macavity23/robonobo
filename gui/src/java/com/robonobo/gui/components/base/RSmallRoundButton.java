package com.robonobo.gui.components.base;

import java.awt.Font;

import javax.swing.Action;
import javax.swing.Icon;

import com.robonobo.gui.RoboFont;

public class RSmallRoundButton extends RButton {

	public RSmallRoundButton() {
		super();
		// TODO Auto-generated constructor stub
	}

	public RSmallRoundButton(Action a) {
		super(a);
		// TODO Auto-generated constructor stub
	}

	public RSmallRoundButton(Icon icon) {
		super(icon);
		// TODO Auto-generated constructor stub
	}

	public RSmallRoundButton(String text, Icon icon) {
		super(text, icon);
		// TODO Auto-generated constructor stub
	}

	public RSmallRoundButton(String text) {
		super(text);
		// TODO Auto-generated constructor stub
	}

	@Override
	protected Font getRFont() {
		setName("robonobo.small.round.button");
		return RoboFont.getFont(11, false);
	}

}
