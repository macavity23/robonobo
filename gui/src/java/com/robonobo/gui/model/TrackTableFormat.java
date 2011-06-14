package com.robonobo.gui.model;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ca.odell.glazedlists.gui.TableFormat;

import com.robonobo.common.util.FileUtil;
import com.robonobo.common.util.TimeUtil;
import com.robonobo.core.api.model.Stream;
import com.robonobo.core.api.model.Track;

class TrackTableFormat implements TableFormat<Track> {
	static DateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	Pattern firstNumPat = Pattern.compile("^\\s*(\\d*).*$");
	String[] colNames = { " "/* StatusIcon */, "Title", "Artist", "Album", "Track", "Year", "Time", "Size", "Status", "Download", "Upload", "Added to Library", "Stream Id" };

	public int getColumnCount() {
		return colNames.length;
	}

	@Override
	public String getColumnName(int col) {
		return colNames[col];
	}

	@Override
	public Object getColumnValue(Track t, int col) {
		Stream s = t.getStream();
		switch (col) {
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
			Date addDate = t.getDateAdded();
			if (addDate == null)
				return null;
			return df.format(t.getDateAdded());
		case 12:
			return s.getStreamId();
		}
		return null;
	}

	Integer getTrackNumber(Stream s) {
		String trackStr = s.getAttrValue("track");
		if (trackStr == null || trackStr.length() == 0)
			return null;
		Matcher m = firstNumPat.matcher(trackStr);
		if (!m.matches())
			return null;
		return Integer.parseInt(m.group(1));
	}
}