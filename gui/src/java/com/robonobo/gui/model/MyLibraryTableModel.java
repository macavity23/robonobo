package com.robonobo.gui.model;

import static com.robonobo.common.util.CodeUtil.*;

import java.util.ArrayList;
import java.util.List;

import javax.swing.text.Document;

import ca.odell.glazedlists.*;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;

import com.robonobo.common.concurrent.CatchingRunnable;
import com.robonobo.common.util.CodeUtil;
import com.robonobo.core.api.RobonoboException;
import com.robonobo.core.api.model.*;
import com.robonobo.gui.components.TrackList;
import com.robonobo.gui.frames.RobonoboFrame;

@SuppressWarnings("serial")
public class MyLibraryTableModel extends GlazedTrackListTableModel {
	public static MyLibraryTableModel create(RobonoboFrame frame, Document searchTextDoc) {
		EventList<Track> el = new BasicEventList<Track>();
		SortedList<Track> sl = new SortedList<Track>(el, new TrackComparator());
		TextComponentMatcherEditor<Track> matchEdit = new TextComponentMatcherEditor<Track>(searchTextDoc, new TrackFilterator());
		matchEdit.setLive(true);
		FilterList<Track> fl = new FilterList<Track>(sl, matchEdit);
		return new MyLibraryTableModel(frame, el, sl, fl);
	}

	private MyLibraryTableModel(RobonoboFrame frame, EventList<Track> el, SortedList<Track> sl, FilterList<Track> fl) {
		super(frame, el, sl, fl);
	}

	@Override
	public void trackUpdated(String streamId, Track t) {
		// Override the super method - add this track to our library if we're now sharing/downloading it
		updateLock.lock();
		try {
			Integer idx = trackIndices.get(streamId);
			if (idx != null)
				eventList.set(idx, t);
			else {
				if ((t instanceof SharedTrack) || (t instanceof DownloadingTrack))
					add(t);
			}
		} finally {
			updateLock.unlock();
		}
	}

	@Override
	public void allTracksLoaded() {
		final List<Track> trax = new ArrayList<Track>();
		for (String streamId : control.getShares()) {
			Track t = control.getTrack(streamId);
			trax.add(t);
		}
		for (String streamId : control.getDownloads()) {
			Track t = control.getTrack(streamId);
			trax.add(t);
		}
		if (trax.size() < TrackList.TRACKLIST_SIZE_THRESHOLD)
			add(trax);
		else {
			frame.runSlowTask("library loading", new CatchingRunnable() {
				public void doRun() throws Exception {
					add(trax);
				}
			});
		}
	}

	@Override
	public boolean allowDelete() {
		return true;
	}

	@Override
	public String deleteTracksDesc() {
		return "Remove tracks from library";
	}
	
	@Override
	public void deleteTracks(List<String> streamIds) {
		// We delete downloads all at once to avoid starting downloads we're about to delete
		List<String> dlSids = new ArrayList<String>();
		try {
			for (String sid : streamIds) {
				Track t = control.getTrack(sid);
				if (t instanceof DownloadingTrack)
					dlSids.add(sid);
				else if (t instanceof SharedTrack)
					control.deleteShare(sid);
			}
			control.deleteDownloads(dlSids);
		} catch (RobonoboException ex) {
			log.error("Error deleting share/download", ex);
		}
		super.deleteTracks(streamIds);
	}
}
