package com.robonobo.wang.server.controller;

import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ExceptionHandler;

import com.google.protobuf.AbstractMessage;
import com.google.protobuf.GeneratedMessage;
import com.robonobo.wang.server.UserAccount;
import com.robonobo.wang.server.dao.*;

public abstract class BaseController {
	public static final char WANG_CHAR = 0x65fa;
	protected Log log = LogFactory.getLog(getClass());
	@Autowired
	protected UserAccountDao uaDao;
	
	protected UserAccount getAuthUser(HttpServletRequest req, HttpServletResponse resp) {
		if(req.getHeader("Authorization") != null) {
			String authString = new String(Base64.decodeBase64(req.getHeader("Authorization").replaceAll("Basic ", "").getBytes()));
			String[] pair = authString.split(":", 2);
			String email = pair[0];
			String pwd = pair[1];
			UserAccount ua = null;
			try {
				ua = uaDao.getUserAccount(email);
			} catch (DAOException e) {
				log.error("Caught exception when getting user", e);
			}
			if(ua != null && ua.getPassword().equals(pwd))
				return ua;
		}
		return null;
	}

	@SuppressWarnings("unchecked")
	protected void readFromInput(AbstractMessage.Builder bldr, HttpServletRequest req) throws IOException {
		bldr.mergeFrom(req.getInputStream());
	}

	protected void writeToOutput(GeneratedMessage msg, HttpServletResponse resp) throws IOException {
		msg.writeTo(resp.getOutputStream());
	}

	@ExceptionHandler(Exception.class)
	protected void catchException(Exception e, HttpServletResponse resp) {
		log.error("Uncaught exception in controller", e);
		resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	}
	
	protected void send401(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		resp.setContentType("text/html");
		resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		PrintWriter writer = resp.getWriter();
		writer.write("<html><head><title>401 Unauthorized</title></head><body><h1>401 Unauthorized</h1>"+
		"<p>As your currently logged-in user, you are not allowed to access the resource represented by URL ");
		writer.write(req.getRequestURL().toString());
		writer.write(" was not found.</p></body></html>");
		writer.flush();
	}
}
