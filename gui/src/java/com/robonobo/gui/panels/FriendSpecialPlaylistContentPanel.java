package com.robonobo.gui.panels;

import com.robonobo.core.api.model.Playlist;
import com.robonobo.core.api.model.PlaylistConfig;
import com.robonobo.gui.frames.RobonoboFrame;

@SuppressWarnings("serial")
public class FriendSpecialPlaylistContentPanel extends OtherPlaylistContentPanel {
	private long userId;

	public FriendSpecialPlaylistContentPanel(RobonoboFrame f, long userId, Playlist pl, PlaylistConfig pc) {
		super(f, pl, pc);
		this.userId = userId;
		// hackitty-hack: urlText() depends on userId, so set it again here
		toolsPanel.urlField.setText(toolsPanel.urlText());
	}

	@Override
	protected PlaylistToolsPanel createToolsPanel() {
		return new ToolsPanel();
	}

	class ToolsPanel extends PlaylistToolsPanel {
		@Override
		protected String urlText() {
			String result = frame.ctrl.getConfig().getShortUrlBase() + "sp/" + Long.toHexString(userId) + "/" + p.getTitle().toLowerCase();
			return result;
		}
	}
}
