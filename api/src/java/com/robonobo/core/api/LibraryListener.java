package com.robonobo.core.api;

import java.util.Date;
import java.util.Map;

import com.robonobo.core.api.model.Comment;

public interface LibraryListener {
	public void myLibraryUpdated();
	public void friendLibraryReady(long userId, int numUnseen);
	/**
	 * @param newTracks Copy this rather than assigning or altering it
	 */
	public void friendLibraryUpdated(long userId, int numUnseen, Map<String, Date> newTracks);
	/** Map<Comment, haveSeenBefore> */
	public void gotLibraryComments(long userId, Map<Comment, Boolean> comments);

}
