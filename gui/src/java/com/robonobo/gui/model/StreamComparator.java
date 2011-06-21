package com.robonobo.gui.model;

import static com.robonobo.common.util.TextUtil.isNonEmpty;

import java.util.Comparator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.robonobo.core.api.model.Stream;

/**
 * Defines default order for streams.  Sorted first by artist, then album, then track number, then title, then stream id
 */
public class StreamComparator implements Comparator<Stream> {
	protected static Pattern trackNumPat = Pattern.compile("^(\\d+)");

	public StreamComparator() {
	}
	
	public int compare(Stream s1, Stream s2) {
		int result = compareByArtist(s1, s2);
		if(result != 0)
			return result;
		result = compareByAlbum(s1, s2);
		if(result != 0)
			return result;
		result = compareByTrackNum(s1, s2);
		if(result != 0)
			return result;
		result = compareByTitle(s1, s2);
		if(result != 0)
			return result;
		return compareByStreamId(s1, s2);
	}
	
	private static int compareByArtist(Stream s1, Stream s2) {
		return compareStrings(s1.artist, s2.artist);		
	}

	private static int compareByAlbum(Stream s1, Stream s2) {
		return compareStrings(s1.album, s2.album);
	}
	
	private static int compareByTitle(Stream s1, Stream s2) {
		return compareStrings(s1.title, s2.title);
	}
	
	private static int compareByStreamId(Stream s1, Stream s2) {
		return compareStrings(s1.streamId, s2.streamId);
	}
	
	private static int compareStrings(String s1, String s2) {
		if(isNonEmpty(s1)) {
			if(isNonEmpty(s2))
				return s1.compareTo(s2);
			else
				return 1;
		} else {
			if(isNonEmpty(s2))
				return -1;
			else
				return 0;
		}
	}
	
	private static int compareByTrackNum(Stream s1, Stream s2) {
		return (s1.track - s2.track);
	}
}
