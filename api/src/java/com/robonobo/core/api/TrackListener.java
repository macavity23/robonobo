package com.robonobo.core.api;

import java.util.Collection;

import com.robonobo.core.api.model.Track;

public interface TrackListener {
	public void trackUpdated(String streamId, Track t);
	public void tracksUpdated(Collection<Track> trax);
	public void allTracksLoaded();
}
