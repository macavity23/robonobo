package com.robonobo.gui.sheets;

import info.clearthought.layout.TableLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import com.robonobo.gui.RoboFont;
import com.robonobo.gui.components.base.*;
import com.robonobo.gui.frames.RobonoboFrame;

public abstract class ConfirmWithFeckOffSheet extends Sheet {
	RButton confirmBtn;
	RButton cancelBtn;
	RCheckBox feckOffCB;

	public ConfirmWithFeckOffSheet(RobonoboFrame frame, String title, String message, String feckOffLbl, boolean feckOffSelected, String confirmBtnLbl) {
		super(frame);
		confirmBtn = new RGlassButton(confirmBtnLbl.toUpperCase());
		int btnWidth = getFontMetrics(confirmBtn.getFont()).stringWidth(confirmBtnLbl.toUpperCase()) + 50;
		double[][] cellSizen = { { 10, 200, btnWidth, 10, 100, 10 }, { 10, 20, 5, 50, 5, 25, 5, 30, 5 } };
		setLayout(new TableLayout(cellSizen));
		setName("playback.background.panel");
		RLabel titleLbl = new RLabel14B(title);
		add(titleLbl, "1,1,4,1,CENTER,CENTER");
		JPanel msgLbl = new LineBreakTextPanel(message, RoboFont.getFont(13, false), 310 + btnWidth);
		add(msgLbl, "1,3,4,3");
		feckOffCB = new RCheckBox(feckOffLbl);
		feckOffCB.setSelected(feckOffSelected);
		add(feckOffCB, "1,5,4,5,LEFT,CENTER");
		confirmBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				confirmed(feckOffCB.isSelected());
			}
		});
		add(confirmBtn, "2,7");
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
		confirmBtn.requestFocusInWindow();
	}

	@Override
	public JButton defaultButton() {
		return confirmBtn;
	}

	/** Will be called on UI thread, be speedy! */
	protected abstract void confirmed(boolean feckOffSelected);
}
