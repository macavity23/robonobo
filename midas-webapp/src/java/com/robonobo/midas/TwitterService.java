package com.robonobo.midas;

import com.robonobo.core.api.model.Playlist;
import com.robonobo.midas.model.MidasUserConfig;

public interface TwitterService {

	public abstract void postPlaylistCreateToTwitter(MidasUserConfig muc, Playlist p);

	public abstract void postPlaylistUpdateToTwitter(MidasUserConfig muc, Playlist p, String msg);

	public abstract void postToTwitter(MidasUserConfig muc, String msg);

}