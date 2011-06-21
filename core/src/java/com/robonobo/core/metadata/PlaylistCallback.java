package com.robonobo.core.metadata;

import com.robonobo.core.api.model.Playlist;

public interface PlaylistCallback {
	public void success(Playlist p);
	public void error(long playlistId, Exception ex);
}
