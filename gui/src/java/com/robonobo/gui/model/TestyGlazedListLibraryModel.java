package com.robonobo.gui.model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

import ca.odell.glazedlists.*;
import ca.odell.glazedlists.swing.EventTableModel;
import ca.odell.glazedlists.swing.TextComponentMatcherEditor;
import ca.odell.glazedlists.util.concurrent.Lock;

import com.robonobo.common.exceptions.Errot;
import com.robonobo.core.RobonoboController;
import com.robonobo.core.api.LibraryListener;
import com.robonobo.core.api.TrackListener;
import com.robonobo.core.api.model.Library;
import com.robonobo.core.api.model.Track;
import com.robonobo.gui.components.base.RTextField;
import com.robonobo.gui.frames.RobonoboFrame;
import com.robonobo.mina.external.FoundSourceListener;

@SuppressWarnings("serial")
public class TestyGlazedListLibraryModel extends EventTableModel<Track> implements TrackListTableModel, TrackListener, LibraryListener, FoundSourceListener {
	private static final int[] HIDDEN_COLS = new int[] { 4, 12 };
	static DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	Pattern firstNumPat = Pattern.compile("^\\s*(\\d*).*$");
	String[] colNames = { " "/* StatusIcon */, "Title", "Artist", "Album", "Track", "Year", "Time", "Size", "Status", "Download", "Upload", "Added to Library", "Stream Id" };
	private RobonoboFrame frame;
	private RobonoboController control;
	private TrackTableFormat tableFormat = new TrackTableFormat();
	private Map<String, Integer> trackIndices = new HashMap<String, Integer>();
	private Lock readLock, updateLock;
	private Library lib;
	private RTextField searchField;
	private EventList<Track> eventList;
	private SortedList<Track> sortedList;
	private FilterList<Track> filterList;

	public static TestyGlazedListLibraryModel create(RobonoboFrame frame, Library lib, List<Track> startingTrax, boolean filtering) {
		EventList<Track> origList = GlazedLists.eventList(startingTrax);
		SortedList<Track> sortedList = new SortedList<Track>(origList, new TrackComparator());
		FilterList<Track> filterList = null;
		RTextField searchField = null;
		if (filtering) {
			// TODO Creating jtextfields inside the model like this is really nasty, but glazedlists requires the
			// textfield to pre-exist - create search Document instead and pass through
			searchField = new RTextField();
			TextComponentMatcherEditor<Track> matchEdit = new TextComponentMatcherEditor<Track>(searchField, new TrackFilterator());
			matchEdit.setLive(true);
			filterList = new FilterList<Track>(sortedList, matchEdit);
		}
		return new TestyGlazedListLibraryModel(frame, lib, origList, sortedList, filterList, searchField);
			
	}

	private TestyGlazedListLibraryModel(RobonoboFrame frame, Library lib, EventList<Track> eventList, SortedList<Track> sortedList, FilterList<Track> filterList,
			RTextField searchField) {
		super(filterList, new TrackTableFormat());
		this.frame = frame;
		this.eventList = eventList;
		this.sortedList = sortedList;
		this.filterList = filterList;
		this.lib = lib;
		this.control = frame.getController();
		this.searchField = searchField;
		readLock = eventList.getReadWriteLock().readLock();
		updateLock = eventList.getReadWriteLock().writeLock();
	}

	public SortedList<Track> viewTrackList() {
		return sortedList;
	}

	@Override
	public void libraryChanged(Library lib, Collection<String> newTrackSids) {
		List<Track> newTrax = new ArrayList<Track>();
		for (String sid : newTrackSids) {
			Track t = control.getTrack(sid);
			if (t == null)
				throw new Errot();
			newTrax.add(t);
		}
		updateLock.lock();
		try {
			this.lib = lib;
			int trackIdx = eventList.size();
			for (int i = 0; i < newTrax.size(); i++) {
				Track t = newTrax.get(i);
				eventList.add(t);
				trackIndices.put(t.getStream().getStreamId(), trackIdx++);
			}
		} finally {
			updateLock.unlock();
		}
	}

//	@Override
//	public void trackUpdated(String sid) {
//		updateLock.lock();
//		try {
//			Integer idx = trackIndices.get(sid);
//			if (idx == null)
//				return;
//			Track t = control.getTrack(sid);
//			eventList.set(idx, t);
//		} finally {
//			updateLock.unlock();
//		}
//	}
//
//	@Override
//	public void tracksUpdated(Collection<String> streamIds) {
//		updateLock.lock();
//		try {
//			for (String sid : streamIds) {
//				Integer idx = trackIndices.get(sid);
//				if (idx == null)
//					continue;
//				Track t = control.getTrack(sid);
//				eventList.set(idx, t);
//			}
//		} finally {
//			updateLock.unlock();
//		}
//	}

	
	@Override
	public void allTracksLoaded() {
		// Do nothing
	}

	@Override
	public void foundBroadcaster(String streamId, String nodeId) {
//		trackUpdated(streamId);
	}

	@Override
	public void myLibraryUpdated() {
		// Do nothing
	}

	@Override
	public int[] hiddenCols() {
		return HIDDEN_COLS;
	}

	@Override
	public boolean wantScrollEventsEver() {
		return false;
	}

	@Override
	public boolean wantScrollEventsNow() {
		return false;
	}

	@Override
	public void onScroll(int[] indexen) {
		// Do nothing for now
	}

	@Override
	public Track getTrack(int index) {
		readLock.lock();
		try {
			return eventList.get(index);
		} finally {
			readLock.unlock();
		}
	}

	@Override
	public String getStreamId(int index) {
		return getTrack(index).getStream().getStreamId();
	}

	@Override
	public int getTrackIndex(String streamId) {
		readLock.lock();
		try {
			Integer i = trackIndices.get(streamId);
			if (i == null)
				return -1;
			return i;
		} finally {
			readLock.unlock();
		}
	}

//	@Override
//	public int numTracks() {
//		readLock.lock();
//		try {
//			return eventList.size();
//		} finally {
//			readLock.unlock();
//		}
//	}

	@Override
	public boolean allowDelete() {
		return false;
	}

	@Override
	public void deleteTracks(List<String> streamIds) {
		// Do nothing
	}

	public RTextField getSearchField() {
		return searchField;
	}

	@Override
	public void trackUpdated(String streamId, Track t) {
	}

	@Override
	public void tracksUpdated(Collection<Track> trax) {
	}
}
