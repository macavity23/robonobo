package com.robonobo.gui.platform;

import com.apple.eawt.ApplicationEvent;
import com.apple.eawt.ApplicationListener;
import com.robonobo.gui.frames.RobonoboFrame;

public class MacAppListener implements ApplicationListener {
	public MacAppListener() {
	}
	
	public void handleAbout(ApplicationEvent e) {
		RobonoboFrame.getInstance().showAbout();
		e.setHandled(true);
	}

	public void handleOpenApplication(ApplicationEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void handleOpenFile(ApplicationEvent e) {
	}

	public void handlePreferences(ApplicationEvent e) {
		RobonoboFrame.getInstance().showPreferences();
		e.setHandled(true);
	}

	public void handlePrintFile(ApplicationEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void handleQuit(ApplicationEvent e) {
		RobonoboFrame.getInstance().confirmThenShutdown();
	}

	public void handleReOpenApplication(ApplicationEvent arg0) {
		// TODO Auto-generated method stub

	}

	
}
