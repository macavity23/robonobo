package com.robonobo.sonar;

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
	public CleanupBean getCleanupBean() {
		return new CleanupBean();
	}
	
	public long getMaxNodeAge() {
		return Long.parseLong(sc.getInitParameter("maxNodeAgeMs"));
	}
	
	@Override
	public void setServletContext(ServletContext sc) {
		this.sc = sc;
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		// Make sure we see all exceptions
		SafetyNet.addListener(new ExceptionListener() {
			public void onException(ExceptionEvent e) {
				log.error("Uncaught exception", e.getException());
			}
		});
	}
}
