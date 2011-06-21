package com.robonobo.gui;

public class GuiConfig {
	private boolean showWelcomePanel = true;
	private boolean confirmExit = true;
	/** After a pFetcher finishes, it will be removed from the pFetcher list after this many seconds */
	private int zombieTaskLifetime = 300;
	
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

	public int getZombieTaskLifetime() {
		return zombieTaskLifetime;
	}

	public void setZombieTaskLifetime(int zombieTaskLifetime) {
		this.zombieTaskLifetime = zombieTaskLifetime;
	}
}
