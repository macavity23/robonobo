package com.robonobo.wang.server;

import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.ServletContextAware;

import com.robonobo.common.concurrent.SafetyNet;
import com.robonobo.common.util.ExceptionEvent;
import com.robonobo.common.util.ExceptionListener;

@Configuration
public class AppConfig implements ServletContextAware, InitializingBean {
	private ServletContext sc;
	Log log = LogFactory.getLog(getClass());
	
	@Bean
	public RemoteWangService getRemoteWang() throws Exception {
		// This will be null when we're running the setup script
		if(sc == null)
			return null;
		String listenUrl = sc.getInitParameter("remoteWangListenURL");
		String sekrit = sc.getInitParameter("remoteWangSecret");
		return new RemoteWangService(listenUrl, sekrit);
	}
	
	@Override
	public void setServletContext(ServletContext sc) {
		this.sc = sc;
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		SafetyNet.addListener(new ExceptionListener() {
			public void onException(ExceptionEvent e) {
				log.error("Uncaught exception", e.getException());
			}
		});
	}
}
