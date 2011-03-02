package com.robonobo.core.service;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.robonobo.core.RobonoboInstance;
import com.robonobo.spi.RuntimeService;

/**
 * The robonobo service framework allows arbitary addition of services which
 * have dependencies and provide a named service.
 * 
 * Instances of RuntimeServiceProvider provide methods for instantiating and
 * destructing a given service as well as denoting its dependencies and also
 * what named service it provides
 * 
 * The ServiceManager ties this together and starts services in an order that
 * satisfies the dependencies.
 * 
 * Each service is related to an arbitary "provides" keyword. Logically, this
 * may implement an interface and/or replace behvaiour on the RobonoboCore.
 * Services that "depend" on a provides keyword will only be loaded once that
 * service has been loaded. This means that services that need, for example, a
 * UI, are able to wait for another service to initialize one, whatever it may
 * be, before it starts using it.
 * 
 * Plugins may inject services into the service manager to resolve complex
 * dependencies, eg ones which may require other plugins' services.
 * 
 * @author Ray, macavity
 * 
 */
public class ServiceManager {
	Log log = LogFactory.getLog(getClass());
	Map<String, RuntimeService> services = new HashMap<String, RuntimeService>();
	boolean running = false;
	List<RuntimeService> locks = new ArrayList<RuntimeService>();
	RobonoboInstance robonobo;

	public ServiceManager(RobonoboInstance robonobo) {
		super();
		this.robonobo = robonobo;
	}

	public RobonoboInstance getRobonobo() {
		return robonobo;
	}

	public RuntimeService registerService(RuntimeService service) {
		services.put(service.getProvides(), service);
		return service;
	}

	public void unregisterService(String provides) throws ServiceException {
		if (services.containsKey(provides)) {
			unregisterService(services.get(provides));
		}
	}

	public void unregisterService(RuntimeService service) throws ServiceException {
		if (service.isRunning())
			stopService(service);
		services.remove(service);
	}

	public RuntimeService getService(String provides) {
		return services.get(provides);
	}

	protected void startServiceAndDependencies(RuntimeService service) throws ServiceException {
		log.debug("Inspecting service '" + service.getName() + "' (" + service.getProvides() + ") for startup");
		for (String provides : service.getDependencies().keySet()) {
			RuntimeService s = getService(provides);
			boolean required = service.getDependencies().get(provides);
			// if there is no such service
			if (s == null) {
				if (required)
					throw new ServiceException("Could not start " + service.getName() + " (" + service.getProvides()
							+ ") since no service provides '" + provides + "'");
			} else {
				if (locks.contains(s))
					throw new ServiceException("Cyclical dependency! '" + provides + "'");
				else
					locks.add(s);
				if (!s.isRunning()) {
					if (required)
						log.debug(service.getName() + " REQUIRES '" + provides + "', attempting to start");
					else
						log.debug(service.getName() + " USES '" + provides + "', attempting to start");
					startServiceAndDependencies(s);
				}
				if (!s.isRunning()) {
					if (required) {
						throw new ServiceException("Could not start " + service.getName() + " ("
								+ service.getProvides() + ") since " + s.getName() + " (" + s.getProvides()
								+ ") is disabled");
					}
				}
				locks.remove(s);
			}
		}
		log.info("Starting service '" + service.getName() + "' (" + service.getProvides() + ")");
		startService(service);
		log.info("Started service '" + service.getName() + "' (" + service.getProvides() + ")");
	}

	protected void stopServiceAndDependencies(RuntimeService service) {
		log.debug("Inspecting service '" + service.getName() + "' (" + service.getProvides() + ") for shutdown");
		for (RuntimeService s : services.values()) {
			if (s.isRunning() && s.getDependencies().keySet().contains(service.getProvides())) {
				log.debug("'" + s.getName() + "' (" + s.getProvides() + ") depends on service '" + service.getName()
						+ "' (" + service.getProvides() + "), shutting that down");
				stopServiceAndDependencies(s);
			}
		}
		try {
			log.info("Stopping service '" + service.getName() + "' (" + service.getProvides() + ")");
			stopService(service);
			log.info("Stopped service '" + service.getName() + "' (" + service.getProvides() + ")");
		} catch (ServiceException e) {
			log.error("Failed to shutdown '" + service.getName() + "' (" + service.getProvides() + ")", e);
			// dont try and shut it down again
			service.setRunning(false);
		}
	}

	public void startup() throws ServiceException {
		log.info("Service Manager, spinning up...");
		while (true) {
			try {
				for (RuntimeService service : services.values()) {
					if (!service.isRunning()) {
						try {
							startServiceAndDependencies(service);
						} catch (ServiceException e) {
							log.fatal("Caught ServiceException when starting "+service.getProvides()+" ("+service.getName()+"): "+e.getMessage());
							shutdown();
							throw new ServiceException("A service threw an exception when attempting to start, services have been shutdown", e);
						}
					}
				}
				running = true;
				return;
			} catch (ConcurrentModificationException e) {
				// something modified this while we were enumerating,
				// restart, but it will skip services that are already
				// started
				log.info("services modfied, reperforming service startup");
			}
		}
	}

	public void shutdown() {
		log.info("Service Manager, spinning down...");
		for (RuntimeService service : services.values()) {
			if (service.isRunning())
				stopServiceAndDependencies(service);
		}
		log.fatal("Goodbye.");
		running = false;
	}

	public boolean isRunning() {
		return running;
	}

	protected void startService(RuntimeService service) throws ServiceException {
		try {
			service.setRobonobo(getRobonobo());
			service.startup();
			service.setRunning(true);
		} catch (Exception e) {
			log.error("Service " + service.getName() + " (" + service.getProvides() + ") failed to start", e);
			throw new ServiceException("Service " + service.getName() + " (" + service.getProvides()
					+ ") failed to start", e);
		}
	}

	protected void stopService(final RuntimeService service) throws ServiceException {
		try {
			service.shutdown();
			service.setRunning(false);
		} catch (Exception e) {
			throw new ServiceException("Caugh exception while stopping service: " + service.getName(), e);
		}
	}
}
