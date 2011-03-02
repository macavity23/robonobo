package com.robonobo.gui.platform;

import com.apple.eawt.ApplicationEvent;
import com.apple.eawt.ApplicationListener;
import com.robonobo.gui.frames.RobonoboFrame;

public class MacAppListener implements ApplicationListener {
	private RobonoboFrame frame;
		
	public MacAppListener(RobonoboFrame frame) {
		this.frame = frame;
	}

	public void handleAbout(ApplicationEvent e) {
		frame.showAbout();
		e.setHandled(true);
	}

	public void handleOpenApplication(ApplicationEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void handleOpenFile(ApplicationEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void handlePreferences(ApplicationEvent e) {
		frame.showPreferences();
		e.setHandled(true);
	}

	public void handlePrintFile(ApplicationEvent arg0) {
		// TODO Auto-generated method stub

	}

	public void handleQuit(ApplicationEvent e) {
		frame.confirmThenShutdown();
	}

	public void handleReOpenApplication(ApplicationEvent arg0) {
		// TODO Auto-generated method stub

	}

	
}
