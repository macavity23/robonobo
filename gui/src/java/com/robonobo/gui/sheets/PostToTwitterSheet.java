package com.robonobo.gui.sheets;

import com.robonobo.core.api.model.Playlist;
import com.robonobo.gui.frames.RobonoboFrame;

@SuppressWarnings("serial")
public class PostToTwitterSheet extends PostUpdateSheet {
	public PostToTwitterSheet(RobonoboFrame f, Playlist pl) {
		super(f, pl);
	}

	@Override
	protected String getServiceName() {
		return "Twitter";
	}

	@Override
	protected int charLimit() {
		return 140;
	}
}
