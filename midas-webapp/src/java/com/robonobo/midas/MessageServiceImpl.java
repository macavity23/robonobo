package com.robonobo.midas;

import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.mail.HtmlEmail;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.context.ServletContextAware;

import com.robonobo.common.util.TextUtil;
import com.robonobo.core.api.model.Playlist;
import com.robonobo.midas.dao.PlaylistDao;
import com.robonobo.midas.dao.UserDao;
import com.robonobo.midas.model.*;
import com.robonobo.remote.service.MidasService;

import freemarker.cache.FileTemplateLoader;
import freemarker.template.*;

@Service("message")
@SuppressWarnings({ "rawtypes", "unchecked" })
public class MessageServiceImpl implements MessageService, InitializingBean, DisposableBean, ServletContextAware {
	private String smtpHost;
	private int smtpPort;
	private boolean useTls;
	private String smtpUser;
	private String smtpPwd;
	private String rbnbUrl;
	private Configuration freemarkerCfg;
	private ExecutorService executor;
	Log log = LogFactory.getLog(getClass());
	@Autowired
	AppConfig appConfig;
	@Autowired
	MidasService midas;
	@Autowired
	UserDao userDao;
	@Autowired
	PlaylistDao playlistDao;

	public MessageServiceImpl() {
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		smtpHost = appConfig.getInitParam("smtpHost");
		if (smtpHost == null) {
			log.error("SMTP host not set - no mails will be sent!");
			return;
		}
		smtpPort = Integer.parseInt(appConfig.getInitParam("smtpPort"));
		useTls = Boolean.parseBoolean(appConfig.getInitParam("smtpUseTls"));
		smtpUser = appConfig.getInitParam("smtpUser");
		smtpPwd = appConfig.getInitParam("smtpPwd");
		rbnbUrl = appConfig.getInitParam("rbnbUrl");
		log.warn("Starting mail threadpool");
		executor = Executors.newFixedThreadPool(8);
	}

	@Override
	public void setServletContext(ServletContext sc) {
		// Set up freemarker
		freemarkerCfg = new Configuration();
		freemarkerCfg.setServletContextForTemplateLoading(sc, "WEB-INF/freemarker");
		freemarkerCfg.setObjectWrapper(new DefaultObjectWrapper());
	}

	@Override
	public void destroy() throws Exception {
		log.warn("Shutting down mail threadpool");
		executor.shutdown();
		log.warn("Mail threadpool shutdown");
	}

	private Map newModel(String toName, String toEmail) {
		Map result = new HashMap();
		result.put("rbnbUrl", rbnbUrl);
		result.put("toName", toName);
		result.put("toEmail", toEmail);
		return result;
	}

	@Override
	public void sendWelcome(MidasUser newUser) throws IOException {
		Map model = newModel(newUser.getFriendlyName(), newUser.getEmail());
		model.put("toUser", newUser);
		sendMail(null, null, newUser.getEmail(), newUser.getFriendlyName(), newUser.getFriendlyName() + ", welcome to robonobo", "welcome", model);
	}

	@Override
	public void sendFriendRequest(MidasUser requestor, MidasUser requestee, MidasPlaylist p) throws IOException {
		MidasFriendRequest friendReq = midas.createOrUpdateFriendRequest(requestor, requestee, p);
		Map model = newModel(requestee.getFriendlyName(), requestee.getEmail());
		model.put("fromUser", requestor);
		model.put("acceptUrl", rbnbUrl + "friendrequest/" + friendReq.getRequestCode());
		model.put("playlist", p);
		sendMail(requestor.getEmail(), requestor.getFriendlyName(), requestee.getEmail(), requestee.getFriendlyName(), requestor.getFriendlyName()
				+ " would like to be your friend on robonobo", "friendrequest", model);
	}

	@Override
	public MidasInvite sendInvite(MidasUser fromUser, String toEmail, MidasPlaylist p) throws IOException {
		MidasInvite invite = midas.createOrUpdateInvite(toEmail, fromUser, p);
		Map model = newModel(null, toEmail);
		model.put("fromUser", fromUser);
		model.put("inviteUrl", rbnbUrl + "invite/" + invite.getInviteCode());
		model.put("playlist", p);
		sendMail(fromUser.getEmail(), fromUser.getFriendlyName(), toEmail, null, fromUser.getFriendlyName() + " has invited you to robonobo", "invite", model);
		return invite;
	}

