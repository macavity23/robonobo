package com.robonobo.gui;

public class GuiConfig {
	private boolean showWelcomePanel = true;
	private boolean confirmExit = true;
	
	public GuiConfig() {
	}

	public boolean getShowWelcomePanel() {
		return showWelcomePanel;
	}

	public void setShowWelcomePanel(boolean showWelcomePanel) {
		this.showWelcomePanel = showWelcomePanel;
	}

	public boolean getConfirmExit() {
		return confirmExit;
	}

	public void setConfirmExit(boolean confirmExit) {
		this.confirmExit = confirmExit;
	}
}
