package com.robonobo.midas.dao;

import java.util.List;

import com.robonobo.midas.model.MidasPlaylist;

public interface PlaylistDao {

	/**
	 * Returns the playlist id that is currently highest, or 0 if no playlists
	 */
	public abstract long getHighestPlaylistId();

	public abstract void deletePlaylist(MidasPlaylist playlist);

	public abstract MidasPlaylist getPlaylistById(long playlistId);

	public abstract void savePlaylist(MidasPlaylist playlist);

	public abstract List<MidasPlaylist> getRecentPlaylists(long maxAgeMs);

	public abstract MidasPlaylist getPlaylistByUserIdAndTitle(long uid, String title);

}