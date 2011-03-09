package com.robonobo.mina.network;

import java.util.*;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;

import com.robonobo.common.concurrent.Attempt;
import com.robonobo.common.concurrent.CatchingRunnable;
import com.robonobo.core.api.proto.CoreApi.EndPoint;
import com.robonobo.core.api.proto.CoreApi.Node;
import com.robonobo.mina.external.*;
import com.robonobo.mina.instance.MinaInstance;
import com.robonobo.mina.message.proto.MinaProtocol.MyDetailsChanged;

public class NetworkMgr {
	private final Log log;
	private final MinaInstance mina;
	private boolean listenerReady = false;
	private ScheduledFuture nodeLocatorTask;
	private List<NodeLocator> nodeLocators = new ArrayList<NodeLocator>();
	private List<EndPointMgr> endPointMgrs = new ArrayList<EndPointMgr>();
	private List<NodeFilter> nodeFilters = new ArrayList<NodeFilter>();
	private String myNodeId;
	private String myAppUri;
	private boolean iAmSuper;

	/** Node descriptor to be sent out publically */
	private Node publicNodeDesc;
	/** Node descriptor for local nodes */
	private Node localNodeDesc;
	private boolean gotPublicEps;

	public NetworkMgr(MinaInstance mina) {
		this.mina = mina;
		log = mina.getLogger(getClass());
		instantiateEndPointMgrs();
	}

