package com.robonobo.gui.model;

import java.util.Comparator;

import com.robonobo.core.api.model.Track;

class TrackComparator implements Comparator<Track> {
	StreamComparator sc = new StreamComparator();

	public int compare(Track t1, Track t2) {
		return sc.compare(t1.getStream(), t2.getStream());
	}
}