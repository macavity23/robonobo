package com.robonobo.gui.preferences;

import info.clearthought.layout.TableLayout;

import java.awt.Dimension;

import javax.swing.ButtonGroup;

import com.robonobo.gui.components.base.*;
import com.robonobo.gui.frames.RobonoboFrame;

@SuppressWarnings("serial")
public class BoolPrefPanel extends PrefPanel {
	RRadioButton trueBut;
	RRadioButton falseBut;
	String propName;
	
	public BoolPrefPanel(RobonoboFrame frame, String propName, String description) {
		super(frame);
		this.propName = propName;
		double[][] cellSizen = { { 5, TableLayout.FILL, 140, 70, 5, 70, 5 }, { 25 } };
		setLayout(new TableLayout(cellSizen));
		RLabel descLbl = new RLabel12(description);
		add(descLbl, "1,0");
		trueBut = new RRadioButton("Yes");
		falseBut = new RRadioButton("No");
		ButtonGroup butGr = new ButtonGroup();
		butGr.add(trueBut);
		butGr.add(falseBut);
		resetValue();
		add(trueBut, "3,0");
		add(falseBut, "5,0");
		setMaximumSize(new Dimension(478, 27));
	}

	public void resetValue() {
		boolean propVal = Boolean.parseBoolean(getProperty(propName));
		if(propVal)
			trueBut.setSelected(true);
		else
			falseBut.setSelected(true);
	}
	
	@Override
	public void applyChanges() {
		setProperty(propName, String.valueOf(trueBut.isSelected()));
	}

	@Override
	public boolean hasChanged() {
		return !(trueBut.isSelected() == Boolean.parseBoolean(getProperty(propName)));
	}
}
