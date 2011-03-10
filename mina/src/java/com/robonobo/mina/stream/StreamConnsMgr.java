package com.robonobo.mina.stream;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;

import com.google.protobuf.GeneratedMessage;
import com.robonobo.common.concurrent.Attempt;
import com.robonobo.common.concurrent.CatchingRunnable;
import com.robonobo.core.api.proto.CoreApi.EndPoint;
import com.robonobo.core.api.proto.CoreApi.Node;
import com.robonobo.mina.external.StreamingDetails;
import com.robonobo.mina.external.StreamingNode;
import com.robonobo.mina.instance.MinaInstance;
import com.robonobo.mina.message.proto.MinaProtocol.SourceStatus;
import com.robonobo.mina.message.proto.MinaProtocol.StreamStatus;
import com.robonobo.mina.network.BCPair;
import com.robonobo.mina.network.ConnectionPair;
import com.robonobo.mina.network.ControlConnection;
import com.robonobo.mina.network.LCPair;
import com.robonobo.mina.util.MinaConnectionException;

/**
 * @syncpriority 180
 */
public class StreamConnsMgr {
	private final Log log;
	private final MinaInstance mina;
	private final Map<String, Attempt> pendingCons;
	private final Map<String, LCPair> lcPairs;
	private final Map<String, BCPair> bcPairs;
	private final StreamMgr sm;

	StreamConnsMgr(StreamMgr cm) {
		sm = cm;
		mina = cm.getMinaInstance();
		log = mina.getLogger(getClass());
		pendingCons = new HashMap<String, Attempt>();
		lcPairs = new HashMap<String, LCPair>();
		bcPairs = new HashMap<String, BCPair>();
	}

	public synchronized void abort() {
		BCPair[] tmpBcPairs = new BCPair[bcPairs.size()];
		bcPairs.values().toArray(tmpBcPairs);
		for (int i = 0; i < tmpBcPairs.length; i++) {
			tmpBcPairs[i].abort();
		}
		LCPair[] tmpLcPairs = new LCPair[lcPairs.size()];
		lcPairs.values().toArray(tmpLcPairs);
		for (int i = 0; i < tmpLcPairs.length; i++) {
			tmpLcPairs[i].abort();
		}
		bcPairs.clear();
		lcPairs.clear();
		pendingCons.clear();
	}

	/**
	 * @syncpriority 180
	 */
	public synchronized void closeAll() {
		closeAllListenConns();
		closeAllBroadcastConns();
	}

	/**
	 * Close everyone we are broadcasting to
	 * 
	 * @syncpriority 180
	 */
	public synchronized void closeAllBroadcastConns() {
		BCPair[] tmpPairs = new BCPair[bcPairs.size()];
		bcPairs.values().toArray(tmpPairs);
		for (int i = 0; i < tmpPairs.length; i++) {
			tmpPairs[i].die();
		}
	}

	/**
	 * Close everyone we are listening to
	 * 
	 * @syncpriority 180
	 */
	public synchronized void closeAllListenConns() {
		ConnectionPair[] tmpPairs = new ConnectionPair[lcPairs.size()];
		lcPairs.values().toArray(tmpPairs);
		for (int i = 0; i < tmpPairs.length; i++) {
			tmpPairs[i].die();
		}
	}

	/**
	 * Copied out, so safe to iterate over
	 * 
	 * @syncpriority 180
	 */
	public synchronized LCPair[] getAllListenConns() {
		LCPair[] pairs = new LCPair[lcPairs.size()];
		lcPairs.values().toArray(pairs);
		return pairs;
	}

	/**
	 * @syncpriority 180
	 */
	public synchronized BCPair getBroadcastConn(String nodeId) {
		return bcPairs.get(nodeId);
	}

	/**
	 * Copied out, so safe to iterate over
	 * 
	 * @syncpriority 180
	 */
	public synchronized BCPair[] getBroadcastConns() {
		BCPair[] pairs = new BCPair[bcPairs.size()];
		bcPairs.values().toArray(pairs);
		return pairs;
	}

	/**
	 * @syncpriority 180
	 */
	public synchronized LCPair getListenConn(String nodeId) {
		return lcPairs.get(nodeId);
	}

	/**
	 * The number of people we are broadcasting to
	 * 
	 * @syncpriority 180
	 */
	public synchronized int getNumBroadcastConns() {
		return bcPairs.size();
	}

	/**
	 * The number of people we are listening to
	 * 
	 * @syncpriority 180
	 */
	public synchronized int getNumListenConns() {
		return lcPairs.size();
	}

