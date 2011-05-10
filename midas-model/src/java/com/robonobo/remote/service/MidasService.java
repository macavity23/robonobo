package com.robonobo.remote.service;

import java.util.*;

import com.robonobo.core.api.model.Library;
import com.robonobo.midas.model.*;

public interface MidasService {
	public List<MidasUser> getAllUsers();
	
	public MidasUser getUserByEmail(String email);

	public MidasUser getUserById(long userId);

	public MidasUser createUser(MidasUser user);
	
	public void saveUser(MidasUser user);

	/**
	 * Returns the target user, but only the bits that are allowed to be seen by
	 * the requesting user
	 */
	public MidasUser getUserAsVisibleBy(MidasUser target, MidasUser requestor);

	public void deleteUser(long userId);
	
	public MidasPlaylist getPlaylistById(long playlistId);

	/**
	 * Returns the playlist with the playlistId set
	 */
	public MidasPlaylist newPlaylist(MidasPlaylist playlist);
	
	public void savePlaylist(MidasPlaylist playlist);

	public void deletePlaylist(MidasPlaylist playlist);

	public MidasStream getStreamById(String streamId);

	public void saveStream(MidasStream stream);

	public void deleteStream(MidasStream stream);
	
	/**
	 * This is for monitoring, ensures db connection is ok
	 */
	public Long countUsers();
	
	public MidasFriendRequest createOrUpdateFriendRequest(MidasUser requestor, MidasUser requestee, MidasPlaylist pl);
	
	public MidasFriendRequest getFriendRequest(String requestCode);
	
	/**
	 * Returns error message, null if no error
	 */
	public String acceptFriendRequest(MidasFriendRequest request);
	
	public void ignoreFriendRequest(MidasFriendRequest request);
	
	public List<MidasFriendRequest> getPendingFriendRequests(long userId);
	
	public MidasInvite createOrUpdateInvite(String email, MidasUser friend, MidasPlaylist pl);
	
	public MidasInvite getInvite(String inviteCode);
	
	public void deleteInvite(String inviteCode);
	
	/**
	 * @param since Pass null to get the whole library
	 */
	public Library getLibrary(MidasUser u, Date since);
	
	public void putLibrary(Library lib);
	
	public MidasUserConfig getUserConfig(MidasUser u);
	
	public void putUserConfig(MidasUserConfig config);

	public List<MidasPlaylist> getRecentPlaylists(long maxAgeMs);
}
