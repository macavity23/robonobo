package com.robonobo.common.servlets.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import javax.servlet.ServletException;

public class ContextUtil {
	static Properties webappProperties;
	
	public static String getAppDescription() throws ServletException{
		return getWebAppProperties().getProperty("app.description", "");
	}
	
	public static String getAppName() throws ServletException{
		return getWebAppProperties().getProperty("app.name", "Unknown");
	}
	
	public static String getAppVersion() throws ServletException{
		return getWebAppProperties().getProperty("app.version", "Unknown");
	}
	
	public static String getAppCopyright() throws ServletException{
		return getWebAppProperties().getProperty("app.copyright", "Unknown");
	}
	
	public static Properties getWebAppProperties() throws ServletException {
		if(webappProperties == null) {
			webappProperties = new Properties();
			try {
				InputStream in = ContextUtil.class.getClassLoader().getResourceAsStream("webapp.properties");
				if(in != null)
					webappProperties.load(in);
			}
			catch(IOException e) {
				throw new ServletException("Unable to find webapp.properties", e);
			}
		}
			
		return webappProperties;
	}
}
