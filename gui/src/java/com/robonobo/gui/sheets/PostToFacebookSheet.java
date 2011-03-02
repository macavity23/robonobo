package com.robonobo.gui.sheets;

import com.robonobo.core.api.model.Playlist;
import com.robonobo.gui.frames.RobonoboFrame;

@SuppressWarnings("serial")
public class PostToFacebookSheet extends PostUpdateSheet {
	public PostToFacebookSheet(RobonoboFrame f, Playlist pl) {
		super(f, pl);
	}

	@Override
	protected String getServiceName() {
		return "Facebook";
	}

	@Override
	protected int charLimit() {
		return -1;
	}

	@Override
	public void postUpdate() {
		frame.getController().postFacebookUpdate(p.getPlaylistId(), getMsg());
	}

}
