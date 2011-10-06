package com.robonobo.remote.service;

import java.io.IOException;
import java.util.*;

import com.google.protobuf.InvalidProtocolBufferException;
import com.robonobo.common.exceptions.SeekInnerCalmException;
import com.robonobo.core.api.model.Library;
import com.robonobo.core.api.model.Playlist;
import com.robonobo.core.api.proto.CoreApi.FriendRequestMsg;
import com.robonobo.core.api.proto.CoreApi.InviteMsg;
import com.robonobo.core.api.proto.CoreApi.PlaylistMsg;
import com.robonobo.core.api.proto.CoreApi.StreamMsg;
import com.robonobo.core.api.proto.CoreApi.UserConfigMsg;
import com.robonobo.core.api.proto.CoreApi.UserMsg;
import com.robonobo.midas.model.*;

/** The client end of a remote midas service
 * 
 * @author macavity */
public class RemoteMidasFacade extends JbossRemotingFacade implements MidasService {
	/** @param url
	 *            The jboss-remoting url to connect
	 * @param secret
	 *            The sekrit string that must be passed with all calls */
	public RemoteMidasFacade(String url, String secret) throws Exception {
		super(url, "midas", secret);
	}

	public void deletePlaylist(MidasPlaylist playlist) {
		invoke("deletePlaylist", playlist.toMsg().toByteArray(), null);
	}

	public void deleteStream(MidasStream stream) {
		invoke("deleteStream", stream.toMsg().toByteArray(), null);
	}

	public MidasPlaylist getPlaylistById(long playlistId) {
		byte[] arr = (byte[]) invoke("getPlaylistById", playlistId, null);
		return playlistFromByteArr(arr);
	}

	@Override
	public MidasPlaylist getPlaylistByUserIdAndTitle(long uid, String title) {
		byte[] arr = (byte[]) invoke("getPlaylistByUserIdAndTitle", uid, Arrays.asList(title));
		return playlistFromByteArr(arr);
	}

	@Override
	public MidasPlaylist newPlaylist(MidasPlaylist playlist) {
		// Don't do newPlaylist remotely
		throw new SeekInnerCalmException();
	}

	public MidasStream getStreamById(String streamId) {
		byte[] arr = (byte[]) invoke("getStreamById", streamId, null);
		return streamFromByteArr(arr);
	}

	public MidasUser getUserAsVisibleBy(MidasUser target, MidasUser requestor) {
		byte[] arr = (byte[]) invoke("getUserAsVisibleBy", target.toMsg(false).toByteArray(), Arrays.asList(requestor.toMsg(false).toByteArray()));
		return userFromByteArr(arr);
	}

	public MidasUser getUserByEmail(String email) {
		byte[] arr = (byte[]) invoke("getUserByEmail", email, null);
		return userFromByteArr(arr);
	}

	public MidasUser getUserById(long userId) {
		byte[] arr = (byte[]) invoke("getUserById", userId, null);
		return userFromByteArr(arr);
	}

	public List<MidasUser> getAllUsers() {
		// Arrrrrrrrrrr!
		byte[][] arrOfArrs = (byte[][]) invoke("getAllUsers", null, null);
		List<MidasUser> result = new ArrayList<MidasUser>(arrOfArrs.length);
		for (byte[] arr : arrOfArrs) {
			result.add(userFromByteArr(arr));
		}
		return result;
	}

	public MidasUser createUser(MidasUser user) {
		byte[] arr = (byte[]) invoke("createUser", user.toMsg(true).toByteArray(), null);
		return userFromByteArr(arr);
	}

	public void deleteUser(long userId) {
		invoke("deleteUser", userId, null);
	}

	public void savePlaylist(MidasPlaylist playlist) {
		invoke("savePlaylist", playlist.toMsg().toByteArray(), null);
	}

	public void saveStream(MidasStream stream) {
		invoke("saveStream", stream.toMsg().toByteArray(), null);
	}

	public void saveUser(MidasUser user) {
		invoke("saveUser", user.toMsg(true).toByteArray(), null);
	}

	public Long countUsers() {
		return (Long) invoke("countUsers", null, null);
	}

	public MidasFriendRequest createOrUpdateFriendRequest(MidasUser requestor, MidasUser requestee, MidasPlaylist pl) {
		byte[] arr = (byte[]) invoke("createOrUpdateFriendRequest",
				requestor.toMsg(false).toByteArray(),
				Arrays.asList(requestee.toMsg(false).toByteArray(), pl.toMsg().toByteArray()));
		if (arr == null)
			return null;
		return friendReqFromByteArr(arr);
	}

	public MidasFriendRequest getFriendRequest(String requestCode) {
		byte[] arr = (byte[]) invoke("getFriendRequest", requestCode, null);
		if (arr == null)
			return null;
		return friendReqFromByteArr(arr);
	}

	public String acceptFriendRequest(MidasFriendRequest fr) {
		return (String) invoke("acceptFriendRequest", fr.toMsg().toByteArray(), null);
	}

	public List<MidasFriendRequest> getPendingFriendRequests(long userId) {
		byte[][] arrOfArrs = (byte[][]) invoke("getPendingFriendRequests", userId, null);
		List<MidasFriendRequest> result = new ArrayList<MidasFriendRequest>(arrOfArrs.length);
		for (byte[] arr : arrOfArrs) {
			result.add(friendReqFromByteArr(arr));
		}
		return result;
	}