	/**
	 * @syncpriority 180
	 */
	public synchronized int getNumLocalLCPairs() {
		int numPairs = 0;
		Iterator<LCPair> i = lcPairs.values().iterator();
		while (i.hasNext()) {
			ConnectionPair pair = i.next();
			if (pair.getCC().isLocal())
				numPairs++;
		}
		return numPairs;
	}

	/**
	 * @syncpriority 180
	 */
	public synchronized int getNumPendingCons() {
		return pendingCons.size();
	}

	public StreamingDetails getStreamingDetails() {
		StreamingNode[] bNodes = getBroadcastStreamingNodes();
		StreamingNode[] lNodes = getListenStreamingNodes();
		StreamingDetails sd = new StreamingDetails(sm.getStreamId());
		sd.setReceivingFromNodes(lNodes);
		sd.setSendingToNodes(bNodes);
		sd.setBytesDownloaded(sm.getPageBuffer().getBytesReceived());
		return sd;
	}

	/**
	 * @syncpriority 180
	 */
	public synchronized boolean haveBroadcastConnWithId(String thisNodeId) {
		return bcPairs.containsKey(thisNodeId);
	}

	/**
	 * @syncpriority 180
	 */
	public synchronized boolean haveListenConnWithId(String thisNodeId) {
		return lcPairs.containsKey(thisNodeId);
	}

	/**
	 * @param pages
	 * @syncpriority 180
	 */
	public synchronized void makeBroadcastConnectionTo(ControlConnection cc, EndPoint listenEp, List<Long> pages) {
		if (mina.getCCM().isShuttingDown()) {
			log.debug("Not making broadcast connection to " + cc.getNodeId() + ": shutting down");
			return;
		}
		if (bcPairs.containsKey(cc.getNodeId())) {
			log.error("Error: not creating broadcasting connection to already-receiving node " + cc.getNodeId());
			return;
		}
		bcPairs.put(cc.getNodeId(), new BCPair(mina, sm, cc, listenEp, pages));
		mina.getSmRegister().updateSmStatus(sm.getStreamId(), true);
	}

	/**
	 * @syncpriority 200
	 */
	public void makeListenConnectionTo(final SourceStatus ss) throws MinaConnectionException {
		Node node = ss.getFromNode();
		final String nodeId = node.getId();
		if (mina.getCCM().isShuttingDown()) {
			log.debug("Not making listen connection to " + nodeId + ": shutting down");
			return;
		}

		// If we have no currency client, wait til we do
		if (mina.getConfig().isAgoric() && !mina.getCurrencyClient().isReady()) {
			log.debug("Not making lcpair to " + nodeId + " as currency client is not ready - waiting 5s");
			mina.getExecutor().schedule(new CatchingRunnable() {
				public void doRun() throws Exception {
					makeListenConnectionTo(ss);
				}
			}, 5, TimeUnit.SECONDS);
			return;
		}

		ControlConnection cc = mina.getCCM().getCCWithId(nodeId);
		if (cc == null) {
			synchronized (this) {
				Attempt getCCAttempt = new GetCCAttempt(mina.getConfig().getMessageTimeout(), ss);
				pendingCons.put(nodeId, getCCAttempt);
				getCCAttempt.start();
				log.debug("Making connection to " + nodeId + " for listening to " + sm.getStreamId());
				mina.getCCM().makeCCTo(node, getCCAttempt);
			}
		} else
			startListeningTo(cc, ss);
	}

	/**
	 * @syncpriority 200
	 */
	private void startListeningTo(final ControlConnection cc, SourceStatus ss) {
		String nodeId = cc.getNodeId();
		synchronized (this) {
			if (lcPairs.containsKey(nodeId)) {
				log.error("Error: Asked to make ListenConnection to already-listening node " + nodeId);
				return;
			}
			log.info("Starting listening to " + nodeId + " for stream " + sm.getStreamId());
			try {
				lcPairs.put(nodeId, new LCPair(sm, cc, ss));
			} catch (MinaConnectionException e) {
				log.error("Error creating listen connection to " + nodeId + " for stream " + sm.getStreamId(), e);
				return;
			}
		}
		mina.getEventMgr().fireReceptionConnsChanged(sm.streamId);
	}