	@Override
	public void sendPlaylistShare(MidasUser fromUser, MidasUser toUser, Playlist p) throws IOException {
		Map model = newModel(toUser.getFriendlyName(), toUser.getEmail());
		model.put("fromUser", fromUser);
		model.put("playlist", p);
		model.put("playlistUrl", playlistUrl(p));
		sendMail(fromUser.getEmail(),
				fromUser.getFriendlyName(),
				toUser.getEmail(),
				toUser.getFriendlyName(),
				fromUser.getFriendlyName() + " has shared a playlist with you",
				"shareplaylist",
				model);
	}

	@Override
	public void sendPlaylistNotification(MidasUser updateUser, MidasUser notifyUser, Playlist p) throws IOException {
		Map model = newModel(notifyUser.getFriendlyName(), notifyUser.getEmail());
		model.put("updateUser", updateUser);
		model.put("playlist", p);
		model.put("playlistUrl", playlistUrl(p));
		String subject = updateUser.getFriendlyName() + " updated their playlist '" + p.getTitle() + "'";
		sendMail(updateUser.getEmail(), updateUser.getFriendlyName(), notifyUser.getEmail(), notifyUser.getFriendlyName(), subject, "playlist-notif", model);
	}

	@Override
	public void sendLovesNotification(MidasUser updateUser, MidasUser notifyUser, List<String> artists) throws IOException {
		Map model = newModel(notifyUser.getFriendlyName(), notifyUser.getEmail());
		model.put("updateUser", updateUser);
		model.put("artists", artists);
		model.put("lovesUrl", lovesUrl(updateUser.getUserId()));
		String subject = updateUser.getFriendlyName() + " loves " + TextUtil.numItems(artists, "new artist");
		sendMail(updateUser.getEmail(), updateUser.getFriendlyName(), notifyUser.getEmail(), notifyUser.getFriendlyName(), subject, "loves-notif", model);
	}

	private String playlistUrl(Playlist p) {
		return appConfig.getInitParam("shortUrlBase") + "p/" + Long.toHexString(p.getPlaylistId());
	}

	private String lovesUrl(long uid) {
		return appConfig.getInitParam("shortUrlBase") + "sp/" + uid + "/loves";
	}

	@Override
	public void sendLibraryNotification(MidasUser updateUser, MidasUser notifyUser, int numTrax) throws IOException {
		Map model = newModel(notifyUser.getFriendlyName(), notifyUser.getEmail());
		model.put("updateUser", updateUser);
		model.put("numTrax", numTrax);
		sendMail(updateUser.getEmail(), updateUser.getFriendlyName(), notifyUser.getEmail(), notifyUser.getFriendlyName(), updateUser.getFriendlyName()
				+ " added to their music library", "library-notif", model);
	}

	@Override
	public void sendCommentNotificationForPlaylist(MidasUser owner, MidasUser commentUser, MidasPlaylist p) throws IOException {
		if (owner.getUserId() == commentUser.getUserId())
			return;
		sendCommentNotification(owner, commentUser, "in your playlist '" + p.getTitle() + "'");
	}

	@Override
	public void sendCommentNotificationForLibrary(MidasUser libUser, MidasUser commentUser) throws IOException {
		if (libUser.getUserId() == commentUser.getUserId())
			return;
		sendCommentNotification(libUser, commentUser, "in your music library");
	}

	private void sendCommentNotification(MidasUser updateUser, MidasUser commentUser, String whereIsComment) throws IOException {
		Map model = newModel(updateUser.getFriendlyName(), updateUser.getEmail());
		model.put("commentUser", commentUser);
		model.put("whereIsComment", whereIsComment);
		sendMail(commentUser.getEmail(), commentUser.getFriendlyName(), updateUser.getEmail(), updateUser.getFriendlyName(), commentUser.getFriendlyName() + " commented "
				+ whereIsComment, "comment", model);
	}

	@Override
	public void sendReplyNotificationForPlaylist(MidasUser origUser, MidasUser replyUser, MidasPlaylist p) throws IOException {
		if (origUser.getUserId() == replyUser.getUserId())
			return;
		sendCommentReplyNotification(origUser, replyUser, "in the playlist '" + p.getTitle() + "'");
	}

