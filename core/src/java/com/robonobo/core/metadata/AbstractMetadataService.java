package com.robonobo.core.metadata;

import java.util.Collection;
import java.util.Date;

import com.robonobo.core.api.model.*;
import com.robonobo.core.service.AbstractService;

public abstract class AbstractMetadataService extends AbstractService {
	/**
	 * 'Serial' means that all the objects in one request will be done before moving onto the next, 'Parallel' means that one object from each request will be done in turn
	 */
	public enum RequestFetchOrder {
		Serial, Parallel
	}

	protected RequestFetchOrder fetchOrder = RequestFetchOrder.Serial;

	public void setFetchOrder(RequestFetchOrder fetchOrder) {
		this.fetchOrder = fetchOrder;
	}

	@Override
	public String getProvides() {
		return "core.metadata";
	}

	public abstract void setCredentials(String username, String password);

	public abstract void fetchStreams(Collection<String> sids, StreamCallback handler);

	/**
	 * @param handler
	 *            on success, the passed stream may be null
	 */
	public abstract void putStream(Stream s, StreamCallback handler);

	/**
	 * @param handler on error, the user id will not be meaningful
	 */
	public abstract void fetchUserForLogin(String email, String password, UserCallback handler);

	public abstract void fetchUser(long userId, UserCallback handler);

	public abstract void fetchUsers(Collection<Long> userIds, UserCallback handler);

	public abstract void fetchUserConfig(long userId, UserConfigCallback handler);

	/**
	 * @param handler
	 *            on success, the passed userconfig may be null
	 */
	public abstract void updateUserConfig(UserConfig uc, UserConfigCallback handler);

	public abstract void fetchPlaylist(long playlistId, PlaylistCallback handler);

	public abstract void fetchPlaylists(Collection<Long> playlistIds, PlaylistCallback handler);

	public abstract void updatePlaylist(Playlist p, PlaylistCallback handler);

	/**
	 * @param handler
	 *            on success, the passed playlist may be null
	 */
	public abstract void postPlaylistUpdateToService(String service, long playlistId, String msg, PlaylistCallback handler);

	/**
	 * Will remove the logged-in user from the list of playlist owners, or delete the playlist if they are the only owner
	 * 
	 * @param handler
	 *            on success, the passed playlist will be null
	 */
	public abstract void deletePlaylist(Playlist p, PlaylistCallback handler);

	public abstract void sharePlaylist(Playlist p, Collection<Long> shareFriendIds, Collection<String> friendEmails, PlaylistCallback handler);

	public abstract void fetchLibrary(long userId, Date lastUpdated, LibraryCallback handler);

	/**
	 * @param handler
	 *            on success, the passed library may be null
	 */
	public abstract void addToLibrary(long userId, Library addedLib, LibraryCallback handler);

	/**
	 * @param handler
	 *            on success, the passed library may be null
	 */
	public abstract void deleteFromLibrary(long userId, Library delLib, LibraryCallback handler);

	public abstract void search(String query, int firstResult, SearchCallback handler);
}
