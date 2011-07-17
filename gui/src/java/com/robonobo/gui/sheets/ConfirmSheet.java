package com.robonobo.gui.sheets;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import info.clearthought.layout.TableLayout;

import javax.swing.JButton;
import javax.swing.JPanel;

import com.robonobo.gui.RoboFont;
import com.robonobo.gui.components.base.*;
import com.robonobo.gui.frames.RobonoboFrame;

@SuppressWarnings("serial")
public class ConfirmSheet extends Sheet {
	RButton confirmBtn;
	RButton cancelBtn;

	public ConfirmSheet(RobonoboFrame f, String title, String message, String confirmBtnLbl, final Runnable runOnUiThread) {
		super(f);
		setName("playback.background.panel");
		confirmBtn = new RGlassButton(confirmBtnLbl.toUpperCase());
		int btnWidth = getFontMetrics(confirmBtn.getFont()).stringWidth(confirmBtnLbl.toUpperCase()) + 50;
		double[][] cellSizen = { { 10, 30, btnWidth, 10, 100, 10 }, { 10, 20, 5, 50, 10, 30, 10 } };
		setLayout(new TableLayout(cellSizen));
		RLabel titleLbl = new RLabel14B(title);
		add(titleLbl,"1,1,4,1,CENTER,CENTER");
		JPanel msgLbl = new LineBreakTextPanel(message, RoboFont.getFont(13, false), new Dimension((400 + btnWidth), 200));
		add(msgLbl, "1,3,4,3");
		confirmBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent evt) {
				setVisible(false);
				runOnUiThread.run();
			}
		});
		add(confirmBtn, "2,5");
		cancelBtn = new RRedGlassButton("CANCEL");
		cancelBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
			}
		});
		add(cancelBtn, "4,5");
	}

	@Override
	public void onShow() {
		confirmBtn.requestFocusInWindow();
	}

	@Override
	public JButton defaultButton() {
		return confirmBtn;
	}
}
