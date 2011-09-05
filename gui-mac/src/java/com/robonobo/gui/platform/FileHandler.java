package com.robonobo.gui.platform;

import com.apple.eawt.AppEvent.OpenFilesEvent;
import com.apple.eawt.OpenFilesHandler;
import com.robonobo.gui.frames.RobonoboFrame;

public class FileHandler implements OpenFilesHandler {
	public void openFiles(OpenFilesEvent e) {
		RobonoboFrame.getInstance().shareFilesOrDirectories(e.getFiles());
	}
}
