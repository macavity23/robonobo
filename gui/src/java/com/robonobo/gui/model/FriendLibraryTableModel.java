package com.robonobo.gui.model;

import java.util.List;

import javax.swing.SwingUtilities;

import com.robonobo.common.concurrent.CatchingRunnable;
import com.robonobo.core.RobonoboController;
import com.robonobo.core.api.UserPlaylistListener;
import com.robonobo.core.api.model.*;
import com.robonobo.mina.external.FoundSourceListener;

@SuppressWarnings("serial")
public class FriendLibraryTableModel extends FreeformTrackListTableModel implements UserPlaylistListener, FoundSourceListener {
	private Library lib;
	private boolean activated;

	public FriendLibraryTableModel(RobonoboController controller, Library lib) {
		super(controller);
		this.lib = lib;
		for (String sid : lib.getTracks().keySet()) {
			add(control.getTrack(sid), false);
		}
		controller.addUserPlaylistListener(this);
	}

	@Override
	public Track getTrack(int index) {
		// We set the 'date added' to the time it was added to the library
		Track t = super.getTrack(index);
		t.setDateAdded(lib.getTracks().get(t.getStream().getStreamId()));
		return t;
	}
	
	@Override
	public void libraryChanged(Library lib) {
		if(lib.getUserId() != this.lib.getUserId())
			return;
		synchronized (this) {
			streams.clear();
			streamIndices.clear();
		}
		this.lib = lib;
		for (String sid : lib.getTracks().keySet()) {
			add(control.getTrack(sid), false);
		}
		SwingUtilities.invokeLater(new CatchingRunnable() {
			public void doRun() throws Exception {
				fireTableDataChanged();
			}
		});
		// If we have been activated already, find sources for any new streams
		if(activated)
			activate();
	}

	public void activate() {
		activated = true;
		for (String streamId : lib.getTracks().keySet()) {
			Track t = control.getTrack(streamId);
			if (t instanceof CloudTrack)
				control.findSources(streamId, this);
		}
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
	public void allTracksLoaded() {
		// Do nothing
	}

	@Override
	public boolean allowDelete() {
		return false;
	}

	@Override
	public void deleteTracks(List<String> streamIds) {
		// Never called
	}

	@Override
	public void loggedIn() {
		// Do nothing
	}

	@Override
	public void userChanged(User u) {
		// Do nothing
	}

	@Override
	public void allUsersAndPlaylistsUpdated() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void playlistChanged(Playlist p) {
		// Do nothing
	}
	
	@Override
	public void userConfigChanged(UserConfig cfg) {
		// Do nothing
	}
}
