package com.robonobo.midas.dao;

import com.robonobo.midas.model.MidasPlaylist;

public interface PlaylistDao {

	/**
	 * Returns the playlist id that is currently highest, or 0 if no playlists
	 */
	public abstract long getHighestPlaylistId();

	public abstract void deletePlaylist(MidasPlaylist playlist);

	public abstract MidasPlaylist loadPlaylist(long playlistId);

	public abstract void savePlaylist(MidasPlaylist playlist);

}