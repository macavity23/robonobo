package com.robonobo.gui.model;

import com.robonobo.core.api.model.Playlist;
import com.robonobo.gui.frames.RobonoboFrame;

public class LovesTreeNode extends PlaylistTreeNode {

	public LovesTreeNode(Playlist p, RobonoboFrame frame) {
		super(p, frame);
	}
	
	@Override
	protected int getSpecialIndex() {
		return 1;
	}
}
