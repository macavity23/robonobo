package com.robonobo.core.api;

import java.util.Set;

import com.robonobo.core.api.model.Library;

public interface LibraryListener {
	/**
	 * @param lib The updated library, which includes the new tracks
	 * @param newTrackSids The stream ids of new tracks (if any)
	 */
	public void libraryChanged(Library lib, Set<String> newTrackSids);
	public void myLibraryUpdated();
}
