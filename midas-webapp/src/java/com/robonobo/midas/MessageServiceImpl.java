package com.robonobo.midas;

import java.io.IOException;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
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

import com.robonobo.common.exceptions.Errot;
import com.robonobo.core.api.model.Playlist;
import com.robonobo.midas.model.*;
import com.robonobo.remote.service.MidasService;

import freemarker.template.*;

@Service("message")
@SuppressWarnings({ "rawtypes", "unchecked" })
public class MessageServiceImpl implements MessageService, InitializingBean, DisposableBean, ServletContextAware {
	private static final String FROM_ADDR = "mailmonkey@robonobo.com";
	private String smtpHost;
	private int smtpPort;
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
		sendMail(null, null, newUser.getEmail(), newUser.getFriendlyName(), newUser.getFriendlyName() + ", welcome to robonobo", "welcome", model);
	}

	@Override
	public void sendFriendRequest(MidasUser requestor, MidasUser requestee, MidasPlaylist p) throws IOException {
		MidasFriendRequest friendReq = midas.createOrUpdateFriendRequest(requestor, requestee, p);
		Map model = newModel(requestee.getFriendlyName(), requestee.getEmail());
		model.put("fromUser", requestor);
		model.put("acceptUrl", appConfig.getInitParam("friendReqUrlBase") + friendReq.getRequestCode());
		model.put("playlist", p);
		sendMail(requestor.getEmail(), requestor.getFriendlyName(), requestee.getEmail(), requestee.getFriendlyName(), requestor.getFriendlyName()
				+ " would like to be your friend on robonobo", "friendrequest", model);
	}

	@Override
	public void sendInvite(MidasUser fromUser, String toEmail, MidasPlaylist p) throws IOException {
		MidasInvite invite = midas.createOrUpdateInvite(toEmail, fromUser, p);
		Map model = newModel(null, toEmail);
		model.put("fromUser", fromUser);
		model.put("inviteUrl", appConfig.getInitParam("inviteUrlBase") + invite.getInviteCode());
		model.put("playlist", p);
		sendMail(fromUser.getEmail(), fromUser.getFriendlyName(), toEmail, null, fromUser.getFriendlyName() + " has invited you to robonobo", "invite", model);
	}

	@Override
	public void sendPlaylistShare(MidasUser fromUser, MidasUser toUser, Playlist p) throws IOException {
		Map model = newModel(toUser.getFriendlyName(), toUser.getEmail());
		model.put("fromUser", fromUser);
		model.put("playlist", p);
		model.put("playlistUrl", appConfig.getInitParam("playlistShortUrlBase") + Long.toHexString(p.getPlaylistId()));
		sendMail(fromUser.getEmail(),
				fromUser.getFriendlyName(),
				toUser.getEmail(),
				toUser.getFriendlyName(),
				fromUser.getFriendlyName() + " has shared a playlist with you",
				"shareplaylist",
				model);
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
				mail.setAuthentication(smtpUser, smtpPwd);
				mail.setFrom(appConfig.getInitParam("from-email"), appConfig.getInitParam("from-name"));
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
