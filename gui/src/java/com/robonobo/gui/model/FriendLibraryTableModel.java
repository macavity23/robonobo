package com.robonobo.gui.model;

import java.util.*;
import java.util.Map.Entry;

import javax.swing.text.Document;

import ca.odell.glazedlists.*;
import ca.odell.glazedlists.matchers.MatcherEditor;
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
	// instead we look them up as the user scrolls - but we batch them to avoid hanging the ui thread
	static final long SCROLL_DELAY = 1000;
	private TrackScrollBatcher scrollBatcher = new TrackScrollBatcher();
	boolean activated = false;
	MatcherEditor<Track> matchEdit;
	Set<String> initialSids;

	public static FriendLibraryTableModel create(RobonoboFrame frame, Library lib, Document searchTextDoc) {
		List<Track> trax = new ArrayList<Track>();
		for (Entry<String, Date> e : lib.getTracks().entrySet()) {
			String sid = e.getKey();
			Date added = e.getValue();
			Track t = frame.ctrl.getTrack(sid);
			t.setDateAdded(added);
			trax.add(t);
		}
		EventList<Track> el = GlazedLists.eventList(trax);
		SortedList<Track> sl = new SortedList<Track>(el, new TrackComparator());
		TextComponentMatcherEditor<Track> matchEdit = new TextComponentMatcherEditor<Track>(searchTextDoc, new TrackFilterator());
		matchEdit.setLive(true);
		FilterList<Track> fl = new FilterList<Track>(sl, matchEdit);
		return new FriendLibraryTableModel(frame, lib, el, sl, fl, matchEdit);
	}

	private FriendLibraryTableModel(RobonoboFrame frame, Library lib, EventList<Track> el, SortedList<Track> sl, FilterList<Track> fl, MatcherEditor<Track> matchEdit) {
		super(frame, el, sl, fl);
		this.lib = lib;
		this.matchEdit = matchEdit;
		// Because of the synching between the initial call to ctrl.getLibrary() (the results of which are passed
		// here in 'lib') and the periodic friendLibraryUpdated events, this initial set of tracks may be partly
		// duplicated in the first friendLibraryUpdated - so we keep track of them here to avoid adding them twice
		// to the tracklist
		initialSids = new HashSet<String>();
		initialSids.addAll(lib.getTracks().keySet());
		frame.ctrl.addLibraryListener(this);
	}

	@Override
	public MatcherEditor<Track> getMatcherEditor() {
		return matchEdit;
	}

	@Override
	public void trackUpdated(String sid, Track t) {
		if (containsTrack(sid)) {
			// We can't set our date on this track as it will be re-used in other places - clone it instead
			Track ct = t.clone();
			ct.setDateAdded(lib.getTracks().get(sid));
			super.trackUpdated(sid, ct);
		}
	}

	@Override
	public void friendLibraryReady(long userId, int numUnseen) {
		// Do nothing
	}

	@Override
	public void friendLibraryUpdated(long userId, int numUnseen, Map<String, Date> newTracks) {
		if (userId != lib.getUserId())
			return;
		List<Track> addTrax = new ArrayList<Track>();
		for (String sid : newTracks.keySet()) {
			// Check for duplicate
			if(initialSids != null && initialSids.contains(sid))
				continue;
			Track t = control.getTrack(sid);
			t.setDateAdded(newTracks.get(sid));
			addTrax.add(t);
		}
		// Duplicates can only happen on the first update event, so nuke our set here to return the memory (libraries can be large)
		initialSids = null;
		lib.getTracks().putAll(newTracks);
		add(addTrax);
	}

	@Override
	public void myLibraryUpdated() {
		// Do nothing
	}

	@Override
	public void gotLibraryComments(long userId, boolean anyUnread, Map<Comment, Boolean> comments) {
		// Do nothing
	}

	public void foundBroadcaster(String sid, String nodeId) {
		if(!containsTrack(sid))
			return;
		// Get a fresh track to include this new broadcaster
		Track t = control.getTrack(sid);
		trackUpdated(sid, t);
	}

	@Override
	public boolean allowDelete() {
		return false;
	}

	@Override
	public String deleteTracksTooltipDesc() {
		return null;
	}

	@Override
	public String longDeleteTracksDesc() {
		return null;
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
					Track t = filterList.get(sIdx);
					String sid = t.stream.streamId;
					scrollBatcher.add(sid);
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
				if (t instanceof CloudTrack) {
					control.findSources(sid, FriendLibraryTableModel.this);
				}
			}
		}
	}
}
