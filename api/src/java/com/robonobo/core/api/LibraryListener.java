package com.robonobo.core.api;

import java.util.Date;
import java.util.Map;

public interface LibraryListener {
	public void myLibraryUpdated();
	public void friendLibraryReady(long userId, int numUnseen);
	/**
	 * @param newTracks Copy this rather than assigning or altering it
	 */
	public void friendLibraryUpdated(long userId, int numUnseen, Map<String, Date> newTracks);
}
