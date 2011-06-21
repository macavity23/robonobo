package com.robonobo.gui.preferences;

import info.clearthought.layout.TableLayout;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JTextField;

import com.robonobo.core.api.config.RobonoboConfig;
import com.robonobo.gui.components.base.*;
import com.robonobo.gui.frames.RobonoboFrame;

@SuppressWarnings("serial")
public class GatewayPrefPanel extends PrefPanel {
	RRadioButton autoBut, neverBut, manualBut;
	JTextField manualPort;
	RobonoboConfig roboCfg;
	
	public GatewayPrefPanel(RobonoboFrame frame) {
		super(frame);
		double[][] cellSizen = { { 5, TableLayout.FILL, 5, 200, 5, 45, 5 }, { 25, 5, 25, 5, 25 } };
		setLayout(new TableLayout(cellSizen));
		RLabel ipLbl = new RLabel12("Router IP address and port");
		add(ipLbl, "1,0");

		roboCfg = frame.getController().getConfig();
		ButtonGroup butGr = new ButtonGroup();
		ActionListener radLis = new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				manualPort.setEnabled(manualBut.isSelected());
			}
		};
		
		autoBut = new RRadioButton("Automatically detect");
		autoBut.addActionListener(radLis);
		butGr.add(autoBut);
		add(autoBut, "3,0,5,0");
		
		neverBut = new RRadioButton("Disable (reduces performance)");
		neverBut.addActionListener(radLis);
		butGr.add(neverBut);
		add(neverBut, "3,2,5,2");
		
		manualBut = new RRadioButton("Manual: use router port");
		manualBut.addActionListener(radLis);
		butGr.add(manualBut);
		add(manualBut, "3,4");
		manualPort = new RIntegerTextField(null, false);
		add(manualPort, "5,4");
		
		resetValue();
		setMaximumSize(new Dimension(478, 87));
	}

	public void resetValue() {
		manualPort.setText(null);
		String cfgMode = roboCfg.getGatewayCfgMode();
		if(cfgMode.equals("auto"))
			autoBut.doClick();
		else if(cfgMode.equals("off"))
			neverBut.doClick();
		else {
			manualBut.doClick();
			int port = Integer.parseInt(cfgMode);
			manualPort.setText(String.valueOf(port));
		}
	}
	
	@Override
	public void applyChanges() {
		roboCfg.setGatewayCfgMode(getCfgMode());
	}

	@Override
	public boolean hasChanged() {
		return !(roboCfg.getGatewayCfgMode().equals(getCfgMode()));
	}

	private String getCfgMode() {
		if(autoBut.isSelected())
			return "auto";
		if(neverBut.isSelected())
			return "off";
		return String.valueOf(manualPort.getText());
	}
}