	@Override
	public void sendReplyNotificationForLibrary(MidasUser origUser, MidasUser replyUser, long libUserId) throws IOException {
		if (origUser.getUserId() == replyUser.getUserId())
			return;
		String whereIs;
		if (libUserId == origUser.getUserId())
			whereIs = "in your music library";
		else if (libUserId == replyUser.getUserId())
			whereIs = "in their music library";
		else {
			MidasUser libUser = userDao.getById(libUserId);
			whereIs = "in " + libUser.getFriendlyName() + "'s library";
		}
		sendCommentReplyNotification(origUser, replyUser, whereIs);
	}

	private void sendCommentReplyNotification(MidasUser origUser, MidasUser replyUser, String whereIsComment) throws IOException {
		Map model = newModel(origUser.getFriendlyName(), origUser.getEmail());
		model.put("replyUser", replyUser);
		model.put("whereIsComment", whereIsComment);
		sendMail(replyUser.getEmail(), replyUser.getFriendlyName(), origUser.getEmail(), origUser.getFriendlyName(), replyUser.getFriendlyName()
				+ " replied to your robonobo comment", "commentreply", model);
	}

	@Override
	public void sendCombinedNotification(MidasUser notifyUser, Map<MidasUser, Integer> libTraxAdded, Map<Long, List<Playlist>> playlists, Map<Long, List<String>> loveArtists)
			throws IOException {
		List<Map<String, Object>> updateUsers = new ArrayList<Map<String, Object>>();
		boolean havePlaylistsOrLoves = false;
		for (MidasUser updateUser : libTraxAdded.keySet()) {
			Map<String, Object> userMap = new HashMap<String, Object>();
			userMap.put("friendlyName", updateUser.getFriendlyName());
			userMap.put("email", updateUser.getEmail());
			userMap.put("numLibTrax", libTraxAdded.get(updateUser));
			List<Playlist> pObjList = playlists.get(updateUser.getUserId());
			List<Map<String, String>> pl = new ArrayList<Map<String, String>>();
			for (Playlist p : pObjList) {
				havePlaylistsOrLoves = true;
				Map<String, String> pm = new HashMap<String, String>();
				String title = p.getTitle();
				pm.put("title", title);
				pm.put("url", playlistUrl(p));
				pl.add(pm);
			}
			userMap.put("playlists", pl);
			List<String> ll = loveArtists.get(updateUser.getUserId());
			userMap.put("loves", ll);
			if (ll.size() > 0) {
				havePlaylistsOrLoves = true;
				userMap.put("lovesUrl", lovesUrl(updateUser.getUserId()));
			}
			updateUsers.add(userMap);
		}
		Map model = newModel(notifyUser.getFriendlyName(), notifyUser.getEmail());
		model.put("users", updateUsers);
		model.put("havePlaylistsOrLoves", havePlaylistsOrLoves);
		sendMail(null, null, notifyUser.getEmail(), notifyUser.getFriendlyName(), "Your friends have added music to robonobo", "combined-notif", model);
	}

	@Override
	public void sendFriendConfirmation(MidasUser userSentFriendReq, MidasUser userConfirmedFriendReq) throws IOException {
		Map model = newModel(userSentFriendReq.getFriendlyName(), userSentFriendReq.getEmail());
		model.put("fromUser", userConfirmedFriendReq);
		sendMail(userConfirmedFriendReq.getEmail(),
				userConfirmedFriendReq.getFriendlyName(),
				userSentFriendReq.getEmail(),
				userSentFriendReq.getFriendlyName(),
				userConfirmedFriendReq.getFriendlyName() + " confirmed your robonobo friend request",
				"friendconfirm",
				model);
	}

	@Override
	public void sendTopUpRequest(MidasUser requestor) throws IOException {
		Map model = new HashMap();
		model.put("user", requestor);
		sendMail(null, null, "info@robonobo.com", "rbnb topups", "TopUp Request", "topup", model);
	}

	@Override
	public void sendNewUserNotification(MidasUser user) throws IOException {
		Map model = new HashMap();
		model.put("user", user);
		sendMail(null, null, "info@robonobo.com", "rbnb admin", "New user: "+user.getEmail(), "newusernotif", model);
	}

