package com.robonobo.gui.preferences;

import info.clearthought.layout.TableLayout;

import java.awt.Dimension;

import com.robonobo.gui.RoboFont;
import com.robonobo.gui.components.base.*;
import com.robonobo.gui.frames.RobonoboFrame;

@SuppressWarnings("serial")
public class IntPrefPanel extends PrefPanel {
	RTextField textField;
	String propName;
	
	public IntPrefPanel(RobonoboFrame frame, String propName, String description, boolean allowNegative) {
		super(frame);
		double[][] cellSizen = { { 5, TableLayout.FILL, 185, 50, 5 }, { 25 } };
		setLayout(new TableLayout(cellSizen));
		RLabel descLbl = new RLabel12(description);
		add(descLbl, "1,0");
		textField = new RIntegerTextField(Integer.parseInt(getProperty(propName)), allowNegative);
		textField.setFont(RoboFont.getFont(11, false));
		add(textField, "3,0");
		this.propName = propName;
		setMaximumSize(new Dimension(478, 27));
	}
	
	@Override
	public void applyChanges() {
		String text = textField.getText();
		Integer.parseInt(text);
		setProperty(propName, text);
	}

	@Override
	public boolean hasChanged() {
		return !(textField.getText().equals(getProperty(propName)));
	}	
	
	@Override
	public void resetValue() {
		textField.setText(getProperty(propName));
	}
}
