package com.robonobo.gui.preferences;

import info.clearthought.layout.TableLayout;

import java.awt.Dimension;

import com.robonobo.gui.components.base.*;
import com.robonobo.gui.frames.RobonoboFrame;


@SuppressWarnings("serial")
public class StringPrefPanel extends PrefPanel {
	RTextField textField;
	String propName;
	
	public StringPrefPanel(RobonoboFrame frame, String propName, String description) {
		super(frame);
		double[][] cellSizen = { { 5, TableLayout.FILL, 5, 230, 5 }, { 25 } };
		setLayout(new TableLayout(cellSizen));
		RLabel descLbl = new RLabel12(description);
		add(descLbl, "1,0");
		textField = new RTextField(getProperty(propName));
		add(textField, "3,0");
		this.propName = propName;
		setMaximumSize(new Dimension(478, 27));
	}
	
	@Override
	public void applyChanges() {
		setProperty(propName, textField.getText());
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
