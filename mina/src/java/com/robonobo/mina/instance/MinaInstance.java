package com.robonobo.mina.instance;

import java.util.*;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.doomdark.uuid.UUID;
import org.doomdark.uuid.UUIDGenerator;

import com.robonobo.common.concurrent.SafetyNet;
import com.robonobo.common.exceptions.SeekInnerCalmException;
import com.robonobo.common.util.ExceptionEvent;
import com.robonobo.common.util.ExceptionListener;
import com.robonobo.core.api.*;
import com.robonobo.core.api.proto.CoreApi.EndPoint;
import com.robonobo.core.api.proto.CoreApi.Node;
import com.robonobo.mina.agoric.BuyMgr;
import com.robonobo.mina.agoric.SellMgr;
import com.robonobo.mina.escrow.EscrowMgr;
import com.robonobo.mina.escrow.EscrowProvider;
import com.robonobo.mina.external.*;
import com.robonobo.mina.external.buffer.PageBuffer;
import com.robonobo.mina.message.proto.MinaProtocol.Agorics;
import com.robonobo.mina.network.*;
import com.robonobo.mina.stream.StreamMgr;
import com.robonobo.mina.util.BadNodeList;
import com.robonobo.mina.util.Tagline;

public class MinaInstance implements MinaControl {
	public static final int MINA_PROTOCOL_VERSION = 1;

	private String myNodeId;
	private final Log log;
	private final MinaConfig config;
	private final CCMgr ccm;
	private final MessageMgr messageMgr;
	private final NetworkMgr netMgr;
	private final SMRegister smRegister;
	private SupernodeMgr supernodeMgr;
	private SellMgr sellMgr;
	private BuyMgr buyMgr;
	private SourceMgr sourceMgr;
	private EscrowMgr escrowMgr;
	private EscrowProvider escrowProvider;
	private ScheduledThreadPoolExecutor executor;
	private final BadNodeList badNodes;
	private final EventMgr eventMgr;
	private StreamAdvertiser streamAdvertiser;
	private final Application implementingApplication;
	private boolean started = false;
	private Agorics myAgorics;
	private CurrencyClient curClient;

	public MinaInstance(MinaConfig config, Application application, ScheduledThreadPoolExecutor executor) {
		// Make sure we know about exceptions
		SafetyNet.addListener(new MinaExceptionListener());
		log = getLogger(getClass());
		this.config = config;
		this.executor = executor;
		if (config.getNodeId() != null)
			myNodeId = config.getNodeId();
		else
			myNodeId = generateNodeId();
		log.fatal("Mina running on udp port " + config.getListenUdpPort() + ", node id: " + myNodeId);
		messageMgr = new MessageMgr(this);
		ccm = new CCMgr(this);
		netMgr = new NetworkMgr(this);
		smRegister = new SMRegister(this);
		if (config.isSupernode())
			supernodeMgr = new SupernodeMgr(this);
		if (config.isAgoric()) {
			sellMgr = new SellMgr(this);
			buyMgr = new BuyMgr(this);
			escrowMgr = new EscrowMgr(this);
		}
		if (config.getRunEscrowProvider()) {
			escrowProvider = new EscrowProvider(this);
		}
		badNodes = new BadNodeList(this);
		eventMgr = new EventMgr(this);
		sourceMgr = new SourceMgr(this);
		streamAdvertiser = new StreamAdvertiser(this);
		implementingApplication = application;
	}

	private String generateNodeId() {
		UUID uuid = UUIDGenerator.getInstance().generateRandomBasedUUID();
		return uuid.toString().replace("-", "");
	}

	/**
	 * @syncpriority 90
	 */
	public void abort() {
		log.fatal("Mina instance ABORTING!");
		ccm.prepareForShutdown();
		StreamMgr[] sms = smRegister.getAllSMs();
		for (int i = 0; i < sms.length; i++) {
			sms[i].abort();
		}
		ccm.abort();
		netMgr.stop();
		started = false;
		log.fatal("Mina instance stopped");
	}

	public void addMinaListener(MinaListener listener) {
		eventMgr.addMinaListener(listener);
	}

	public void addNodeLocator(NodeLocator locator) {
		getNetMgr().addNodeLocator(locator);
	}

	public void startBroadcast(String streamId, PageBuffer pb) {
		StreamMgr sm = getSmRegister().getSM(streamId);
		if (sm == null) {
			sm = getSmRegister().getOrCreateSM(streamId, pb);
		} else {
			sm.setPageBuffer(pb);
		}
		sm.startBroadcast();
		getEventMgr().fireBroadcastStarted(streamId);
	}

