package com.robonobo.gui.sheets;

import javax.swing.JButton;
import javax.swing.JPanel;

import com.robonobo.gui.frames.RobonoboFrame;

@SuppressWarnings("serial")
public abstract class Sheet extends JPanel {
	protected RobonoboFrame frame;

	public Sheet(RobonoboFrame frame) {
		this.frame = frame;
	}
	
	/** Called after sheet is shown, on the gui thread */
	public abstract void onShow();
	
	/** This will be made the default button when the sheet is shown */
	public abstract JButton defaultButton();
	
	/** Called by the frame when we should go away. Make sure to call super.hideSheet() if you override this! */
	public void hideSheet() {
		super.setVisible(false);
	}
	
	@Override
	public void setVisible(boolean visible) {
		super.setVisible(visible);
		if (!visible)
			frame.discardTopSheet();
	}
}
