package com.robonobo.gui.model;

import com.robonobo.gui.frames.RobonoboFrame;

@SuppressWarnings("serial")
public class AddFriendsTreeNode extends ButtonTreeNode {
	RobonoboFrame frame;
	public AddFriendsTreeNode(RobonoboFrame frame) {
		super("Add friends...");
		this.frame = frame;
	}
	
	@Override
	public void onClick() {
		frame.showAddFriendsSheet();
	}
}
