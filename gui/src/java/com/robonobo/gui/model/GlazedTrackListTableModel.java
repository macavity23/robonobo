package com.robonobo.gui.model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ca.odell.glazedlists.*;
import ca.odell.glazedlists.swing.EventTableModel;
import ca.odell.glazedlists.util.concurrent.Lock;

import com.robonobo.common.util.FileUtil;
import com.robonobo.common.util.TimeUtil;
import com.robonobo.core.RobonoboController;
import com.robonobo.core.api.TrackListener;
import com.robonobo.core.api.model.Stream;
import com.robonobo.core.api.model.Track;
import com.robonobo.gui.frames.RobonoboFrame;

@SuppressWarnings("serial")
public abstract class GlazedTrackListTableModel extends EventTableModel<Track> implements TrackListTableModel, TrackListener {
	private static final int[] HIDDEN_COLS = new int[] { 4, 12 };
	static DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	static String[] colNames = { " "/* StatusIcon */, "Title", "Artist", "Album", "Track", "Year", "Time", "Size", "Status", "Download", "Upload", "Added to Library", "Stream Id" };
	static Pattern firstNumPat = Pattern.compile("^\\s*(\\d*).*$");
	protected Map<String, Integer> trackIndices = new HashMap<String, Integer>();
	protected Lock readLock, updateLock;
	/** Base list of items we can add to - trackIndices is in terms of this list */
	protected EventList<Track> eventList;
	protected SortedList<Track> sortedList;
	protected FilterList<Track> filterList;
	protected RobonoboFrame frame;
	protected RobonoboController control;
	protected Log log = LogFactory.getLog(getClass());

	public GlazedTrackListTableModel(RobonoboFrame frame, EventList<Track> el, SortedList<Track> sl, FilterList<Track> fl) {
		// Use first non-null of fl, sl, el
		super((fl != null) ? fl : ((sl != null) ? sl : el), new TrackTableFormat());
		this.frame = frame;
		this.control = frame.getController();
		eventList = el;
		sortedList = sl;
		filterList = fl;
		readLock = eventList.getReadWriteLock().readLock();
		updateLock = eventList.getReadWriteLock().writeLock();
		control.addTrackListener(this);
		// Build our initial trackindices list
		int i=0;
		for (Track t : el) {
			trackIndices.put(t.stream.streamId, i++);
		}
	}

	public boolean canSort() {
		return true;
	}

	public SortedList<Track> getSortedList() {
		return sortedList;
	}

	public int getColumnCount() {
		return colNames.length;
	}

	@Override
	public String getColumnName(int column) {
		return colNames[column];
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		switch (columnIndex) {
		case 0:
			return Track.PlaybackStatus.class;
		case 1:
		case 2:
		case 3:
		case 5:
		case 7:
		case 9:
		case 10:
		case 12:
			return String.class;
		case 11:
			return Date.class;
		case 4:
			return Integer.class;
		default:
			return Object.class;
		}
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		Track t = getTrack(rowIndex);
		if (t == null)
			return null;
		Stream s = t.getStream();
		switch (columnIndex) {
		case 0:
			return t.getPlaybackStatus();
		case 1:
			return s.title;
		case 2:
			return s.artist;
		case 3:
			return s.album;
		case 4:
			return getTrackNumber(s);
		case 5:
			return s.getAttrValue("year");
		case 6:
			return TimeUtil.minsSecsFromMs(s.getDuration());
		case 7:
			return FileUtil.humanReadableSize(s.getSize());
		case 8:
			return t.getTransferStatus();
		case 9:
			int rate = t.getDownloadRate();
			if (rate == 0) {
				return null;
			}
			return FileUtil.humanReadableSize(rate) + "/s";
		case 10:
			rate = t.getUploadRate();
			if (rate == 0) {
				return null;
			}
			return FileUtil.humanReadableSize(rate) + "/s";
		case 11:
			return t.getDateAdded();
		case 12:
			return s.getStreamId();
		}
		return null;
	}

