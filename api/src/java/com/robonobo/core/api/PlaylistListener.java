package com.robonobo.core.api;

import java.util.Map;

import com.robonobo.core.api.model.Comment;
import com.robonobo.core.api.model.Playlist;

public interface PlaylistListener {
	public void playlistChanged(Playlist p);
	/** Map<Comment, haveSeenBefore> */
	public void gotPlaylistComments(long plId, Map<Comment, Boolean> comments);
}
