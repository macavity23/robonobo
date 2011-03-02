package com.robonobo.gui.panels;

import com.robonobo.core.api.model.Library;
import com.robonobo.gui.frames.RobonoboFrame;
import com.robonobo.gui.model.FriendLibraryTableModel;

public class FriendLibraryContentPanel extends ContentPanel {
	public FriendLibraryContentPanel(RobonoboFrame frame, Library lib) {
		super(frame, new FriendLibraryTableModel(frame.getController(), lib));
		// TODO Any options for friend library?
	}
}
