package com.robonobo.gui.components.base;

import java.awt.Font;
import java.awt.Graphics;

import javax.swing.BoundedRangeModel;
import javax.swing.JProgressBar;

import com.robonobo.gui.GUIUtil;
import com.robonobo.gui.RoboFont;

public class RProgressBar extends JProgressBar {
	public RProgressBar() {
		super();
		setupFont();
	}

	public RProgressBar(BoundedRangeModel newModel) {
		super(newModel);
		setupFont();
	}

	public RProgressBar(int orient, int min, int max) {
		super(orient, min, max);
		setupFont();
	}

	public RProgressBar(int min, int max) {
		super(min, max);
		setupFont();
	}

	public RProgressBar(int orient) {
		super(orient);
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
		return RoboFont.getFont(11, false);
	}

}
