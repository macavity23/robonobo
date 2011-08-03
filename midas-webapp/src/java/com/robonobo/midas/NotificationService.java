package com.robonobo.midas;

import com.robonobo.midas.model.MidasPlaylist;
import com.robonobo.midas.model.MidasUser;

public interface NotificationService {
	public void playlistUpdated(MidasUser updater, MidasPlaylist p);

	public void addedToLibrary(MidasUser user, int numTrax);
}