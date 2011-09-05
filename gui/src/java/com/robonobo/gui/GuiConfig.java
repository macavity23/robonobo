package com.robonobo.gui;

public class GuiConfig {
	private boolean showWelcomePanel = true;
	private boolean confirmExit = true;
	private boolean confirmTrackDelete = true;
	private boolean showNewPlaylistDesc = true;
	private boolean showLovesDesc = true;
	private boolean showRadioDesc = true;
	
	/** After a task finishes, it will be removed from the task list after this many seconds */
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

	public boolean getConfirmTrackDelete() {
		return confirmTrackDelete;
	}

	public void setConfirmTrackDelete(boolean confirmTrackDelete) {
		this.confirmTrackDelete = confirmTrackDelete;
	}

	public boolean getShowNewPlaylistDesc() {
		return showNewPlaylistDesc;
	}

	public void setShowNewPlaylistDesc(boolean showNewPlaylistDesc) {
		this.showNewPlaylistDesc = showNewPlaylistDesc;
	}

	public boolean getShowLovesDesc() {
		return showLovesDesc;
	}

	public void setShowLovesDesc(boolean showLovesDesc) {
		this.showLovesDesc = showLovesDesc;
	}

	public boolean getShowRadioDesc() {
		return showRadioDesc;
	}

	public void setShowRadioDesc(boolean showRadioDesc) {
		this.showRadioDesc = showRadioDesc;
	}
}
