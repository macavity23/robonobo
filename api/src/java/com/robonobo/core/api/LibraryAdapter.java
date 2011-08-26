package com.robonobo.core.api;

import java.util.Date;
import java.util.Map;

import com.robonobo.core.api.model.Comment;

public class LibraryAdapter implements LibraryListener {
	@Override
	public void myLibraryUpdated() {
	}

	@Override
	public void friendLibraryReady(long userId, int numUnseen) {
	}

	@Override
	public void friendLibraryUpdated(long userId, int numUnseen, Map<String, Date> newTracks) {
	}

	@Override
	public void gotLibraryComments(long userId, boolean anyUnread, Map<Comment, Boolean> comments) {
	}
}
