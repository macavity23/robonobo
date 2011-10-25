package com.robonobo.gui.model;

import java.util.*;

import javax.swing.text.Document;

import ca.odell.glazedlists.*;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;

import com.robonobo.core.api.RobonoboException;
import com.robonobo.core.api.model.*;
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

	private boolean trackBelongsInMyLib(Track t) {
		return (t instanceof SharedTrack) || (t instanceof DownloadingTrack);
	}
	
	@Override
	public void trackUpdated(String streamId, Track t) {
		// Override the super method - add this track to our library if we're now sharing/downloading it
		updateLock.lock();
		try {
			if(containsTrack(streamId)) {
				if(trackBelongsInMyLib(t))
					super.trackUpdated(streamId, t);
				else
					deleteTrack(streamId);
			} else {
				if(trackBelongsInMyLib(t))
					add(t);
			}
		} finally {
			updateLock.unlock();
		}
	}

	@Override
	public void tracksUpdated(Collection<Track> trax) {
		// Override the super method - add/remove to our library as appropriate 
		updateLock.lock();
		try {
			List<Track> traxToUpdate = new ArrayList<Track>();
			List<Track> traxToAdd = new ArrayList<Track>();
			List<String> sidsToDel = new ArrayList<String>();
			for(Track t : trax) {
				Integer idx = trackIndices.get(t.getStream().getStreamId());
				if (idx != null) {
					if(trackBelongsInMyLib(t))
						traxToUpdate.add(t);
					else
						sidsToDel.add(t.stream.streamId);
				} else {
					if ((t instanceof SharedTrack) || (t instanceof DownloadingTrack))
						traxToAdd.add(t);
				}
			}
			if(traxToAdd.size() > 0)
				add(traxToAdd);
			if(sidsToDel.size() > 0)
				deleteTracks(sidsToDel);
			if(traxToUpdate.size() > 0)
				super.tracksUpdated(traxToUpdate);
		} finally {
			updateLock.unlock();
		}
	}

	@Override
	public void allTracksLoaded() {
		// Do nothing
	}

	@Override
	public boolean allowDelete() {
		return true;
	}

	@Override
	public String deleteTracksTooltipDesc() {
		return "Remove tracks from library";
	}
	
	@Override
	public String longDeleteTracksDesc() {
		return "remove these tracks from your music library";
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
			StringBuffer sb = new StringBuffer("DEBUG: MLTM deleting downloads:");
			for (String sid : dlSids) {
				sb.append(" ").append(sid);
			}
			log.debug(sb);
			control.deleteDownloads(dlSids);
		} catch (RobonoboException ex) {
			log.error("Error deleting share/download", ex);
		}
		super.deleteTracks(streamIds);
	}
}