	public void ignoreFriendRequest(MidasFriendRequest request) {
		invoke("ignoreFriendRequest", request.toMsg().toByteArray(), null);
	}

	public MidasInvite createOrUpdateInvite(String email, MidasUser friend, MidasPlaylist pl) {
		byte[] arr = (byte[]) invoke("createOrUpdateInvite", email, Arrays.asList(friend.toMsg(false).toByteArray(), pl.toMsg().toByteArray()));
		if (arr == null)
			return null;
		return inviteFromByteArr(arr);
	}

	public void inviteAccepted(long acceptedUserId, String inviteCode) {
		invoke("inviteAccepted", acceptedUserId, Arrays.asList(inviteCode));
	}

	public MidasInvite getInvite(String inviteCode) {
		byte[] arr = (byte[]) invoke("getInvite", inviteCode, null);
		if (arr == null)
			return null;
		return inviteFromByteArr(arr);
	}

	public MidasInvite getInviteByEmail(String email) {
		byte[] arr = (byte[]) invoke("getInviteByEmail", email, null);
		if (arr == null)
			return null;
		return inviteFromByteArr(arr);
	}

	@Override
	public Library getLibrary(MidasUser u, Date since) {
		// We don't do library remoting yet
		throw new SeekInnerCalmException();
	}

	@Override
	public void putLibrary(Library lib) {
		// We don't do library remoting yet
		throw new SeekInnerCalmException();
	}

	@Override
	public MidasUserConfig getUserConfig(MidasUser u) {
		byte[] arr = (byte[]) invoke("getUserConfig", u.getUserId(), null);
		return userCfgFromByteArr(arr);
	}

	@Override
	public void putUserConfig(MidasUserConfig config) {
		invoke("putUserConfig", config.toMsg().toByteArray(), null);
	}

	@Override
	public void addFriends(long userId, List<Long> friendIds, List<String> friendEmails) {
		invoke("addFriends", userId, Arrays.asList(friendIds.toArray(), friendEmails.toArray()));
	}

	@Override
	public String requestAccountTopUp(long userId) {
		byte[] arr = (byte[]) invoke("requestTopUp", userId, null);
		return new String(arr);
	}

	private MidasPlaylist playlistFromByteArr(byte[] arr) {
		if (arr == null)
			return null;
		PlaylistMsg msg;
		try {
			msg = PlaylistMsg.newBuilder().mergeFrom(arr).build();
		} catch (InvalidProtocolBufferException e) {
			return null;
		}
		return new MidasPlaylist(msg);
	}

	private MidasUser userFromByteArr(byte[] arr) {
		if (arr == null)
			return null;
		UserMsg msg;
		try {
			msg = UserMsg.newBuilder().mergeFrom(arr).build();
		} catch (InvalidProtocolBufferException e) {
			return null;
		}
		return new MidasUser(msg);
	}

	private MidasUserConfig userCfgFromByteArr(byte[] arr) {
		if (arr == null)
			return null;
		UserConfigMsg msg;
		try {
			msg = UserConfigMsg.newBuilder().mergeFrom(arr).build();
		} catch (InvalidProtocolBufferException e) {
			return null;
		}
		return new MidasUserConfig(msg);
	}

	private MidasStream streamFromByteArr(byte[] arr) {
		if (arr == null)
			return null;
		StreamMsg msg;
		try {
			msg = StreamMsg.newBuilder().mergeFrom(arr).build();
		} catch (InvalidProtocolBufferException e) {
			return null;
		}
		return new MidasStream(msg);
	}

	private MidasInvite inviteFromByteArr(byte[] arr) {
		InviteMsg msg;
		try {
			msg = InviteMsg.newBuilder().mergeFrom(arr).build();
		} catch (InvalidProtocolBufferException e) {
			return null;
		}
		return new MidasInvite(msg);
	}

	private MidasFriendRequest friendReqFromByteArr(byte[] arr) {
		FriendRequestMsg msg;
		try {
			msg = FriendRequestMsg.newBuilder().mergeFrom(arr).build();
		} catch (InvalidProtocolBufferException e) {
			return null;
		}
		return new MidasFriendRequest(msg);
	}

	public List<MidasPlaylist> getRecentPlaylists(long maxAgeMs) {
		return null;
	}

	public void deleteComment(MidasComment c) {
		// No comments via remote
	}

	public void saveComment(MidasComment c) {
		// No comments via remote
	}

	public MidasComment newCommentForLibrary(MidasComment comment, long userId) {
		// No comments via remote
		return null;
	}

	public MidasComment newCommentForPlaylist(MidasComment comment, long playlistId) {
		// No comments via remote
		return null;
	}

	public List<MidasComment> getCommentsForLibrary(long uid, Date since) {
		// No comments via remote
		return null;
	}

	public List<MidasComment> getCommentsForPlaylist(long plId, Date since) {
		// No comments via remote
		return null;
	}

	public MidasComment getComment(long commentId) {
		return null;
	}

	public void lovesChanged(MidasUser u, Playlist oldP, Playlist newP) throws IOException {
		// No loves via remote
	}
}
