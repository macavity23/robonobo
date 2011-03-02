package com.robonobo.gui.model;

import com.robonobo.core.RobonoboController;
import com.robonobo.core.api.model.Playlist;

@SuppressWarnings("serial")
public class NewPlaylistTableModel extends PlaylistTableModel {
	public NewPlaylistTableModel(RobonoboController controller) {
		super(controller, new Playlist(), true);
	}

	@Override
	protected void runPlaylistUpdate() {
		// Do nothing, don't actually run the update as no playlist exists yet
		return;
	}
}
