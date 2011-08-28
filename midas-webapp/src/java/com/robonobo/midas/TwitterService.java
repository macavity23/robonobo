package com.robonobo.midas;

import java.io.IOException;

import com.robonobo.core.api.model.Playlist;
import com.robonobo.midas.model.MidasUserConfig;

public interface TwitterService {
	public abstract void postPlaylistUpdateToTwitter(MidasUserConfig muc, Playlist p, String msg);

	public abstract void postToTwitter(MidasUserConfig muc, String msg);

	public abstract void postSpecialPlaylistToTwitter(MidasUserConfig muc, long uid, String plName, String msg) throws IOException;

}