	public void stopBroadcast(String streamId) {
		StreamMgr sm = getSmRegister().getSM(streamId);
		if (sm == null) {
			throw new SeekInnerCalmException();
		}
		sm.stopBroadcast();
		getEventMgr().fireBroadcastStopped(streamId);
	}

	public void startReception(String streamId, PageBuffer pb, StreamVelocity sv) {
		StreamMgr sm = getSmRegister().getSM(streamId);
		if (sm == null)
			sm = getSmRegister().getOrCreateSM(streamId, pb);
		else
			sm.setPageBuffer(pb);
		if (sv != null)
			sm.setStreamVelocity(sv);
		sm.startReception();
		getEventMgr().fireReceptionStarted(streamId);
	}

	public void stopReception(String streamId) {
		StreamMgr sm = getSmRegister().getSM(streamId);
		if (sm == null) {
			throw new SeekInnerCalmException();
		}
		sm.stopReception();
		getEventMgr().fireReceptionStopped(streamId);
	}

	public BadNodeList getBadNodeList() {
		return badNodes;
	}

	public CCMgr getCCM() {
		return ccm;
	}

	public MinaConfig getConfig() {
		return config;
	}

	public List<ConnectedNode> getConnectedNodes() {
		return ccm.getConnectedNodes();
	}

	public Set<String> getSources(String streamId) {
		StreamMgr sm = smRegister.getSM(streamId);
		if (sm == null)
			return new HashSet<String>();
		return sm.getSourceNodeIds();
	}

	public int numSources(String streamId) {
		StreamMgr sm = smRegister.getSM(streamId);
		if (sm == null)
			return 0;
		return sm.numSources();
	}

	public List<String> getConnectedSources(String streamId) {
		StreamMgr sm = smRegister.getSM(streamId);
		if (sm == null)
			return new ArrayList<String>();
		return sm.getConnectedSources();
	}

	public boolean isConnectedToSupernode() {
		return ccm.haveSupernode();
	}

	public List<String> getMyEndPointUrls() {
		List<String> result = new ArrayList<String>();
		for (EndPointMgr epMgr : getNetMgr().getEndPointMgrs()) {
			EndPoint localEp = epMgr.getLocalEndPoint();
			if (localEp != null)
				result.add(localEp.getUrl());
			EndPoint publicEp = epMgr.getPublicEndPoint();
			if (publicEp != null && ((localEp == null) || !publicEp.getUrl().equals(localEp.getUrl())))
				result.add(publicEp.getUrl());
		}
		return result;
	}

	public MessageMgr getMessageMgr() {
		return messageMgr;
	}

	public EventMgr getEventMgr() {
		return eventMgr;
	}

	public ScheduledThreadPoolExecutor getExecutor() {
		return executor;
	}

	public Application getImplementingApplication() {
		return implementingApplication;
	}

	public Log getLogger(Class callerClass) {
		return LogFactory.getLog(callerClass.getName() + ".INSTANCE." + myNodeId);
	}

	public String getMyNodeId() {
		return myNodeId;
	}

	public NetworkMgr getNetMgr() {
		return netMgr;
	}

	public SMRegister getSmRegister() {
		return smRegister;
	}

	public SupernodeMgr getSupernodeMgr() {
		return supernodeMgr;
	}

	public boolean isStarted() {
		return started;
	}

	public PageBuffer getPageBuffer(String streamId) {
		StreamMgr sm = smRegister.getSM(streamId);
		if (sm == null) {
			return null;
		}
		return sm.getPageBuffer();
	}

	public void removeNodeLocator(NodeLocator locator) {
		getNetMgr().removeNodeLocator(locator);
	}

	public void start() throws MinaException {
		try {
			log.info(Tagline.getTagLine());
			netMgr.start();
			started = true;
			eventMgr.fireMinaStarted();
		} catch (MinaException e) {
			SafetyNet.notifyException(e, this);
			log.fatal("MinaException caught on startup, stopping Mina");
			stop();
			throw e;
		}
	}

	/**
	 * @syncpriority 200
	 */
	public void stop() throws MinaException {
		log.fatal("Mina instance stopping");
		ccm.prepareForShutdown();
		StreamMgr[] sms = smRegister.getAllSMs();
		for (int i = 0; i < sms.length; i++) {
			sms[i].stop();
		}
		sourceMgr.stop();
		streamAdvertiser.cancel();
		ccm.stop();
		netMgr.stop();
		badNodes.clear();
		started = false;
		log.fatal("Mina instance stopped");
		eventMgr.fireMinaStopped();
	}

