package com.robonobo.spi;

import java.util.Map;

import com.robonobo.core.api.Robonobo;


/**
 * SPI interface for providing runtime services
 * 
 * @author ray
 *
 */
public interface RuntimeService {
	/**
	 * A user-friendly name
	 * @return
	 */
	public String getName();
	
	
	/**
	 * A unique string used to determine dependencies
	 * @return
	 */
	public String getProvides();
	
	/**
	 * return an associative array of service names and a boolean value denoting whether 
	 * the system should fail if the service is not available.
	 * 
	 * @return
	 */
	public Map<String, Boolean> getDependencies();
	
	public void startup() throws Exception;
	
	public void shutdown() throws Exception;
		
	public boolean isRunning();
	public void setRunning(boolean running);
	
	public void setRobonobo(Robonobo robonobo);
}