	private void instantiateEndPointMgrs() {
		// Grab our list of epmgr classnames, and instantiate them
		String[] classNames = mina.getConfig().getEndPointMgrClasses().split(",");
		if (classNames.length == 0)
			throw new RuntimeException("No endpoint manager classes defined");
		for (String className : classNames) {
			try {
				Class clazz = Class.forName(className);
				EndPointMgr epMgr = (EndPointMgr) clazz.newInstance();
				epMgr.setMina(mina);
				endPointMgrs.add(epMgr);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
		}
	}

	public void addNodeLocator(NodeLocator locator) {
		nodeLocators.add(locator);
	}

	/**
	 * We've discovered we can do NAT traversal, and we should tell our supernodes and existing connections (other than locals, they don't need to know)
	 * @syncpriority 140
	 */
	public void readvertiseEndpoints() {
		buildMyNodeDescriptors();
		MyDetailsChanged mdc = MyDetailsChanged.newBuilder().setNode(publicNodeDesc).build();
		mina.getCCM().sendMessageToNonLocals("MyDetailsChanged", mdc);
	}

	private Node getNode(boolean isLocal, Collection<EndPoint> eps) {
		Node.Builder builder = Node.newBuilder();
		builder.setProtocolVersion(MinaInstance.MINA_PROTOCOL_VERSION);
		builder.setId(myNodeId);
		builder.setApplicationUri(myAppUri);
		if (iAmSuper)
			builder.setSupernode(true);
		if (isLocal)
			builder.setLocal(true);
		builder.addAllEndPoint(eps);
		return builder.build();
	}

	public Node getDescriptorForTalkingTo(Node otherNode, boolean isLocal) {
		List<EndPoint> eps = new ArrayList<EndPoint>();
		for (EndPointMgr epMgr : endPointMgrs) {
			EndPoint ep = epMgr.getEndPointForTalkingTo(otherNode);
			if (ep != null)
				eps.add(ep);
		}
		return getNode(isLocal, eps);
	}

	public void removeNodeLocator(NodeLocator locator) {
		nodeLocators.remove(locator);
	}

	public void locateMoreNodes() {
		mina.getExecutor().execute(new LocateNodesRunner());
	}

	public void start() throws MinaException {
		myNodeId = mina.getMyNodeId();
		iAmSuper = mina.getConfig().isSupernode();
		myAppUri = mina.getImplementingApplication().getHomeUri();
		if (mina.getConfig().isSupernode())
			log.info("I am a supernode.  Fear me.");
		else
			log.info("I am a leaf node.");

		for (EndPointMgr epMgr : endPointMgrs) {
			log.info("Starting " + epMgr.getClass().getName());
			try {
				epMgr.start();
			} catch (Exception e) {
				throw new MinaException(e);
			}
		}
		buildMyNodeDescriptors();
		nodeLocatorTask = mina.getExecutor().scheduleAtFixedRate(new LocateNodesRunner(), 0,
				mina.getConfig().getLocateNodesFreq(), TimeUnit.SECONDS);
	}

	private void buildMyNodeDescriptors() {
		gotPublicEps = false;
		List<EndPoint> eps = new ArrayList<EndPoint>();
		if (mina.getConfig().getSendPrivateAddrsToLocator()) {
			// Debug only! Send private addresses to node locator
			gotPublicEps = true;
			for (EndPointMgr epMgr : endPointMgrs) {
				eps.add(epMgr.getLocalEndPoint());
			}
		} else {
			// Only send public addresses
			for (EndPointMgr epMgr : endPointMgrs) {
				EndPoint ep = epMgr.getPublicEndPoint();
				if (ep != null) {
					gotPublicEps = true;
					eps.add(ep);
				}
			}
		}
		publicNodeDesc = getNode(false, eps);

		eps.clear();
		for (EndPointMgr epMgr : endPointMgrs) {
			eps.add(epMgr.getLocalEndPoint());
		}
		localNodeDesc = getNode(true, eps);
	}

	public void stop() {
		if (nodeLocatorTask != null)
			nodeLocatorTask.cancel(true);
		for (EndPointMgr epMgr : endPointMgrs) {
			log.info("Stopping " + epMgr.getClass().getName());
			epMgr.stop();
		}
	}

	public Node getPublicNodeDesc() {
		return publicNodeDesc;
	}

	public Node getLocalNodeDesc() {
		return localNodeDesc;
	}

	public boolean havePublicEndpoints() {
		return gotPublicEps;
	}

	public boolean canConnectTo(Node node) {
		if (node.getProtocolVersion() > MinaInstance.MINA_PROTOCOL_VERSION)
			return false;
		for (NodeFilter nf : nodeFilters) {
			if (!nf.acceptNode(node)) {
				log.debug("Node filter '" + nf.getFilterName() + "' rejected node " + node);
				return false;
			}
		}
		// TODO Support lower protocol versions (when we have more than one...)
		if (mina.getCCM().haveRunningOrPendingCCTo(node.getId()))
			return true;
		for (EndPointMgr epMgr : endPointMgrs) {
			if (epMgr.canConnectTo(node))
				return true;
		}
		return false;
	}

	public void addNodeFilter(NodeFilter nf) {
		nodeFilters.add(nf);
	}

	public void removeNodeFilter(NodeFilter nf) {
		nodeFilters.remove(nf);
	}

	public void configUpdated() {
		for (EndPointMgr epMgr : endPointMgrs) {
			epMgr.configUpdated();
		}
	}

	public void setHandoverHandler(HandoverHandler handler) {
		for (EndPointMgr epMgr : endPointMgrs) {
			epMgr.setHandoverHandler(handler);
		}
	}

	public List<EndPointMgr> getEndPointMgrs() {
		return endPointMgrs;
	}

	private void connectToSupernode(List<Node> supernodes) {
		if (supernodes.size() == 0)
			return;
		Node tryNode = supernodes.remove(0);
		ConnectToSupernodeAttempt a = new ConnectToSupernodeAttempt(supernodes);
		a.start();
		mina.getCCM().makeCCTo(tryNode, a);
	}

	private class LocateNodesRunner extends CatchingRunnable {
		public void doRun() {
			if (mina.getConfig().getLocateLocalNodes())
				locateLocalNodes();
			if (mina.getConfig().isSupernode())
				sendDetailsToLocator();
			else if (mina.getConfig().getLocateRemoteNodes() && !mina.getCCM().haveSupernode())
				locateSupernodes();
		}

		private void sendDetailsToLocator() {
			for (NodeLocator locator : nodeLocators) {
				locator.locateSuperNodes(publicNodeDesc);
			}
		}

		private void locateLocalNodes() {
			for (EndPointMgr epMgr : endPointMgrs) {
				epMgr.locateLocalNodes();
			}
		}

		private void locateSupernodes() {
			log.debug("Locating supernodes");
			for (NodeLocator nl : nodeLocators) {
				List<Node> nodeList = nl.locateSuperNodes(publicNodeDesc);
				if (nodeList != null)
					connectToSupernode(nodeList);
			}
		}
	}

	private class ConnectToSupernodeAttempt extends Attempt {
		private List<Node> remainingSupernodes;

		public ConnectToSupernodeAttempt(List<Node> remainingSupernodes) {
			super(mina.getExecutor(), mina.getConfig().getMessageTimeout() * 1000, "ConnectToSupernodeAttempt");
			this.remainingSupernodes = remainingSupernodes;
		}

		@Override
		protected void onFail() {
			if (remainingSupernodes.size() == 0)
				log.error("Failed to connect to any supernodes... :-(");
			else
				connectToSupernode(remainingSupernodes);
		}

		@Override
		protected void onTimeout() {
			onFail();
		}
	}
}
