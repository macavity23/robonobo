package com.robonobo.remote.service;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class DummyMailService implements MailService {
	Log log = LogFactory.getLog(getClass());

	public void sendMail(String fromName, String fromEmail, String toName, String toEmail, String subject, String body)
			throws IOException {
		sendMail(fromName, fromEmail, toName, toEmail, null, null, subject, body);
	}

	public void sendMail(
			String fromName, String fromEmail, String toName, String toEmail, String replyToName, String replyToEmail,
			String subject, String body) throws IOException {
		log.warn("DEBUG: Dummy mail service sending mail from " + fromName + "/" + fromEmail + " to " + toName + "/"
				+ toEmail + ", Subject: " + subject + ", Body: " + body);
	}
}
