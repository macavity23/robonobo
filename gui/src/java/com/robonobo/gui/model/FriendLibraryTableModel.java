package com.robonobo.gui.model;

import java.util.*;

import javax.swing.SwingUtilities;

import com.robonobo.common.concurrent.Batcher;
import com.robonobo.common.concurrent.CatchingRunnable;
import com.robonobo.core.RobonoboController;
import com.robonobo.core.api.LibraryListener;
import com.robonobo.core.api.model.*;
import com.robonobo.mina.external.FoundSourceListener;

@SuppressWarnings("serial")
public class FriendLibraryTableModel extends FreeformTrackListTableModel implements LibraryListener,
		FoundSourceListener {
	private Library lib;
	// Because we might have very many tracks in a friend's library, we don't look up sources for them straight away -
	// instead we look them up as the user scrolls - but we batch them for performance
	static final long SCROLL_DELAY = 1000;
	private TrackScrollBatcher scrollBatcher = new TrackScrollBatcher();
	private boolean activated = false;

	public FriendLibraryTableModel(RobonoboController controller, Library lib) {
		super(controller);
		controller.addLibraryListener(this);
		this.lib = lib;
		libraryChanged(lib, lib.getTracks().keySet(), false);
	}

	@Override
	public Track getTrack(int index) {
		// We set the 'date added' to the time it was added to the library
		Track t = super.getTrack(index);
		t.setDateAdded(lib.getTracks().get(t.getStream().getStreamId()));
		return t;
	}

	@Override
	public void libraryChanged(Library lib, Set<String> newTrackSids) {
		libraryChanged(lib, newTrackSids, true);
	}

	public void libraryChanged(Library lib, Set<String> newTrackSids, boolean fireEvent) {
		if (lib.getUserId() != this.lib.getUserId())
			return;
		List<Track> addTrax = new ArrayList<Track>();
		for (String sid : newTrackSids) {
			addTrax.add(control.getTrack(sid));
		}
		add(addTrax, fireEvent);
		this.lib = lib;
	}

	public void activate() {
		activated = true;
	}

	public synchronized void foundBroadcaster(String streamId, String nodeId) {
		if (!streamIndices.containsKey(streamId))
			return;
		trackUpdated(streamId);
	}

	@Override
	public void trackUpdated(String streamId) {
		int index = -1;
		boolean shouldAdd = false;
		if (lib.getTracks().keySet().contains(streamId)) {
			// We want this one
			synchronized (this) {
				if (streamIndices.containsKey(streamId))
					index = streamIndices.get(streamId);
				else
					shouldAdd = true;
			}
		}
		if (shouldAdd)
			add(control.getTrack(streamId));
		else if (index >= 0) {
			// Updated
			final int findex = index;
			SwingUtilities.invokeLater(new CatchingRunnable() {
				public void doRun() throws Exception {
					fireTableRowsUpdated(findex, findex);
				}
			});
		}
	}

	@Override
	public boolean allowDelete() {
		return false;
	}

	@Override
	public boolean wantScrollEvents() {
		return true;
	}

	@Override
	public synchronized void onScroll(int[] indexen) {
		if(!activated)
			return;
		for (int i = 0; i < indexen.length; i++) {
			scrollBatcher.add(streams.get(i).getStreamId());
		}
	}

	@Override
	public void allTracksLoaded() {
		// Do nothing
	}

	@Override
	public void deleteTracks(List<String> streamIds) {
		// Never called
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
