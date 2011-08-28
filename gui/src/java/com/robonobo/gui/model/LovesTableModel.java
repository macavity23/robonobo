package com.robonobo.gui.model;

import java.util.ArrayList;
import java.util.List;

import ca.odell.glazedlists.*;

import com.robonobo.core.api.model.Playlist;
import com.robonobo.core.api.model.Track;
import com.robonobo.gui.frames.RobonoboFrame;

public class LovesTableModel extends SpecialPlaylistTableModel {
	public static LovesTableModel create(RobonoboFrame frame, Playlist p) {
		List<Track> trax = new ArrayList<Track>();
		for (String sid : p.getStreamIds()) {
			trax.add(frame.ctrl.getTrack(sid));
		}
		EventList<Track> el = GlazedLists.eventList(trax);
		SortedList<Track> sl = new SortedList<Track>(el, new TrackComparator());
		return new LovesTableModel(frame, p, el, sl);
	}

	protected LovesTableModel(RobonoboFrame frame, Playlist p, EventList<Track> el, SortedList<Track> sl) {
		super(frame, p, false, el, sl);
	}

	@Override
	public String deleteTracksTooltipDesc() {
		return "Stop loving these tracks";
	}

	@Override
	public String longDeleteTracksDesc() {
		return "stop loving these tracks";
	}
}
