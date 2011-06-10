package com.robonobo.core.metadata;

import java.util.*;

import com.robonobo.core.api.model.*;
import com.robonobo.core.metadata.*;
import com.robonobo.core.service.AbstractService;

public abstract class AbstractMetadataService extends AbstractService {

	@Override
	public String getProvides() {
		return "core.metadata";
	}

	public abstract void updateCredentials(String username, String password);
	
	public abstract void fetchStreams(Collection<String> sids, StreamHandler handler);

	/**
	 * @param handler on success, the passed stream may be null
	 */
	public abstract void putStream(Stream s, StreamHandler handler);
	
	public abstract void fetchUser(String email, String password, UserHandler handler);
	
	public abstract void fetchUser(long userId, UserHandler handler);

	public abstract void fetchUsers(Collection<Long> userIds, UserHandler handler);

	public abstract void fetchUserConfig(long userId, UserConfigHandler handler);

	/**
	 * @param handler on success, the passed userconfig may be null
	 */
	public abstract void updateUserConfig(UserConfig uc, UserConfigHandler handler);

	public abstract void fetchPlaylist(long playlistId, PlaylistHandler handler);

	public abstract void fetchPlaylists(Collection<Long> playlistIds, PlaylistHandler handler);

	public abstract void updatePlaylist(Playlist p, PlaylistHandler handler);
	
	/**
	 * @param handler on success, the passed playlist may be null
	 */
	public abstract void postPlaylistUpdateToService(String service, long playlistId, String msg, PlaylistHandler handler);
	
	/**
	 * Will remove the logged-in user from the list of playlist owners, or delete the playlist if they are the only owner
	 * @param handler on success, the passed playlist will be null
	 */
	public abstract void deletePlaylist(Playlist p, PlaylistHandler handler);
	
	public abstract void sharePlaylist(Playlist p, Collection<Long> shareFriendIds, Collection<String> friendEmails, PlaylistHandler handler);
	
	public abstract void fetchLibrary(long userId, Date lastUpdated, LibraryHandler handler);
	
	/**
	 * @param handler on success, the passed library may be null
	 */
	public abstract void addToLibrary(long userId, Library addedLib, LibraryHandler handler);
	
	/**
	 * @param handler on success, the passed library may be null
	 */
	public abstract void deleteFromLibrary(long userId, Library delLib, LibraryHandler handler);
	
	public abstract void search(String query, int firstResult, SearchHandler handler);
}
