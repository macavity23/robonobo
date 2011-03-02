package com.robonobo.common.servlets.util;

import javax.servlet.http.HttpServletRequest;

public class RequestUtil {

	/**
	 * returns the base uri for this instance of midas.
	 * @return
	 */
	public static String getBaseUri(HttpServletRequest req) 
	{
		return req.getScheme()
		+ "://"
		+ req.getServerName()
		+ (req.getServerPort() == 80 ? "" : ":"
			+ req.getServerPort())
		+ req.getContextPath();
	}
	
	public static String getAppHost(HttpServletRequest request) {
		return request.getLocalName() + ":" + request.getLocalPort() + " (" + request.getSession().getServletContext().getServerInfo() + ")";
	}
	
}