	/**
	 * @syncpriority 180
	 */
	public synchronized void removeConnectionPair(ConnectionPair pair) {
		if (pair instanceof LCPair) {
			pendingCons.remove(pair.getCC().getNodeId());
			lcPairs.remove(pair.getCC().getNodeId());
		} else if (pair instanceof BCPair) {
			bcPairs.remove(pair.getCC().getNodeId());
			if (!sm.isReceiving())
				mina.getSmRegister().updateSmStatus(sm.getStreamId(), bcPairs.size() > 0);
		}
	}

	/**
	 * @syncpriority 180
	 */
	public synchronized void sendToBroadcastConns(String msgName, GeneratedMessage msg, String except) {
		Iterator<BCPair> i = bcPairs.values().iterator();
		while (i.hasNext()) {
			BCPair bcPair = i.next();
			if (bcPair.getCC().getNodeId().equals(except))
				continue;
			bcPair.sendMessage(msgName, msg);
		}
	}

	/**
	 * @syncpriority 180
	 */
	public synchronized void sendToListenConns(String msgName, GeneratedMessage msg) {
		sendToListenConns(msgName, msg, null);
	}

	/**
	 * @syncpriority 180
	 */
	public synchronized void sendToListenConns(String msgName, GeneratedMessage msg, String except) {
		Iterator<LCPair> i = lcPairs.values().iterator();
		while (i.hasNext()) {
			ConnectionPair lcPair = i.next();
			if (lcPair.getCC().getNodeId().equals(except))
				continue;
			lcPair.sendMessage(msgName, msg);
		}
	}

	/**
	 * @syncpriority 180
	 */
	public synchronized void sendToNonLocalBroadcastConns(String msgName, GeneratedMessage msg, String except) {
		Iterator<BCPair> i = bcPairs.values().iterator();
		while (i.hasNext()) {
			BCPair bcPair = i.next();
			if (bcPair.getCC().getNodeId().equals(except))
				continue;
			if (bcPair.getCC().isLocal())
				continue;
			bcPair.sendMessage(msgName, msg);
		}
	}

	/**
	 * @syncpriority 180
	 */
	private synchronized StreamingNode[] getBroadcastStreamingNodes() {
		StreamingNode[] bNodes = new StreamingNode[bcPairs.size()];
		int i = 0;
		for (Iterator<BCPair> iter = bcPairs.values().iterator(); iter.hasNext();) {
			BCPair bcp = iter.next();
			bNodes[i++] = new StreamingNode(bcp.getCC().getNodeId().toString(), bcp.getFlowRate());
		}
		return bNodes;
	}

	/**
	 * @syncpriority 180
	 */
	private synchronized StreamingNode[] getListenStreamingNodes() {
		int i;
		StreamingNode[] lNodes = new StreamingNode[lcPairs.size()];
		i = 0;
		for (Iterator<LCPair> iter = lcPairs.values().iterator(); iter.hasNext();) {
			LCPair lcp = iter.next();
			StreamingNode node = new StreamingNode(lcp.getCC().getNodeId().toString(), lcp.getFlowRate());
			node.setConnected(true);
			node.setComplete(lcp.isComplete());
			lNodes[i++] = node;
		}
		return lNodes;
	}

	private class GetCCAttempt extends Attempt {
		private SourceStatus sourceStat;
		private String nodeId;

		public GetCCAttempt(int timeoutSecs, SourceStatus ss) {
			super(mina.getExecutor(), timeoutSecs * 1000, "GetCCAttempt");
			this.sourceStat = ss;
			this.nodeId = ss.getFromNode().getId();
		}

		/**
		 * @syncpriority 180
		 */
		protected void onFail() {
			log.info("Failed to connect to " + nodeId + " for stream '" + sm.getStreamId() + "'");
			synchronized (StreamConnsMgr.this) {
				pendingCons.remove(nodeId);
			}
			mina.getSourceMgr().cachePossiblyDeadSource(sourceStat.getFromNode(), sm.getStreamId());
			// Request more if we need them
			sm.requestCachedSources();
		}

		/**
		 * @syncpriority 200
		 */
		protected void onSuccess() {
			synchronized (StreamConnsMgr.this) {
				pendingCons.remove(nodeId);
			}
			ControlConnection cc = mina.getCCM().getCCWithId(nodeId);
			if (cc == null) {
				// Oops, CC has disappeared
				onFail();
				return;
			}
			log.info("Successfully got CC " + cc.getNodeId() + " for listening to stream " + sm.getStreamId());
			startListeningTo(cc, sourceStat);
		}

		/**
		 * @syncpriority 180
		 */
		protected void onTimeout() {
			onFail();
		}
	}
}
