package com.robonobo.core.mina;

import java.util.*;
import java.util.concurrent.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.robonobo.common.concurrent.CatchingRunnable;
import com.robonobo.core.api.TransferSpeed;
import com.robonobo.core.service.AbstractService;
import com.robonobo.mina.Mina;
import com.robonobo.mina.external.*;

public class MinaService extends AbstractService {
	SonarNodeLocator locator;
	Log log = LogFactory.getLog(getClass());
	protected MinaControl mina;
	ScheduledFuture<?> transferSpeedsTask = null;

	public MinaService() {
		super();
		addHardDependency("core.gateway");
		addHardDependency("core.event");
		addHardDependency("core.wang");
	}

	public String getName() {
		return "Core Networking Service";
	}

	public String getProvides() {
		return "core.mina";
	}

	public MinaControl getMina() {
		return mina;
	}

	public SonarNodeLocator getSonarNodeLocator() {
		return locator;
	}

	public void shutdown() throws Exception {
		transferSpeedsTask.cancel(false);
		mina.stop();
		mina = null;
	}

	public void startup() throws Exception {
		MinaConfig minaCfg = (MinaConfig) getRobonobo().getConfig("mina");
		Application application = getRobonobo().getApplication();
		ScheduledThreadPoolExecutor executor = getRobonobo().getExecutor();
		mina = Mina.newInstance(minaCfg, application, executor);
		mina.addMinaListener(getRobonobo().getEventService());
		locator = new SonarNodeLocator();
		locator.addLocatorUri(getRobonobo().getConfig().getSonarServerUrl());
		mina.addNodeLocator(locator);

		if(getRobonobo().getConfig().isAgoric())
			mina.setCurrencyClient(getRobonobo().getWangService());
		mina.start();
		// Fire off our transfer speeds every second
		transferSpeedsTask = getRobonobo().getExecutor().scheduleAtFixedRate(new CatchingRunnable() {
			public void doRun() throws Exception {
				Map<String, TransferSpeed> speedsByStream = mina.getTransferSpeeds();
				List<ConnectedNode> nodeArr = mina.getConnectedNodes();
				Map<String, TransferSpeed> speedsByNode = new HashMap<String, TransferSpeed>();
				for (ConnectedNode node : nodeArr) {
					TransferSpeed ts = new TransferSpeed(node.nodeId, node.downloadRate, node.uploadRate);
					speedsByNode.put(node.nodeId, ts);
				}
				getRobonobo().getEventService().fireNewTransferSpeeds(speedsByStream, speedsByNode);
			}
		}, 1, 1, TimeUnit.SECONDS);
	}
}
