package com.robonobo.midas;

import java.io.IOException;
import java.util.List;

import com.robonobo.midas.model.*;

public interface NotificationService {
	public void playlistUpdated(MidasUser updater, MidasPlaylist p);

	public void addedToLibrary(MidasUser user, int numTrax);

	public abstract void newComment(MidasComment c) throws IOException;

	public abstract void lovesAdded(MidasUser user, List<String> artists) throws IOException;
}