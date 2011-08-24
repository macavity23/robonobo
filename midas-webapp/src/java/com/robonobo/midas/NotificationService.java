package com.robonobo.midas;

import java.io.IOException;

import com.robonobo.midas.model.*;

public interface NotificationService {
	public void playlistUpdated(MidasUser updater, MidasPlaylist p);

	public void addedToLibrary(MidasUser user, int numTrax);

	public abstract void newComment(MidasComment c) throws IOException;
}