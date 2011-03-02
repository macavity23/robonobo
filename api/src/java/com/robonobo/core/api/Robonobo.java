package com.robonobo.core.api;

import com.robonobo.core.api.config.RobonoboConfig;
import com.robonobo.mina.external.Application;
import com.robonobo.mina.external.MinaControl;





/**
 * This is the base interface for Robonobo
 * 
 * @author Ray
 *
 */
public interface Robonobo {
	public RobonoboConfig getConfig();
	
	public Application getApplication();
			
	public void start() throws RobonoboException;

	public void shutdown();

	public MinaControl getMina();
	
}