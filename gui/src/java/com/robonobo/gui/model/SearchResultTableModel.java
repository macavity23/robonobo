package com.robonobo.gui.model;

import java.util.List;

import ca.odell.glazedlists.*;

import com.robonobo.common.exceptions.Errot;
import com.robonobo.core.api.SearchListener;
import com.robonobo.core.api.model.*;
import com.robonobo.gui.frames.RobonoboFrame;
import com.robonobo.mina.external.FoundSourceListener;

@SuppressWarnings("serial")
public class SearchResultTableModel extends GlazedTrackListTableModel implements SearchListener, FoundSourceListener {
	public static SearchResultTableModel create(RobonoboFrame frame) {
		BasicEventList<Track> el = new BasicEventList<Track>();
		SortedList<Track> sl = new SortedList<Track>(el, new TrackComparator());
		return new SearchResultTableModel(frame, el, sl);
	}

	private SearchResultTableModel(RobonoboFrame frame, EventList<Track> el, SortedList<Track> sl) {
		super(frame, el, sl, null);
	}

	public void die() {
		readLock.lock();
		try {
			for (Track t : eventList) {
				control.stopFindingSources(t.stream.streamId, this);
			}
		} finally {
			readLock.unlock();
		}
	}

	public void gotNumberOfResults(int numResults) {
		// Do nothing
	}

	public void foundResult(final Stream s) {
		// Ignore duplicates
		if(getTrackIndex(s.streamId) >= 0)
			return;
		Track t = control.getTrack(s.getStreamId());
		add(t);
		if (t instanceof CloudTrack)
			control.findSources(s.getStreamId(), this);
	}

	public void foundBroadcaster(String streamId, String nodeId) {
		Track t = frame.control.getTrack(streamId);
		trackUpdated(streamId, t);
	}

	@Override
	public boolean allowDelete() {
		return false;
	}

	@Override
	public void deleteTracks(List<String> streamIds) {
		throw new Errot();
	}
}
