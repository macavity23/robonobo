package com.robonobo.core.api;

import java.util.Collection;

public interface TrackListener {
	public void trackUpdated(String streamId);
	public void tracksUpdated(Collection<String> streamIds);
	public void allTracksLoaded();
}
