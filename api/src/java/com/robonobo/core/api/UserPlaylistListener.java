package com.robonobo.core.api;

import com.robonobo.core.api.model.*;

/**
 * Notifies when users or playlists change
 */
public interface UserPlaylistListener {
	public void loggedIn();
	public void userChanged(User u);
	public void playlistChanged(Playlist p);
	public void libraryChanged(Library lib);
	public void allUsersAndPlaylistsUpdated();
	public void userConfigChanged(UserConfig cfg);
}
