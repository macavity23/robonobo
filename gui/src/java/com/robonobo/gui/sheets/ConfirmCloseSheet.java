package com.robonobo.gui.sheets;

import info.clearthought.layout.TableLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

import com.robonobo.gui.components.base.*;
import com.robonobo.gui.frames.RobonoboFrame;

public class ConfirmCloseSheet extends Sheet {
	private RButton exitBtn;

	public ConfirmCloseSheet(RobonoboFrame rFrame) {
		super(rFrame);
		double[][] cellSizen = { { 10, 200, 100, 10, 100, 10 }, {10, 20, 5, 45, 5, 25, 5, 30, 5} };
		setLayout(new TableLayout(cellSizen));
		setName("playback.background.panel");
		
		RLabel title = new RLabel14B("Please confirm exit");
		add(title, "1,1,4,1,CENTER,CENTER");
		
		RLabel blurb = new RLabel12("<html><center><p>Your friends will not be able to download the tracks on your playlists if nobody is sharing them.</p><p>Are you sure you want to close robonobo?</p></center></html>");
		add(blurb, "1,3,4,3");
		
		final RCheckBox feckOffCB = new RCheckBox("Don't show this screen on exit");
		feckOffCB.setSelected(false);
		add(feckOffCB, "1,5,4,5,CENTER,CENTER");
		
		exitBtn = new RGlassButton("EXIT");
		exitBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if(feckOffCB.isSelected()) {
					frame.getGuiConfig().setConfirmExit(false);
					frame.getController().saveConfig();
				}
				frame.shutdown();
			}
		});
		add(exitBtn, "2,7");
		RButton cancelBtn = new RRedGlassButton("CANCEL");
		cancelBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		add(cancelBtn, "4,7");
	}
	
	@Override
	public void onShow() {
		exitBtn.requestFocusInWindow();
	}
	
	@Override
	public JButton defaultButton() {
		return exitBtn;
	}
}
