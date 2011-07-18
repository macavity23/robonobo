package com.robonobo.midas;

import static com.robonobo.common.util.TextUtil.*;
import static com.robonobo.common.util.TimeUtil.*;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import com.restfb.*;
import com.restfb.types.FacebookType;
import com.restfb.types.User;
import com.robonobo.common.concurrent.CatchingRunnable;
import com.robonobo.core.api.model.Playlist;
import com.robonobo.core.api.model.UserConfig;
import com.robonobo.midas.dao.UserConfigDao;
import com.robonobo.midas.dao.UserDao;
import com.robonobo.midas.model.MidasUser;
import com.robonobo.midas.model.MidasUserConfig;
import com.robonobo.remote.service.MidasService;
import com.twmacinta.util.MD5;

@Service("facebook")
public class FacebookServiceImpl implements InitializingBean, FacebookService {
	@Autowired
	AppConfig appConfig;
	@Autowired
	UserConfigDao userConfigDao;
	@Autowired
	UserDao userDao;
	@Autowired
	MidasService midas;
	@Autowired
	PlatformTransactionManager transactionManager;
	Log log = LogFactory.getLog(getClass());
	static final long MIN_MS_BETWEEN_FB_HITS = 1000;
	Lock rateLimitLock = new ReentrantLock(true);
	Date lastHitFBTime = new Date(0);
	String facebookVerifyTok;

	public void afterPropertiesSet() throws Exception {
		Random rand = new Random();
		MD5 flarp = new MD5();
		flarp.Update(rand.nextInt());
		facebookVerifyTok = flarp.asHex();
		subscribeToFBUpdates();
		getUpdatedFBInfoForAllUsers();
	}

