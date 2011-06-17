package com.robonobo.gui.model;

import java.util.*;
import java.util.Map.Entry;

import javax.swing.text.Document;

import ca.odell.glazedlists.*;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;

import com.robonobo.common.concurrent.Batcher;
import com.robonobo.core.api.LibraryListener;
import com.robonobo.core.api.model.*;
import com.robonobo.gui.frames.RobonoboFrame;
import com.robonobo.mina.external.FoundSourceListener;

@SuppressWarnings("serial")
public class FriendLibraryTableModel extends GlazedTrackListTableModel implements LibraryListener, FoundSourceListener {
	private Library lib;
	// Because we might have very many tracks in a friend's library, we don't look up sources for them straight away -
	// instead we look them up as the user scrolls - but we batch them for performance
	static final long SCROLL_DELAY = 1000;
	private TrackScrollBatcher scrollBatcher = new TrackScrollBatcher();
	boolean activated = false;

	public static FriendLibraryTableModel create(RobonoboFrame frame, Library lib, Document searchTextDoc) {
		List<Track> trax = new ArrayList<Track>();
		for (Entry<String, Date> e : lib.getTracks().entrySet()) {
			String sid = e.getKey();
			Date added = e.getValue();
			Track t = frame.control.getTrack(sid);
			t.setDateAdded(added);
			trax.add(t);
		}
		EventList<Track> el = GlazedLists.eventList(trax);
		SortedList<Track> sl = new SortedList<Track>(el, new TrackComparator());
		TextComponentMatcherEditor<Track> matchEdit = new TextComponentMatcherEditor<Track>(searchTextDoc, new TrackFilterator());
		matchEdit.setLive(true);
		FilterList<Track> fl = new FilterList<Track>(sl, matchEdit);
		return new FriendLibraryTableModel(frame, lib, el, sl, fl);
	}

	private FriendLibraryTableModel(RobonoboFrame frame, Library lib, EventList<Track> el, SortedList<Track> sl, FilterList<Track> fl) {
		super(frame, el, sl, fl);
		this.lib = lib;
		frame.control.addLibraryListener(this);
	}

	@Override
	public void trackUpdated(String streamId, Track t) {
		if (containsTrack(streamId)) {
			// We can't set our date on this track as it will be re-used in other places - clone it instead
			t = t.clone();
			t.setDateAdded(lib.getTracks().get(streamId));
			super.trackUpdated(streamId, t);
		}
	}

	@Override
	public void libraryChanged(Library lib, Collection<String> newTrackSids) {
		if (lib.getUserId() != this.lib.getUserId())
			return;
		List<Track> addTrax = new ArrayList<Track>();
		for (String sid : newTrackSids) {
			Track t = control.getTrack(sid);
			Date da = lib.getTracks().get(t.stream.streamId);
			t.setDateAdded(da);
			addTrax.add(t);
		}
		this.lib = lib;
		add(addTrax);
	}

	@Override
	public void myLibraryUpdated() {
		// Do nothing
	}

	public void foundBroadcaster(String sid, String nodeId) {
		// Get a fresh track to include this new broadcaster
		Track t = control.getTrack(sid);
		trackUpdated(sid, t);
	}

	@Override
	public boolean allowDelete() {
		return false;
	}

	@Override
	public boolean wantScrollEventsEver() {
		return true;
	}

	public void activate() {
		activated = true;
	}

	@Override
	public boolean wantScrollEventsNow() {
		return activated;
	}

	@Override
	public void onScroll(int[] viewIndexen) {
		readLock.lock();
		try {
			for (int i = 0; i < viewIndexen.length; i++) {
				int sIdx = viewIndexen[i];
				if (sIdx >= 0) {
					Track t = filterList.get(i);
					scrollBatcher.add(t.stream.streamId);
				}
			}
		} finally {
			readLock.unlock();
		}
	}

	class TrackScrollBatcher extends Batcher<String> {
		public TrackScrollBatcher() {
			super(SCROLL_DELAY, control.getExecutor());
		}

		@Override
		protected void runBatch(Collection<String> sids) throws Exception {
			for (String sid : sids) {
				Track t = control.getTrack(sid);
				if (t instanceof CloudTrack)
					control.findSources(sid, FriendLibraryTableModel.this);
			}
		}
	}
}
