package com.robonobo.gui.model;

import java.util.ArrayList;
import java.util.List;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.GlazedLists;

import com.robonobo.core.api.model.Playlist;
import com.robonobo.core.api.model.Track;
import com.robonobo.gui.frames.RobonoboFrame;

public class RadioTableModel extends SpecialPlaylistTableModel {
	public static RadioTableModel create(RobonoboFrame frame, Playlist p, boolean canEdit) {
		List<Track> trax = new ArrayList<Track>();
		for (String sid : p.getStreamIds()) {
			trax.add(frame.ctrl.getTrack(sid));
		}
		EventList<Track> el = GlazedLists.eventList(trax);
		return new RadioTableModel(frame, p, canEdit, el);
	}

	protected RadioTableModel(RobonoboFrame frame, Playlist p, boolean canEdit, EventList<Track> el) {
		super(frame, p, canEdit, el, null);
	}

	@Override
	public String deleteTracksTooltipDesc() {
		return "Remove tracks from Radio";
	}

	@Override
	public String longDeleteTracksDesc() {
		return "remove these tracks from my radio";
	}
}
