package com.robonobo.core.service;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.robonobo.core.RobonoboInstance;
import com.robonobo.core.api.Robonobo;
import com.robonobo.spi.RuntimeService;


abstract public class AbstractService implements RuntimeService {
	protected boolean running = false;
	protected Map<String, Boolean> deps = new HashMap<String, Boolean>();
	protected RobonoboInstance rbnb;
	protected Log log = LogFactory.getLog(getClass());

	public AbstractService() {
		super();
	}

	protected RobonoboInstance getRobonobo() {
		return rbnb;
	}

	public void setRobonobo(Robonobo robonobo) {
		this.rbnb = (RobonoboInstance) robonobo;
	}

	protected void addHardDependency(String need) {
		addDependency(need, true);
	}

	protected void addSoftDependency(String need) {
		addDependency(need, false);
	}

	protected void addDependency(String need, boolean required) {
		deps.put(need, new Boolean(required));
	}

	protected void removeDependency(String need) {
		deps.remove(need);
	}

	public Map<String, Boolean> getDependencies() {
		return deps;
	}

	abstract public void startup() throws Exception;

	abstract public void shutdown() throws Exception;

	public boolean isRunning() {
		return running;
	}

	public void setRunning(boolean running) {
		this.running = running;
	}
}