	private void sendMail(String replyToAddr, String replyToName, String toAddr, String toName, String subject, String templateBase, Map model) throws IOException {
		if (smtpHost == null) {
			log.info("Not sending mail to " + toAddr + " with subject '" + subject + "' - no SMTP server configured");
			return;
		}
		try {
			Template htmlTemplate = freemarkerCfg.getTemplate(templateBase + "-html.ftl");
			StringWriter htmlWri = new StringWriter();
			htmlTemplate.process(model, htmlWri);
			String htmlMsg = htmlWri.toString();
			Template textTemplate = freemarkerCfg.getTemplate(templateBase + "-text.ftl");
			StringWriter textWri = new StringWriter();
			textTemplate.process(model, textWri);
			String textMsg = textWri.toString();
			executor.execute(new MailRunner(replyToAddr, replyToName, toAddr, toName, subject, htmlMsg, textMsg));
		} catch (TemplateException e) {
			throw new IOException(e);
		}
	}

	public static void main(String[] args) throws Exception {
		Random rand = new Random();
		boolean havePlaylistsOrLoves = false;
		List<Map<String, Object>> userList = new ArrayList<Map<String, Object>>();
		for (String userName : Arrays.asList("Foo", "Bar")) {
			Map<String, Object> userMap = new HashMap<String, Object>();
			userMap.put("friendlyName", userName);
			userMap.put("email", userName.toLowerCase() + "@robonobo.com");
			// userMap.put("numLibTrax", 0);
			userMap.put("numLibTrax", (1 + rand.nextInt(10)));
			List<Map<String, String>> pl = new ArrayList<Map<String, String>>();
			for (String pTitle : Arrays.asList("Testy1", "Testy2", "Testy3")) {
				havePlaylistsOrLoves = true;
				Map<String, String> pm = new HashMap<String, String>();
				pm.put("title", pTitle);
				pm.put("url", "http://rbnb.co/foo/" + pTitle);
				pl.add(pm);
			}
			userMap.put("playlists", pl);
			List<String> ll = Arrays.asList("Thingie", "Thang", "Thong", "Flarble");
			// List<String> ll = Arrays.asList();
			userMap.put("loves", ll);
			if (ll.size() > 0) {
				havePlaylistsOrLoves = true;
				userMap.put("lovesUrl", "http://rbnb.co/some/loves");
			}
			userList.add(userMap);
		}
		Map model = new HashMap();
		model.put("toName", "Notify User");
		model.put("toEmail", "heydude@robonobo.com");
		model.put("rbnbUrl", "http://robonobo.com/");
		model.put("users", userList);
		model.put("havePlaylistsOrLoves", havePlaylistsOrLoves);
		testTemplate(model, "combined-notif-text.ftl");
	}

	private static void testTemplate(Map model, String templateName) throws TemplateException, IOException {
		Configuration cfg = new Configuration();
		cfg.setTemplateLoader(new FileTemplateLoader(new File("/Users/macavity/src/git/robonobo/midas-webapp/WebContent/WEB-INF/freemarker")));
		cfg.setObjectWrapper(new DefaultObjectWrapper());
		Template t = cfg.getTemplate(templateName);
		PrintWriter wri = new PrintWriter(System.out);
		t.process(model, wri);
		wri.close();
	}

	class MailRunner implements Runnable {
		String replyToAddr;
		String replyToName;
		String toAddr;
		String toName;
		String subject;
		String htmlMsg;
		String textMsg;

		public MailRunner(String replyToAddr, String replyToName, String toAddr, String toName, String subject, String htmlMsg, String textMsg) {
			this.replyToAddr = replyToAddr;
			this.replyToName = replyToName;
			this.toAddr = toAddr;
			this.toName = toName;
			this.subject = subject;
			this.htmlMsg = htmlMsg;
			this.textMsg = textMsg;
		}

		public void run() {
			try {
				HtmlEmail mail = new HtmlEmail();
				mail.setHostName(smtpHost);
				mail.setSmtpPort(smtpPort);
				mail.setTLS(useTls);
				mail.setAuthentication(smtpUser, smtpPwd);
				mail.setFrom(appConfig.getInitParam("fromEmail"), appConfig.getInitParam("fromName"));
				if (replyToAddr != null)
					mail.addReplyTo(replyToAddr, replyToName);
				mail.setSubject(subject);
				if (toName == null)
					mail.addTo(toAddr);
				else
					mail.addTo(toAddr, toName);
				mail.setHtmlMsg(htmlMsg);
				mail.setTextMsg(textMsg);
				mail.send();
				log.info("Successfully sent mail to " + toAddr + " with subject '" + subject + "'");
			} catch (Exception e) {
				log.error("Caught exception sending mail to " + toAddr + " with subject '" + subject + "'", e);
			}
		}
	}
}
