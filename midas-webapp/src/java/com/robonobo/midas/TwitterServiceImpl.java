package com.robonobo.midas;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.scribe.builder.ServiceBuilder;
import org.scribe.builder.api.TwitterApi;
import org.scribe.model.*;
import org.scribe.oauth.OAuthService;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.robonobo.core.api.model.Playlist;
import com.robonobo.midas.model.MidasUser;
import com.robonobo.midas.model.MidasUserConfig;
import com.robonobo.remote.service.MidasService;

@Service("twitter")
public class TwitterServiceImpl implements InitializingBean, TwitterService{
	private static final int TWITTER_MSG_LIMIT = 140;
	@Autowired
	AppConfig appConfig;
	@Autowired
	MidasService midas;
	protected OAuthService postingService;
	Log log = LogFactory.getLog(getClass());
	
	@Override
	public void afterPropertiesSet() throws Exception {
		String apiKey = appConfig.getInitParam("twitterApiKey");
		String apiSecret = appConfig.getInitParam("twitterApiSecret");
		postingService = new ServiceBuilder().provider(TwitterApi.class).apiKey(apiKey).apiSecret(apiSecret).build();
	}
	
	@Override
	public void postPlaylistUpdateToTwitter(MidasUserConfig muc, Playlist p, String msg) {
		String accTok = muc.getItem("twitterAccessToken");
		if(accTok == null)
			return;
		if(msg == null)
			msg = "I updated my playlist '" + p.getTitle() + "': ";
		String playlistUrl = appConfig.getInitParam("playlistShortUrlBase") + p.getPlaylistId();
		msg += playlistUrl;
		postToTwitter(muc, msg);
	}
	
	@Override
	public void postToTwitter(MidasUserConfig muc, String msg) {
		String accTok = muc.getItem("twitterAccessToken");
		if(accTok == null)
			return;
		// If we've space, pimp ourselves at the end
		String pimpMsg = appConfig.getInitParam("twitterPimpMsg");
		if(pimpMsg != null && (msg.length() + pimpMsg.length()) <= TWITTER_MSG_LIMIT)
			msg += pimpMsg;
		String accSecret = muc.getItem("twitterAccessSecret");
		Token accessToken = new Token(accTok, accSecret);
		OAuthRequest oaReq = new OAuthRequest(Verb.POST, "http://api.twitter.com/1/statuses/update.json");
		oaReq.addBodyParameter("status", msg);
		postingService.signRequest(accessToken, oaReq);
		MidasUser u = midas.getUserById(muc.getUserId());
		log.info("Posting to twitter for "+u.getEmail()+": "+msg);
		Response oaResp = oaReq.send();
		if(oaResp.getCode() != 200)
			log.error("Error posting playlist to twitter to uid "+muc.getUserId()+", error code was "+oaResp.getCode());
		// Well, that was easy
	}
}
