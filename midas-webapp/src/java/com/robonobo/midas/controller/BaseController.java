package com.robonobo.midas.controller;

import static com.robonobo.common.util.TimeUtil.*;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.ModelAndView;

import com.google.protobuf.AbstractMessage;
import com.google.protobuf.GeneratedMessage;
import com.robonobo.midas.AppConfig;
import com.robonobo.midas.model.MidasUser;
import com.robonobo.remote.service.MidasService;

public abstract class BaseController {
	@Autowired
	protected AppConfig appConfig;
	@Autowired
	protected MidasService midas;
	protected Log log = LogFactory.getLog(getClass());

	protected MidasUser getAuthUser(HttpServletRequest req) {
		if (req.getHeader("Authorization") != null) {
			String authString = new String(Base64.decodeBase64(req.getHeader("Authorization").replaceAll("Basic ", "").getBytes()));
			String[] pair = authString.split(":", 2);
			MidasUser user = midas.getUserByEmail(pair[0]);
			if (user != null && user.getPassword().equals(pair[1]))
				return user;
		}
		return null;
	}

	protected String getAuthUserEmail(HttpServletRequest req) {
		if (req.getHeader("Authorization") != null) {
			String authString = new String(Base64.decodeBase64(req.getHeader("Authorization").replaceAll("Basic ", "").getBytes()));
			String[] pair = authString.split(":", 2);
			return pair[0];
		}
		return null;
	}

	protected void send404(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		resp.setContentType("text/html");
		resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
		PrintWriter writer = resp.getWriter();
		writer.write("<html><head><title>404 Not Found</title></head><body><h1>404 Not Found</h1><p>The resource represented by URL ");
		writer.write(req.getRequestURL().toString());
		writer.write(" was not found.</p></body></html>");
		writer.flush();
	}

	protected void send401(HttpServletRequest req, HttpServletResponse resp) throws IOException {
		log.error("Denying access by user '" + getAuthUserEmail(req) + "' to resource "
				+ req.getRequestURL().toString());
		resp.setContentType("text/html");
		resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		PrintWriter writer = resp.getWriter();
		writer.write("<html><head><title>401 Unauthorized</title></head><body><h1>401 Unauthorized</h1>"
				+ "<p>As your currently logged-in user, you are not allowed to access the resource represented by URL ");
		writer.write(req.getRequestURL().toString());
		writer.write("</p></body></html>");
		writer.flush();
	}

	protected String[] getPathElements(HttpServletRequest req) {
		return req.getRequestURI().split("/");
	}

	/**
	 * Returns min(now, lastUpdatedDate+1sec) We only keep 1-second granularity,
	 * so if something is updated > once within 1s, the updated date doesn't
	 * change, so things ignore the update. Since this is a very rare case (only
	 * when server is running locally), we fudge a bit and set the date 1s in
	 * the future
	 */
	protected Date getUpdatedDate(Date lastUpdatedDate) {
		if(lastUpdatedDate == null)
			return now();
		long t = now().getTime();
		long mint = lastUpdatedDate.getTime() + 1000;
		if (t < mint)
			t = mint;
		return new Date(t);
	}
	
	@SuppressWarnings("unchecked")
	protected void readFromInput(AbstractMessage.Builder bldr, HttpServletRequest req) throws IOException {
		bldr.mergeFrom(req.getInputStream());
	}

	protected void writeToOutput(GeneratedMessage msg, HttpServletResponse resp) throws IOException {
		resp.setContentType("application/data");
		msg.writeTo(resp.getOutputStream());
	}

	@ExceptionHandler(Exception.class)
	public void handleException(Exception e, HttpServletRequest req, HttpServletResponse resp) {
		log.error("Uncaught exception handling " + req.getRequestURI(), e);
		resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
	}

}
