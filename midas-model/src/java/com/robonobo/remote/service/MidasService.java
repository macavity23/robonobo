package com.robonobo.remote.service;

import java.util.Date;
import java.util.List;

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

	public MidasPlaylist getPlaylistByUserIdAndTitle(long uid, String title);
		
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
	
	public void inviteAccepted(long acceptedUserId, String inviteCode);
	
	/**
	 * @param since Pass null to get the whole library
	 */
	public Library getLibrary(MidasUser u, Date since);
	
	public void putLibrary(Library lib);
	
	public MidasUserConfig getUserConfig(MidasUser u);
	
	public void putUserConfig(MidasUserConfig config);

	public List<MidasPlaylist> getRecentPlaylists(long maxAgeMs);
	
	public void addFriends(long userId, List<Long> friendIds, List<String> friendEmails);
	
	/**
	 * Returns message to be displayed to the requesting user
	 */
	public String requestAccountTopUp(long userId);

	public abstract MidasInvite getInviteByEmail(String email);

	public abstract void deleteComment(MidasComment c);

	public abstract void saveComment(MidasComment c);

	public abstract MidasComment newCommentForLibrary(MidasComment comment, long userId);

	public abstract MidasComment newCommentForPlaylist(MidasComment comment, long playlistId);

	public abstract List<MidasComment> getCommentsForLibrary(long uid, Date since);

	public abstract List<MidasComment> getCommentsForPlaylist(long plId, Date since);

	public abstract MidasComment getComment(long commentId);
}
