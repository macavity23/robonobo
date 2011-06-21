package com.robonobo.gui.model;

import java.util.List;

import ca.odell.glazedlists.TextFilterator;

import com.robonobo.core.api.model.Stream;
import com.robonobo.core.api.model.Track;

class TrackFilterator implements TextFilterator<Track> {
	public void getFilterStrings(List<String> strs, Track t) {
		Stream s = t.getStream();
		strs.add(s.title);
		strs.add(s.artist);
		strs.add(s.album);
	}
}