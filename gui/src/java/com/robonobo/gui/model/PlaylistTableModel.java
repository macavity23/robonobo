package com.robonobo.gui.model;

import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ca.odell.glazedlists.*;

import com.robonobo.common.concurrent.CatchingRunnable;
import com.robonobo.common.exceptions.SeekInnerCalmException;
import com.robonobo.core.api.model.*;
import com.robonobo.gui.frames.RobonoboFrame;
import com.robonobo.mina.external.FoundSourceListener;

@SuppressWarnings("serial")
public class PlaylistTableModel extends GlazedTrackListTableModel implements FoundSourceListener {
	private static final int[] PLAYLIST_HIDDEN_COLS = new int[] { 4, 11, 12 };
	protected Playlist p;
	protected boolean canEdit;
	protected boolean canDelete;
	/** Are we actively looking for sources for streams on this playlist? */
	private boolean activated = false;
	Log log = LogFactory.getLog(getClass());

	public static PlaylistTableModel create(RobonoboFrame frame, Playlist p, boolean canEdit) {
		List<Track> trax = new ArrayList<Track>();
		for (String sid : p.getStreamIds()) {
			trax.add(frame.ctrl.getTrack(sid));
		}
		EventList<Track> el = GlazedLists.eventList(trax);
		return new PlaylistTableModel(frame, p, canEdit, el, null);
	}

	protected PlaylistTableModel(RobonoboFrame frame, Playlist p, boolean canEdit, EventList<Track> el, SortedList<Track> sl) {
		super(frame, el, sl, null);
		this.p = p;
		this.canEdit = canEdit;
		this.canDelete = canEdit;
		int i = 0;
		for (String sid : p.getStreamIds()) {
			trackIndices.put(sid, i++);
		}
		if (canEdit) {
			control.getExecutor().execute(new CatchingRunnable() {
				public void doRun() throws Exception {
					activate();
				}
			});
		}
	}

	@Override
	public boolean canSort() {
		return false;
	}

	public void update(Playlist p) {
		Playlist oldP = this.p;
		this.p = p;
		// Any items on the old list that aren't on the new, stop finding
		// sources for them
		for (String streamId : oldP.getStreamIds()) {
			if (!p.getStreamIds().contains(streamId))
				control.stopFindingSources(streamId, this);
		}
		updateLock.lock();
		try {
			regenEventList();
		} finally {
			updateLock.unlock();
		}
		if (canEdit || activated)
			activate();
	}

	public void activate() {
		if (!activated) {
			activated = true;
			// Make sure we've got fresh info for all our tracks
			for (String sid : p.getStreamIds()) {
				Track t = control.getTrack(sid);
				if (t instanceof CloudTrack)
					control.findSources(sid, this);
				trackUpdated(sid, t);
			}
		}
	}

	public void nuke() {
		control.removeTrackListener(this);
		for (String streamId : p.getStreamIds()) {
			control.stopFindingSources(streamId, this);
		}
	}

	@Override
	public void trackUpdated(String streamId, Track t) {
		super.trackUpdated(streamId, t);
		if ((activated || canEdit) && containsTrack(streamId))
			control.findSources(streamId, this);
	}

	// Hide 'Added to Library' as well as streamid and track num
	public int[] hiddenCols() {
		return PLAYLIST_HIDDEN_COLS;
	}

	public void foundBroadcaster(String sid, String nodeId) {
		// Get a fresh track to include this new broadcaster
		Track t = control.getTrack(sid);
		trackUpdated(sid, t);
	}

	/** If any of these streams are already in this playlist, they will be removed before being added in their new
	 * position */
	public void addStreams(List<String> streamIds, int position) {
		if (!canEdit)
			throw new SeekInnerCalmException();
		updateLock.lock();
		try {
			// Rather than buggering about inside our eventlist, we re-order the playlist and then just re-add the whole
			// list again
			// First, scan through our playlist and remove any that are in this
			// list (they're being moved)
			for (Iterator<String> iter = p.getStreamIds().iterator(); iter.hasNext();) {
				String pStreamId = iter.next();
				if (streamIds.contains(pStreamId))
					iter.remove();
			}
			if (position > p.getStreamIds().size())
				position = p.getStreamIds().size();
			// Put the new streams in the right spot
			p.getStreamIds().addAll(position, streamIds);
			// Now regenerate our tracks
			regenEventList();
		} finally {
			updateLock.unlock();
		}
		runPlaylistUpdate();
	}

	/** Must only be called from inside updateLock */
	protected void regenEventList() {
		eventList.clear();
		trackIndices.clear();
		int i = 0;
		for (String sid : p.getStreamIds()) {
			eventList.add(control.getTrack(sid));
			trackIndices.put(sid, i++);
		}
	}

	public Playlist getPlaylist() {
		return p;
	}

	@Override
	public boolean allowDelete() {
		return canDelete;
	}

	@Override
	public String deleteTracksTooltipDesc() {
		return "Remove tracks from playlist";
	}

	@Override
	public String longDeleteTracksDesc() {
		return "remove these tracks from this playlist";
	}

	@Override
	public void deleteTracks(List<String> streamIds) {
		if (!canDelete)
			throw new SeekInnerCalmException();
		updateLock.lock();
		try {
			for (String sid : streamIds) {
				p.getStreamIds().remove(sid);
				control.stopFindingSources(sid, this);
			}
			regenEventList();
		} finally {
			updateLock.unlock();
		}
		runPlaylistUpdate();
	}

	protected void runPlaylistUpdate() {
		control.updatePlaylist(p);
	}
}
