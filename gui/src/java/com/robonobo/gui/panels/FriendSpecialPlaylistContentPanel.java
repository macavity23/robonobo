package com.robonobo.gui.panels;

import com.robonobo.core.api.model.Playlist;
import com.robonobo.core.api.model.PlaylistConfig;
import com.robonobo.gui.frames.RobonoboFrame;

@SuppressWarnings("serial")
public class FriendSpecialPlaylistContentPanel extends OtherPlaylistContentPanel {
	private long userId;

	public FriendSpecialPlaylistContentPanel(RobonoboFrame f, long userId, Playlist pl, PlaylistConfig pc) {
		super(f, pl, pc, f.ctrl.getConfig().getShortUrlBase() + "sp/" + Long.toHexString(userId) + "/" + pl.getTitle().toLowerCase());
		this.userId = userId;
	}

	@Override
	protected PlaylistToolsPanel createToolsPanel() {
		return new ToolsPanel();
	}

	class ToolsPanel extends PlaylistToolsPanel {
		protected String urlText() {
			return frame.ctrl.getConfig().getShortUrlBase() + "sp/" + Long.toHexString(userId) + "/" + p.getTitle().toLowerCase();
		}
	}
}
