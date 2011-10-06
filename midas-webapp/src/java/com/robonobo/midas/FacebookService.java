package com.robonobo.midas;

import java.io.IOException;

import com.restfb.FacebookClient;
import com.robonobo.core.api.model.Playlist;
import com.robonobo.midas.model.MidasUser;
import com.robonobo.midas.model.MidasUserConfig;

public interface FacebookService {
	/**
	 * Updates who knows who based on facebook friends
	 */
	public abstract void updateFriends(MidasUser user, MidasUserConfig newUserCfg);

	public abstract String getFacebookVerifyTok();

	public abstract MidasUserConfig getUserConfigByFacebookId(String fbId);

	public abstract void updateFacebookName(String fbId, String newName);

	public abstract void postToFacebook(MidasUserConfig muc, String msg) throws IOException;

	public abstract void postPlaylistUpdateToFacebook(MidasUserConfig muc, Playlist p, String msg) throws IOException;

	public abstract FacebookClient getFacebookClient(String accessToken);

	public abstract void postSpecialPlaylistToFacebook(MidasUserConfig muc, long uid, String plName, String msg) throws IOException;
}