package com.robonobo.remote.service;

import java.io.IOException;

public interface MailService {
	public void sendMail(String fromName, String fromEmail, String toName, String toEmail, String subject, String body)
			throws IOException;

	public void sendMail(
			String fromName, String fromEmail, String toName, String toEmail, String replyToName, String replyToEmail, String subject,
			String body) throws IOException;
}
