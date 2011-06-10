package com.robonobo.core.api;

import java.util.Collection;

import com.robonobo.core.api.model.Library;

public interface LibraryListener {
	/**
	 * @param lib The updated library, which includes the new tracks
	 * @param newTrackSids The stream ids of new tracks (if any)
	 */
	public void libraryChanged(Library lib, Collection<String> newTrackSids);
	public void myLibraryUpdated();
}
