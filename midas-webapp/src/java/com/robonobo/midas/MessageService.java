package com.robonobo.midas;

import java.io.IOException;

import com.robonobo.core.api.model.Playlist;
import com.robonobo.midas.model.MidasPlaylist;
import com.robonobo.midas.model.MidasUser;

public interface MessageService {
	public void sendFriendRequest(MidasUser fromUser, MidasUser toUser, MidasPlaylist p) throws IOException;

	public void sendInvite(MidasUser fromUser, String toEmail, MidasPlaylist p) throws IOException;

	public void sendPlaylistShare(MidasUser fromUser, MidasUser toUser, Playlist p) throws IOException;

	public abstract void sendWelcome(MidasUser newUser) throws IOException;

	public abstract void sendFriendConfirmation(MidasUser userSentFriendReq, MidasUser userApprovedFriendReq) throws IOException;

	public abstract void sendTopUpRequest(MidasUser requestor) throws IOException;
}