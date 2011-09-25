package com.robonobo.gui.sheets;

import info.clearthought.layout.TableLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JPanel;

import com.robonobo.gui.RoboFont;
import com.robonobo.gui.components.base.*;
import com.robonobo.gui.frames.RobonoboFrame;

@SuppressWarnings("serial")
public class InfoSheet extends Sheet {
	RButton okBtn;

	public InfoSheet(RobonoboFrame f, String title, String message) {
		super(f);
		setName("playback.background.panel");
		okBtn = new RGlassButton("OK");
		int btnWidth = okBtn.getPreferredSize().width;
		JPanel msgLbl = new LineBreakTextPanel(message, RoboFont.getFont(13, false), 400 + btnWidth);
		int msgHeight = msgLbl.getPreferredSize().height;
		double[][] cellSizen = { { 10, 200, btnWidth, 10 }, { 10, 20, 5, msgHeight, 10, 30, 10 } };
		setLayout(new TableLayout(cellSizen));
		RLabel titleLbl = new RLabel14B(title);
		add(titleLbl,"1,1,2,1,CENTER,CENTER");
		add(msgLbl, "1,3,2,3");
		okBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				setVisible(false);
			}
		});
		add(okBtn, "2,5");
	}

	@Override
	public void onShow() {
		okBtn.requestFocusInWindow();
	}

	@Override
	public JButton defaultButton() {
		return okBtn;
	}
}
