package com.robonobo.gui.preferences;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import info.clearthought.layout.TableLayout;

import javax.swing.ButtonGroup;

import com.robonobo.gui.components.base.*;
import com.robonobo.gui.frames.RobonoboFrame;
import com.robonobo.mina.external.MinaConfig;

@SuppressWarnings("serial")
public class UploadLimitPrefPanel extends PrefPanel {
	RRadioButton unlimBtn, limBtn;
	RIntegerTextField limField;
	MinaConfig minaCfg;
	
	public UploadLimitPrefPanel(RobonoboFrame frame) {
		super(frame);
		minaCfg = (MinaConfig) frame.getController().getConfig("mina");
		double[][] cellSizen = { { 5, TableLayout.FILL, 120, 30, 50, 30, 5}, { 25 } };
		setLayout(new TableLayout(cellSizen));
		ActionListener as = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(limBtn.isSelected()) {
					if(limField.getIntValue() == null)
						limField.setText("50");
					limField.setEnabled(true);
				} else
					limField.setEnabled(false);
			}
		};
		add(new RLabel12("Upload speed"), "1,0");
		ButtonGroup bg = new ButtonGroup();
		unlimBtn = new RRadioButton11B("Unlimited");
		unlimBtn.addActionListener(as);
		bg.add(unlimBtn);
		add(unlimBtn, "2,0");
		limBtn = new RRadioButton();
		limBtn.addActionListener(as);
		bg.add(limBtn);
		add(limBtn, "3,0");
		limField = new RIntegerTextField(null, false);
		add(limField, "4,0");
		add(new RLabel11B("KB/s"), "5,0");
		resetValue();
	}

	@Override
	public boolean hasChanged() {
		return (minaCfg.getMaxOutboundBps() != getUploadLim());
	}

	@Override
	public void applyChanges() {
		minaCfg.setMaxOutboundBps(getUploadLim());
	}

	@Override
	public void resetValue() {
		int lim = minaCfg.getMaxOutboundBps();
		if(lim >= 0) {
			limBtn.doClick();
			limField.setText(String.valueOf(lim));
		} else
			unlimBtn.doClick();
	}

	private int getUploadLim() {
		if(unlimBtn.isSelected() || limField.getIntValue() == null)
			return -1;
		return limField.getIntValue() * 1024;
	}
}