	/**
	 * Updates who knows whom based on facebook friends
	 * 
	 * @param oldUserCfg
	 *            If this is not null and the facebook id has not changed in the newUserCfg, then nothing will be
	 *            updated
	 * @throws FacebookException
	 */
	@Override
	@Transactional(rollbackFor = Exception.class)
	public void updateFriends(MidasUser user, MidasUserConfig oldUserCfg, MidasUserConfig newUserCfg) {
		if (!newUserCfg.getItems().containsKey("facebookId"))
			return;
		if (oldUserCfg != null && oldUserCfg.getItem("facebookId") != null
				&& oldUserCfg.getItem("facebookId").equals(newUserCfg.getItem("facebookId")))
			return;
		// TODO removing friends?
		// Get this user's list of friends from facebook
		FacebookClient fbCli = new RateLimitFBClient(newUserCfg.getItem("facebookAccessToken"));
		Connection<User> friends;
		try {
			friends = fbCli.fetchConnection("me/friends", User.class);
		} catch (FacebookException e) {
			log.error("Caught exception fetching friends from facebook for user id " + user.getUserId(), e);
			return;
		}
		// For each friend in the list, see if there is a robonobo user with that facebook id
		boolean changedUser = false;
		for (User fbFriend : friends.getData()) {
			UserConfig uc = userConfigDao.getUserConfig("facebookId", fbFriend.getId());
			// If so, make a friendship between them
			if (uc != null) {
				long friendId = uc.getUserId();
				MidasUser friend = userDao.getById(friendId);
				if ((!user.getFriendIds().contains(friendId)) || (!friend.getFriendIds().contains(user.getUserId()))) {
					user.getFriendIds().add(friendId);
					friend.getFriendIds().add(user.getUserId());
					userDao.save(friend);
					changedUser = true;
				}
			}
		}
		if (changedUser)
			userDao.save(user);
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public void updateFacebookName(String fbId, String newName) {
		MidasUserConfig muc = userConfigDao.getUserConfig("facebookId", fbId);
		if (muc == null)
			return;
		MidasUser user = midas.getUserById(muc.getUserId());
		if (user.getFriendlyName().equals(newName))
			return;
		// We only update the user's name if their old name was the same as their facebook name
		if (user.getFriendlyName().equals(muc.getItem("facebookName"))) {
			user.setFriendlyName(newName);
			midas.saveUser(user);
		}
		muc.putItem("facebookName", newName);
		midas.putUserConfig(muc);

	}

	@Override
	public MidasUserConfig getUserConfigByFacebookId(String fbId) {
		return userConfigDao.getUserConfig("facebookId", fbId);
	}

	@Override
	public String getFacebookVerifyTok() {
		return facebookVerifyTok;
	}

	@Override
	public void postPlaylistUpdateToFacebook(MidasUserConfig muc, Playlist p, String msg) throws IOException {
		String fbId = muc.getItem("facebookId");
		if (fbId == null)
			return;
		if(msg == null)
			msg = "I updated my playlist '" + p.getTitle() + "': ";
		String playlistUrl = appConfig.getInitParam("playlistShortUrlBase") + p.getPlaylistId();
		msg += playlistUrl;
		postToFacebook(muc, msg);
	}

	@Override
	public void postToFacebook(MidasUserConfig muc, String msg) throws IOException {
		String fbAccessTok = muc.getItem("facebookAccessToken");
		if (fbAccessTok == null)
			return;
		FacebookClient fbCli = new RateLimitFBClient(fbAccessTok);
		FacebookType response;
		try {
			response = fbCli.publish("me/feed", FacebookType.class, Parameter.with("message", msg));
		} catch (FacebookException e) {
			throw new IOException(e);
		}
		log.debug(response);
	}

	protected void subscribeToFBUpdates() throws IOException {
		final String authTokUrl = appConfig.getInitParam("facebookAuthTokenUrl");
		if (isEmpty(authTokUrl)) {
			log.info("Not subscribing to realtime updates from facebook, facebookAuthTokenUrl not set");
			return;
		}
		Thread t = new Thread(new CatchingRunnable() {
			public void doRun() throws Exception {
				// Wait for 60 secs to let everything start - facebook needs the callback url to be there
				Thread.sleep(60000L);

				log.info("Getting facebook oauth token for realtime updates");
				HttpClient httpCli = new HttpClient();
				GetMethod get = new GetMethod(authTokUrl);
				httpCli.executeMethod(get);
				Pattern fbAccessTokPattern = Pattern.compile("^access_token=(.*)$");
				Matcher m = fbAccessTokPattern.matcher(get.getResponseBodyAsString());
				if (!m.matches())
					throw new IOException("Facebook returned invalid body for access token request: "
							+ get.getResponseBodyAsString());
				String accessTok = m.group(1);
				String subsUrl = appConfig.getInitParam("facebookSubscriptionsUrl") + "?access_token=" + accessTok;
				PostMethod post = new PostMethod(subsUrl);
				post.addParameter("object", "user");
				post.addParameter("fields", "name,friends");
				post.addParameter("callback_url", appConfig.getInitParam("facebookCallbackUrl"));
				post.addParameter("verify_token", facebookVerifyTok);
				log.info("Subscribing to Facebook realtime updates");
				httpCli.executeMethod(post);
				if (post.getStatusCode() != HttpStatus.SC_OK)
					throw new IOException("Facebook returned unexpected status code for subscription: "
							+ post.getStatusCode() + ", with body: " + post.getResponseBodyAsString());
				log.info("Facebook subscription updated ok");
			}
		});
		t.start();
	}

	protected void getUpdatedFBInfoForAllUsers() {
		Thread t = new Thread(new CatchingRunnable() {
			public void doRun() throws Exception {
				// Wait for 90 secs to let everything settle down
				Thread.sleep(90000L);
				List<MidasUserConfig> fbUserCfgs = userConfigDao.getUserConfigsWithKey("facebookId");
				log.info("Getting updated facebook info for " + fbUserCfgs.size() + " users");
				TransactionTemplate tt = new TransactionTemplate(transactionManager);
				for (final MidasUserConfig userCfg : fbUserCfgs) {
					tt.execute(new TransactionCallbackWithoutResult() {
						protected void doInTransactionWithoutResult(TransactionStatus arg0) {
							FacebookClient fbCli = new RateLimitFBClient(userCfg.getItem("facebookAccessToken"));
							User fbUser;
							try {
								fbUser = fbCli.fetchObject("me", User.class, Parameter.with("fields", "name"));
							} catch (FacebookException e) {
								log.error("Error fetching from facebook", e);
								return;
							}
							String name = fbUser.getName();
							updateFacebookName(userCfg.getItem("facebookId"), name);
							MidasUser user = midas.getUserById(userCfg.getUserId());
							updateFriends(user, null, userCfg);
						}
					});
				}
				log.info("Finished getting updated facebook info");
			}
		});
		t.start();
	}

	@Override
	public FacebookClient getFacebookClient(String accessToken) {
		return new RateLimitFBClient(accessToken);
	}

	// rateLimitLock is fair, so threads will queue up here waiting to be allowed to hit fb
	protected void rateLimit() {
		rateLimitLock.lock();
		try {
			long elapsed = now().getTime() - lastHitFBTime.getTime();
			if (elapsed < MIN_MS_BETWEEN_FB_HITS)
				Thread.sleep(MIN_MS_BETWEEN_FB_HITS - elapsed);
			lastHitFBTime = now();
		} catch (InterruptedException justReturn) {
		} finally {
			rateLimitLock.unlock();
		}
	}

	class RateLimitFBClient extends DefaultFacebookClient {
		public RateLimitFBClient(String accessToken) {
			super(accessToken);
		}

		@Override
		public <T> Connection<T> fetchConnection(String connection, Class<T> connectionType, Parameter... parameters)
				throws FacebookException {
			rateLimit();
			return super.fetchConnection(connection, connectionType, parameters);
		}

		@Override
		public <T> T fetchObject(String object, Class<T> objectType, Parameter... parameters) throws FacebookException {
			rateLimit();
			return super.fetchObject(object, objectType, parameters);
		}
	}
}
