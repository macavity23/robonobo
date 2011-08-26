package com.robonobo.core.api;

import java.util.Map;

import com.robonobo.core.api.model.Comment;
import com.robonobo.core.api.model.Playlist;

public class PlaylistAdapter implements PlaylistListener {
	@Override
	public void playlistChanged(Playlist p) {
	}

	@Override
	public void gotPlaylistComments(long plId, boolean anyUnread, Map<Comment, Boolean> comments) {
	}
}
