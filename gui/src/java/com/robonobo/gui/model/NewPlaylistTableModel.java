package com.robonobo.gui.model;

import java.util.ArrayList;
import java.util.List;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;

import com.robonobo.core.api.model.Playlist;
import com.robonobo.core.api.model.Track;
import com.robonobo.gui.frames.RobonoboFrame;

@SuppressWarnings("serial")
public class NewPlaylistTableModel extends PlaylistTableModel {
	public static NewPlaylistTableModel create(RobonoboFrame frame, Playlist p) {
		List<Track> trax = new ArrayList<Track>();
		for (String sid : p.getStreamIds()) {
			trax.add(frame.control.getTrack(sid));
		}
		EventList<Track> el = GlazedLists.eventList(trax);
		return new NewPlaylistTableModel(frame, p, el);
	}

	public NewPlaylistTableModel(RobonoboFrame frame, Playlist p, EventList<Track> el) {
		super(frame, p, true, el);
	}

	@Override
	protected void runPlaylistUpdate() {
		// Do nothing, don't actually run the update as no playlist exists yet
		return;
	}
}
