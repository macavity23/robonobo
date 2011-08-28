package com.robonobo.midas;

import com.robonobo.core.api.model.Playlist;
import com.robonobo.midas.model.MidasInvite;
import com.robonobo.midas.model.MidasUser;

public interface EventService {
	public void userLoggedIn(MidasUser u);

	public void userRemainsOnline(MidasUser u);

	public void playlistCreated(MidasUser u, Playlist p);

	public void playlistUpdated(MidasUser u, Playlist p);

	public void playlistDeleted(MidasUser u, Playlist p);

	public void playlistShared(MidasUser sharer, Playlist p, MidasUser sharee);

	public void playlistPosted(MidasUser u, Playlist p, String postService);

	public void addedToLibrary(MidasUser u, int numTracks);

	public void removedFromLibrary(MidasUser u, int numTracks);

	public void inviteSent(MidasUser u, String email, MidasInvite i);

	public void inviteAccepted(MidasUser u, MidasInvite i);

	public void newUser(MidasUser u);

	public void friendRequestSent(MidasUser requestor, MidasUser requestee);

	public void friendRequestAccepted(MidasUser requestor, MidasUser requestee);

	public abstract void specialPlaylistPosted(MidasUser u, long uid, String plName, String postService);
}