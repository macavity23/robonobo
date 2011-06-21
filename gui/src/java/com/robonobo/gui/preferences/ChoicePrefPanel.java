package com.robonobo.gui.preferences;

import info.clearthought.layout.TableLayout;

import java.awt.Dimension;

import com.robonobo.common.exceptions.Errot;
import com.robonobo.gui.components.base.*;
import com.robonobo.gui.frames.RobonoboFrame;

@SuppressWarnings("serial")
public class ChoicePrefPanel extends PrefPanel {
	String propName;
	String[] choices;
	RComboBox combo;
	
	public ChoicePrefPanel(RobonoboFrame frame, String propName, String description, String[] choices) {
		super(frame);
		double[][] cellSizen = { { 5, TableLayout.FILL, 5, 230, 5 }, { 25 } };
		setLayout(new TableLayout(cellSizen));
		this.propName = propName;
		this.choices = choices;
		RLabel descLbl = new RLabel12(description);
		add(descLbl, "1,0");
		combo = new RComboBox(choices);
		combo.setEditable(false);
		resetValue();
		add(combo, "3,0");
		setMaximumSize(new Dimension(478, 27));
	}

	public void resetValue() {
		String propVal = getProperty(propName);
		int selIndex = -1;
		for (int i = 0; i < choices.length; i++) {
			if(choices[i].equals(propVal)) {
				selIndex = i;
				break;
			}
		}
		if(selIndex < 0)
			throw new Errot("Invalid prop value for property "+propName);
		combo.setSelectedIndex(selIndex);
	}
	
	@Override
	public void applyChanges() {
		setProperty(propName, (String) combo.getSelectedItem());
	}

	@Override
	public boolean hasChanged() {
		return !(combo.getSelectedItem().equals(getProperty(propName)));
	}

}