	private Integer getTrackNumber(Stream s) {
		String trackStr = s.getAttrValue("track");
		if (trackStr == null || trackStr.length() == 0)
			return null;
		Matcher m = firstNumPat.matcher(trackStr);
		if (!m.matches())
			return null;
		return Integer.parseInt(m.group(1));
	}

	@Override
	public void trackUpdated(String streamId, Track t) {
		updateLock.lock();
		try {
			Integer idx = trackIndices.get(streamId);
			if (idx == null)
				return;
			eventList.set(idx, t);
		} finally {
			updateLock.unlock();
		}
	}

	@Override
	public void tracksUpdated(Collection<Track> trax) {
		updateLock.lock();
		try {
			for (Track t : trax) {
				String sid = t.getStream().streamId;
				Integer idx = trackIndices.get(sid);
				if (idx == null)
					continue;
				eventList.set(idx, t);
			}
		} finally {
			updateLock.unlock();
		}
	}

	@Override
	public void allTracksLoaded() {
		// Do nothing
	}

	@Override
	public Track getTrack(int index) {
		if(index >= viewList().size())
			return null;
		return getElementAt(index);
	}

	@Override
	public String getStreamId(int index) {
		if(index >= viewList().size())
			return null;
		return getElementAt(index).stream.streamId;
	}

	public boolean containsTrack(String sid) {
		updateLock.lock();
		try {
			return trackIndices.containsKey(sid);
		} finally {
			updateLock.unlock();
		}
	}
	
	private EventList<Track> viewList() {
		if (filterList != null)
			return filterList;
		if (sortedList != null)
			return sortedList;
		return eventList;
	}

	/** Returns -1 if sid is not in tracklist */
	@Override
	public int getTrackIndex(String sid) {
		Track t = control.getTrack(sid);
		// This indexOf will be fast if we are viewing a sortedlist, but not if it's a filteredlist
		return viewList().indexOf(t);
	}

	protected void add(Track t) {
		updateLock.lock();
		try {
			if (trackIndices.containsKey(t.stream.streamId))
				return;
			trackIndices.put(t.stream.streamId, eventList.size());
			eventList.add(t);
		} finally {
			updateLock.unlock();
		}
	}

	protected void add(Collection<Track> trax) {
		updateLock.lock();
		try {
			int trackIdx = eventList.size();
			for (Track t : trax) {
				eventList.add(t);
				trackIndices.put(t.stream.streamId, trackIdx++);
			}
		} finally {
			updateLock.unlock();
		}
	}

	// By default we hide the track num and stream id
	public int[] hiddenCols() {
		return HIDDEN_COLS;
	}

	/** Return true in a subclass to have onScroll() called every time the track list is scrolled (as long as
	 * wantScrollEventsNow() returns true) */
	public boolean wantScrollEventsEver() {
		return false;
	}

	/** Return true in a subclass to have onScroll() called with the in-view indexen (wantScrollEventsEver() must also
	 * return true) */
	public boolean wantScrollEventsNow() {
		return false;
	}

	/** @param indexen
	 *            The items currently in-viewport (model indexes, not view). Note this is called on the UI thread, so be
	 *            thrifty with your cycles! */
	public void onScroll(int[] indexen) {
		// Default implementation does nothing
	}

	/** Are we allowed to delete tracks from this tracklist? */
	public abstract boolean allowDelete();

	public void deleteTracks(List<String> streamIds) {
		// This will only be called if allowDelete() returns true
		updateLock.lock();
		try {
			for (String sid : streamIds) {
				Integer delIdx = trackIndices.remove(sid);
				if (delIdx != null) {
					eventList.remove((int) delIdx);
					// Bump track indices down for everyone above us
					for (int i = delIdx; i < eventList.size(); i++) {
						String bumpSid = eventList.get(i).stream.streamId;
						trackIndices.put(bumpSid, i);
					}
				}
			}
		} finally {
			updateLock.unlock();
		}
	}
}
