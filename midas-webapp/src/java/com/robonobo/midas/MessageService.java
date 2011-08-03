package com.robonobo.midas;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.robonobo.core.api.model.Playlist;
import com.robonobo.midas.model.*;

public interface MessageService {
	public void sendFriendRequest(MidasUser fromUser, MidasUser toUser, MidasPlaylist p) throws IOException;

	public MidasInvite sendInvite(MidasUser fromUser, String toEmail, MidasPlaylist p) throws IOException;

	public void sendPlaylistShare(MidasUser fromUser, MidasUser toUser, Playlist p) throws IOException;

	public abstract void sendWelcome(MidasUser newUser) throws IOException;

	public abstract void sendFriendConfirmation(MidasUser userSentFriendReq, MidasUser userApprovedFriendReq) throws IOException;

	public abstract void sendTopUpRequest(MidasUser requestor) throws IOException;

	public abstract void sendPlaylistNotification(MidasUser updateUser, MidasUser notifyUser, Playlist p) throws IOException;

	public abstract void sendLibraryNotification(MidasUser updateUser, MidasUser notifyUser, int numTrax) throws IOException;

	public abstract void sendCombinedNotification(MidasUser notifyUser, Map<MidasUser, Integer> libTraxAdded, Map<Long, List<Playlist>> playlists) throws IOException;
}