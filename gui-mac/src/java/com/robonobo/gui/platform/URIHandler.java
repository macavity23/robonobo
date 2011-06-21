package com.robonobo.gui.platform;

import com.apple.eawt.AppEvent.OpenURIEvent;
import com.apple.eawt.OpenURIHandler;
import com.robonobo.gui.frames.RobonoboFrame;

public class URIHandler implements OpenURIHandler {
	public URIHandler() {
	}
	public void openURI(OpenURIEvent e) {
		String uriStr = e.getURI().toString();
		RobonoboFrame.getInstance().openRbnbUri(uriStr);
	}
}