	@Override
	public String toString() {
		return "[MinaInstance,id=" + myNodeId + "]";
	}

	public void addFoundSourceListener(String streamId, FoundSourceListener listener) {
		StreamMgr sm = smRegister.getOrCreateSM(streamId, null);
		sm.addFoundSourceListener(listener);
	}

	public void removeFoundSourceListener(String streamId, FoundSourceListener listener) {
		StreamMgr sm = smRegister.getSM(streamId);
		if (sm != null) {
			sm.removeFoundSourceListener(listener);
		}
	}

	public Set<Node> getKnownSources(String streamId) {
		return smRegister.getSM(streamId).getKnownSources();
	}

	private class MinaExceptionListener implements ExceptionListener {
		public void onException(ExceptionEvent e) {
			log.fatal(e.getSource().getClass().getName() + " caught Exception: ", e.getException());
		}
	}

	public Map<String, TransferSpeed> getTransferSpeeds() {
		Map<String, TransferSpeed> result = new HashMap<String, TransferSpeed>();
		for (StreamMgr sm : smRegister.getLiveSMs()) {
			int upload = sm.getBroadcastingFlowRate();
			int download = sm.getListeningFlowRate();
			if (upload > 0 || download > 0)
				result.put(sm.getStreamId(), new TransferSpeed(sm.getStreamId(), download, upload));
		}
		return result;
	}

	public StreamAdvertiser getStreamAdvertiser() {
		return streamAdvertiser;
	}

	public void clearStreamPriorities() {
		for (StreamMgr sm : smRegister.getLiveSMs()) {
			sm.setPriority(0);
		}
	}

	/**
	 * The stream priority dictates the importance of streams relative to each other (higher is more important)
	 */
	public void setStreamPriority(String streamId, int priority) {
		StreamMgr sm = smRegister.getSM(streamId);
		if (sm != null)
			sm.setPriority(priority);
	}

	/**
	 * The streamvelocity dictates how fast we want this stream (slower = cheaper)
	 */
	public void setStreamVelocity(String streamId, StreamVelocity sv) {
		StreamMgr sm = smRegister.getSM(streamId);
		if (sm != null) {
			sm.setStreamVelocity(sv);
			for (LCPair lcp : sm.getStreamConns().getAllListenConns()) {
				buyMgr.possiblyRebid(lcp.getCC().getNodeId());
			}
		}
	}

	public void setAllStreamVelocitiesExcept(String streamId, StreamVelocity sv) {
		StreamMgr[] sms = smRegister.getLiveSMs();
		Set<String> rebidNodeIds = new HashSet<String>();
		for (StreamMgr sm : sms) {
			if (sm.isReceiving() && !sm.getStreamId().equals(streamId)) {
				sm.setStreamVelocity(sv);
				for (LCPair lcp : sm.getStreamConns().getAllListenConns()) {
					rebidNodeIds.add(lcp.getCC().getNodeId());
				}
			}
		}
		for (String nodeId : rebidNodeIds) {
			buyMgr.possiblyRebid(nodeId);
		}
	}

	@Override
	public void addNodeFilter(NodeFilter nf) {
		netMgr.addNodeFilter(nf);
	}

	@Override
	public void removeNodeFilter(NodeFilter nf) {
		netMgr.removeNodeFilter(nf);
	}

	public void setCurrencyClient(CurrencyClient client) {
		curClient = client;
		Agorics.Builder ab = Agorics.newBuilder();
		ab.setCurrencyUrl(client.currencyUrl());
		ab.setAcceptPaymentMethods(client.getAcceptPaymentMethods());
		ab.setMinBid(client.getMinBid());
		ab.setIncrement(client.getBidIncrement());
		ab.setMinTopRate(client.getMinTopRate());
		myAgorics = ab.build();
		if (escrowProvider != null)
			escrowProvider.setCurrencyClient(client);
	}

	public void configUpdated() {
		netMgr.configUpdated();
	}

	@Override
	public void setHandoverHandler(HandoverHandler handler) {
		netMgr.setHandoverHandler(handler);
	}

	public CurrencyClient getCurrencyClient() {
		return curClient;
	}

	public SellMgr getSellMgr() {
		return sellMgr;
	}

	public BuyMgr getBuyMgr() {
		return buyMgr;
	}

	public SourceMgr getSourceMgr() {
		return sourceMgr;
	}

	public Agorics getMyAgorics() {
		return myAgorics;
	}

	public EscrowMgr getEscrowMgr() {
		return escrowMgr;
	}

	public EscrowProvider getEscrowProvider() {
		return escrowProvider;
	}
}
