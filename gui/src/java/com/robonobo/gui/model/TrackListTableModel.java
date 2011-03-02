package com.robonobo.gui.model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.table.AbstractTableModel;

import com.robonobo.common.util.FileUtil;
import com.robonobo.common.util.TimeUtil;
import com.robonobo.core.api.model.Stream;
import com.robonobo.core.api.model.Track;

@SuppressWarnings("serial")
public abstract class TrackListTableModel extends AbstractTableModel {
	static DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
		
	String[] colNames = { " "/*StatusIcon*/, "Title", "Artist", "Album", "Track", "Year", "Time", "Size", "Status", "Download", "Upload", "Added to Library", "Stream Id" };
	Pattern firstNumPat = Pattern.compile("^\\s*(\\d*).*$");

	public int getColumnCount() {
		return colNames.length;
	}

	@Override
	public String getColumnName(int column) {
		return colNames[column];
	}

	public int getRowCount() {
		return numTracks();
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		switch(columnIndex) {
		case 0:
			return Track.PlaybackStatus.class;
		case 1:
		case 2:
		case 3:
		case 5:
		case 7:
		case 9:
		case 10:
		case 11:
		case 12:
			return String.class;
		case 4:
			return Integer.class;
		default:
			return Object.class;
		}
	}

	public Object getValueAt(int rowIndex, int columnIndex) {
		Track t = getTrack(rowIndex);
		if(t == null)
			return null;
		Stream s = t.getStream();
		switch(columnIndex) {
		case 0:
			return t.getPlaybackStatus();
		case 1:
			return s.getTitle();
		case 2:
			return s.getAttrValue("artist");
		case 3:
			return s.getAttrValue("album");
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
			if(rate == 0) {
				return null;
			}
			return FileUtil.humanReadableSize(rate)+"/s";
		case 10:
			rate = t.getUploadRate();
			if(rate == 0) {
				return null;
			}
			return FileUtil.humanReadableSize(rate)+"/s";
		case 11:
			return df.format(t.getDateAdded());
		case 12:
			return s.getStreamId();
		}
		return null;
	}

	private Integer getTrackNumber(Stream s) {
		String trackStr = s.getAttrValue("track");
		if(trackStr == null || trackStr.length() == 0)
			return null;
		Matcher m = firstNumPat.matcher(trackStr);
		if(!m.matches())
			return null;
		return Integer.parseInt(m.group(1));
	}

	// By default we just hide the stream id, subclasses hide more
	public int[] hiddenCols() {
		return new int[] { 4, 12 };
	}

	public abstract Track getTrack(int index);

	public abstract String getStreamId(int index);
	
	public abstract int getTrackIndex(String streamId);

	public abstract int numTracks();
	
	/** Are we allowed to delete tracks from this tracklist? */
	public abstract boolean allowDelete();
	
	public abstract void deleteTracks(List<String> streamIds);
}
