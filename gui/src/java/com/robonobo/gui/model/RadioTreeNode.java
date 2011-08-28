package com.robonobo.gui.model;

import com.robonobo.core.api.model.Playlist;
import com.robonobo.gui.frames.RobonoboFrame;

public class RadioTreeNode extends PlaylistTreeNode {

	public RadioTreeNode(Playlist p, RobonoboFrame frame) {
		super(p, frame);
	}
	
	protected int getSpecialIndex() {
		return 2;
	}
}
