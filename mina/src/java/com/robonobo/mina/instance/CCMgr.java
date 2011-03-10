package com.robonobo.mina.instance;

import static com.robonobo.mina.message.MessageUtil.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.google.protobuf.GeneratedMessage;
import com.robonobo.common.concurrent.Attempt;
import com.robonobo.core.api.proto.CoreApi.EndPoint;
import com.robonobo.core.api.proto.CoreApi.Node;
import com.robonobo.mina.external.ConnectedNode;
import com.robonobo.mina.message.HelloHelper;
import com.robonobo.mina.message.MessageUtil;
import com.robonobo.mina.message.proto.MinaProtocol.ReqConn;
import com.robonobo.mina.message.proto.MinaProtocol.ReqPublicDetails;
import com.robonobo.mina.message.proto.MinaProtocol.WantSource;
import com.robonobo.mina.network.*;
import com.robonobo.mina.stream.StreamMgr;
import com.robonobo.mina.util.MinaConnectionException;

/**
 * @syncpriority 140
 */
public class CCMgr {
	private MinaInstance mina;
	// The strings in these are nodeIds... wish we had typedefs :-(
	private Map<String, ControlConnection> cons = new HashMap<String, ControlConnection>();
	/**
	 * The value here might be null if there is no CC yet (eg if we are waiting for incoming cc)
	 */
	private Map<String, ControlConnection> inProgressCons = new HashMap<String, ControlConnection>();
	/**
	 * Nodes we are waiting for connections from - will also be in inProgressCons with null value
	 */
	private Set<String> waitingForCons = new HashSet<String>();
	private Log log;
	Map<String, Attempt> connectAttempts = new HashMap<String, Attempt>();
	private boolean shuttingDown = false;

	public CCMgr(MinaInstance mina) {
		this.mina = mina;
		log = mina.getLogger(getClass());
	}

	void prepareForShutdown() {
		shuttingDown = true;
	}

	void stop() {
		// Shut down any in-progress conns immediately
		synchronized (this) {
			for (String nodeId : inProgressCons.keySet()) {
				ControlConnection cc = inProgressCons.get(nodeId);
				if (cc != null)
					cc.close();
			}
		}
		waitingForCons.clear();

		Object[] ccNames = cons.keySet().toArray();
		for (int i = 0; i < ccNames.length; i++) {
			ControlConnection cc = cons.get(ccNames[i]);
			if (cc != null) {
				cc.closeGracefully("Mina exiting");
			}
		}
		shuttingDown = true;
		for (Attempt ca : connectAttempts.values()) {
			ca.cancel();
		}
		connectAttempts.clear();

		// Wait for CCs to exit, there may be pending account closures
		// Could replace this with a wait/notify arrangement, but seeing as this
		// is only on shutdown it seems worth it to eat the polling hit for
		// simplicity's sake
		try {
			while (true) {
				boolean wait = false;
				for (int i = 0; i < ccNames.length; i++) {
					ControlConnection cc = cons.get(ccNames[i]);
					if (cc != null) {
						if (!cc.isClosed()) {
							wait = true;
							break;
						}
					}
				}
				if (wait)
					Thread.sleep(1000L);
				else
					return;
			}
		} catch (InterruptedException e) {
			log.error("Caught InterruptedE closing down CCM", e);
		}
	}

	void abort() {
		synchronized (this) {
			for (ControlConnection cc : cons.values()) {
				cc.abort();
			}
			cons.clear();
			for (String nodeId : inProgressCons.keySet()) {
				ControlConnection cc = inProgressCons.get(nodeId);
				if (cc != null)
					cc.abort();
			}
			inProgressCons.clear();
			waitingForCons.clear();
			for (Attempt ca : connectAttempts.values()) {
				ca.cancel();
			}
			connectAttempts.clear();
		}
	}

	/**
	 * @syncpriority 140
	 */
	public synchronized boolean haveRunningOrPendingCCTo(String newNodeId) {
		return cons.containsKey(newNodeId) || inProgressCons.containsKey(newNodeId);
	}

	/**
	 * @syncpriority 140
	 */
	public synchronized boolean haveSupernode() {
		for (ControlConnection con : cons.values()) {
			if (con.getNode().getSupernode())
				return true;
		}
		return false;
	}

	/**
	 * @syncpriority 140
	 */
	public synchronized boolean haveLocalConn() {
		for (ControlConnection con : cons.values()) {
			if (con.isLocal())
				return true;
		}
		return false;
	}

	public synchronized Set<String> getConnectedNodeIds() {
		HashSet<String> result = new HashSet<String>();
		result.addAll(cons.keySet());
		return result;
	}

	/**
	 * @syncpriority 140
	 */
	public synchronized ControlConnection getCCWithId(String nodeId) {
		return cons.get(nodeId);
	}

	/**
	 * @syncpriority 170
	 */
	public List<ConnectedNode> getConnectedNodes() {
		List<ControlConnection> ccList;
		synchronized (this) {
			ccList = new ArrayList<ControlConnection>(cons.values());
		}
		List<ConnectedNode> result = new ArrayList<ConnectedNode>(ccList.size());
		for (ControlConnection cc : ccList) {
			result.add(buildConnectedNode(cc));
		}
		return result;
	}

	/**
	 * @syncpriority 170
	 */
	public ConnectedNode buildConnectedNode(ControlConnection cc) {
		ConnectedNode node = new ConnectedNode();
		String nodeId = cc.getNodeId();
		node.setNodeId(nodeId);
		node.setAppUri(cc.getNode().getApplicationUri());
		node.setEndPointUrl(cc.getTheirEp().getUrl());
		node.setSupernode(cc.getNode().getSupernode());
		node.setDownloadRate(cc.getDownFlowRate());
		node.setUploadRate(cc.getUpFlowRate());
		if (mina.getConfig().isAgoric()) {
			double myBid = mina.getBuyMgr().getAgreedBidTo(nodeId);
			if (myBid < 0d)
				myBid = 0d;
			node.setMyBid(myBid);
			node.setTheirBid(mina.getSellMgr().getAgreedBidFrom(nodeId));
			node.setMyGamma(mina.getBuyMgr().calculateMyGamma(nodeId));
			node.setTheirGamma(cc.getBroadcastGamma());
		}
		return node;
	}

	/**
	 * Tries to connect to the supplied node. This method will try different endpoints/methods in turn
	 * 
	 * @syncpriority 140
	 */
	public void makeCCTo(Node nd, Attempt onCompletionAttempt) {
		makeCCTo(nd, onCompletionAttempt, true, null);
	}

	/**
	 * @syncpriority 140
	 */
	public void makeCCTo(Node nd, Attempt onCompletionAttempt, boolean sendReqConn, List<EndPoint> triedEps) {
		if (shuttingDown) {
			log.debug("CCM not initiating CC to " + nd.getId() + ": shutting down");
			return;
		}
		String newNodeId = nd.getId();
		synchronized (this) {
			// Check if we have an existing or in-progress connection with this
			// node
			if (cons.containsKey(newNodeId)) {
				log.debug("Not attempting connection to already-connected node " + newNodeId);
				if (onCompletionAttempt != null)
					onCompletionAttempt.succeeded();
				return;
			}
			if (inProgressCons.containsKey(newNodeId)) {
				log.debug("Not attempting connection to in-progress node " + newNodeId);
				if (onCompletionAttempt != null) {
					log.debug("Adding contingent attempt to in-progress connection with " + newNodeId);
					if (connectAttempts.containsKey(newNodeId))
						connectAttempts.get(newNodeId).addContingentAttempt(onCompletionAttempt);
					else
						connectAttempts.put(newNodeId, onCompletionAttempt);
				}
				return;
			}
			// If the node is bad, don't connect again
			if (mina.getBadNodeList().checkBadNode(newNodeId)) {
				log.debug("Not connecting to bad node " + newNodeId);
				if (onCompletionAttempt != null) {
					onCompletionAttempt.failed();
				}
				return;
			}
			log.debug("Initiating connection to " + newNodeId);
			// Add it here with a null cc to make sure we don't make duplicate
			// connection attempts - then on the other side of the sync break,
			// add it again with the cc
			inProgressCons.put(newNodeId, null);
		}
		// Come out of sync here as this may take 30+ secs
		ControlConnection cc = null;
		for (EndPointMgr epMgr : mina.getNetMgr().getEndPointMgrs()) {
			cc = epMgr.connectTo(nd, triedEps, sendReqConn);
			if (cc != null)
				break;
		}
		synchronized (this) {
			inProgressCons.put(newNodeId, cc);
			if (cc == null) {
				// No way for us to connect to them - send a ReqConn to ask them
				// to connect to us if they can (unless we're here as a result
				// of them asking us, in which case fail)
				if (sendReqConn && mina.getNetMgr().havePublicEndpoints()) {
					ReqConn rcMsg = ReqConn.newBuilder().setFromNode(mina.getNetMgr().getPublicNodeDesc())
							.setToNodeId(newNodeId).build();
					log.debug("Sending Connection Request to " + newNodeId);
					waitingForCons.add(newNodeId);
					sendOrForwardMessageTo("ReqConn", rcMsg, newNodeId);
				} else {
					log.error("No more connection methods exist for connecting to " + nd.getId());
					if (onCompletionAttempt != null)
						onCompletionAttempt.failed();
					inProgressCons.remove(newNodeId);
					return;
				}
			}
			// Set up our connection attempt
			EndPoint theirEp = (cc == null) ? null : cc.getTheirEp();
			ConnectAttempt ca = new ConnectAttempt(mina.getConfig().getMessageTimeout(), nd, theirEp, triedEps,
					sendReqConn);
			if (onCompletionAttempt != null)
				ca.addContingentAttempt(onCompletionAttempt);
			ca.start();
			connectAttempts.put(newNodeId, ca);
		}
	}

	/**
	 * @syncpriority 140
	 */
	public synchronized void sendMessageTo(String msgName, GeneratedMessage msg, String toNodeId) {
		if (cons.containsKey(toNodeId)) {
			ControlConnection cc = cons.get(toNodeId);
			cc.sendMessage(msgName, msg);
		} else {
			log.error("Not sending " + msgName + " to " + toNodeId + ": no connection");
		}
	}

	/**
	 * @syncpriority 140
	 */
	public synchronized void sendOrForwardMessageTo(String msgName, GeneratedMessage msg, String toNodeId) {
		if (cons.containsKey(toNodeId)) {
			sendMessageTo(msgName, msg, toNodeId);
		} else {
			sendMessageToSupernodes(msgName, msg);
		}
	}

	/**
	 * @syncpriority 140
	 */
	public synchronized void sendMessageTo(String msgName, GeneratedMessage msg, String[] toNodes) {
		for (int i = 0; i < toNodes.length; i++) {
			sendMessageTo(msgName, msg, toNodes[i]);
		}
	}

	/**
	 * @syncpriority 140
	 */
	public synchronized void sendMessageToNetwork(String msgName, GeneratedMessage msg) {
		sendMessageToSupernodes(msgName, msg);
		sendMessageToLocals(msgName, msg, false);
	}

	/**
	 * @syncpriority 140
	 */
	public synchronized void sendMessageToSupernodes(String msgName, GeneratedMessage msg) {
		for (ControlConnection cc : cons.values()) {
			if (cc.getNode().getSupernode()) {
				cc.sendMessage(msgName, msg);
			}
		}
	}

	/**
	 * @syncpriority 140
	 */
	public void sendMessageToLocals(String msgName, GeneratedMessage msg) {
		sendMessageToLocals(msgName, msg, true);
	}

	/**
	 * @syncpriority 140
	 */
	private synchronized void sendMessageToLocals(String msgName, GeneratedMessage msg, boolean incSuperNodes) {
		for (ControlConnection cc : cons.values()) {
			if (cc.isLocal()) {
				if (cc.getNode().getSupernode() && !incSuperNodes)
					continue;
				cc.sendMessage(msgName, msg);
			}
		}
	}

	/**
	 * @syncpriority 140
	 */
	public synchronized void sendMessageToNonLocals(String msgName, GeneratedMessage msg) {
		for (ControlConnection cc : cons.values()) {
			if (!cc.isLocal())
				cc.sendMessage(msgName, msg);
		}
	}

	/**
	 * @syncpriority 140
	 */
	public void makeCCFrom(HelloHelper helHelper, StreamConnectionFactory scm) throws MinaConnectionException {
		if (shuttingDown)
			return;

		Node targetNode = helHelper.getHello().getNode();
		String helloId = targetNode.getId();

		ControlConnection cc;
		synchronized (this) {
			String myNodeId = mina.getMyNodeId();
			if (myNodeId.equals(helloId))
				throw new MinaConnectionException("Connection attempt from apparently my node ID. Closing.");
			// See if we're waiting for a connection from this guy
			if (waitingForCons.contains(helloId))
				waitingForCons.remove(helloId);
			else {
				if (cons.containsKey(helloId)) {
					log.error("Ignoring conn attempt from node " + helloId + ": already have connection");
					return;
				}
				if (inProgressCons.containsKey(helloId)) {
					// It's possible, between low-computron nodes on a local network, that two nodes trying to connect
					// to each other both get here at the same time
					// We cross these treacherous waters by rejecting only one end - the one with the higher node id
					if (myNodeId.compareTo(helloId) > 0) {
						log.error("Ignoring simultaneous conn attempt from node " + helloId
								+ " - my attempt should get through");
						return;
					}
				}
			}
			cc = new ControlConnection(mina, helHelper, scm);
			inProgressCons.put(helloId, cc);
		}
		cc.completeHandshake();
	}

	/**
	 * @syncpriority 170
	 */
	public void notifyDeadConnection(ControlConnection cc) {
		String nodeId = cc.getNodeId();
		boolean wasConnected = false;
		Attempt a;
		log.debug("Cleaning up CC " + nodeId);
		if (mina.getConfig().isSupernode())
			mina.getSupernodeMgr().notifyDeadConnection(cc.getNode());
		synchronized (this) {
			inProgressCons.remove(nodeId);
			waitingForCons.remove(nodeId);
			wasConnected = cons.containsKey(nodeId);
			cons.remove(nodeId);
			// If we've lost our supernode, ask for more
			if (cc.getNode().getSupernode() && !mina.getConfig().isSupernode() && !shuttingDown) {
				int numSupernodes = 0;
				for (ControlConnection thisCC : cons.values()) {
					if (thisCC.getNode().getSupernode())
						numSupernodes++;
				}
				if (numSupernodes == 0)
					mina.getNetMgr().locateMoreNodes();
			}
			a = connectAttempts.remove(nodeId);
		}
		if (a instanceof ConnectAttempt)
			((ConnectAttempt) a).tryNextMethodOrFail();
		else if (a != null)
			a.failed();
		if (wasConnected)
			mina.getEventMgr().fireNodeDisconnected(buildConnectedNode(cc));
		if (mina.getConfig().isAgoric()) {
			mina.getSellMgr().notifyDeadConnection(nodeId);
			mina.getBuyMgr().notifyDeadConnection(nodeId);
		}
	}

	/**
	 * @syncpriority 140
	 */
	public void notifySuccessfulConnection(ControlConnection cc) {
		log.info("Successfully formed Control Connection with NodeID " + cc.getNodeId());
		synchronized (this) {
			inProgressCons.remove(cc.getNodeId());
			cons.put(cc.getNodeId(), cc);
			Attempt conAttempt = connectAttempts.remove(cc.getNodeId());
			if (conAttempt != null) {
				conAttempt.succeeded();
			}
			mina.getBadNodeList().markNodeAsGood(cc.getNodeId());
			// If this is a supernode, tell them about our broadcasts, and get
			// broadcasters for any streams we're receiving
			if (cc.getNode().getSupernode()) {
				StreamMgr[] sms = mina.getSmRegister().getAllSMs();
				// Batch up our stream adverts (there might be lots of them),
				// but send out broadcaster searches individually (probably
				// won't be many)
				for (int i = 0; i < sms.length; i++) {
					if (sms[i].isBroadcasting() || sms[i].isRebroadcasting())
						mina.getStreamAdvertiser().advertiseStream(sms[i].getStreamId());
					if (mina.getSourceMgr().wantsSource(sms[i].getStreamId())) {
						List<String> streamIds = new ArrayList<String>();
						streamIds.add(sms[i].getStreamId());
						cc.sendMessage("WantSource", WantSource.newBuilder().addAllStreamId(streamIds).build());
					}
				}
			}
		}
		if (mina.getEscrowMgr() != null)
			mina.getEscrowMgr().notifySuccessfulConnection(cc);
		mina.getEventMgr().fireNodeConnected(buildConnectedNode(cc));
		checkNATTraversal(cc);
	}

	/**
	 * Use this new connection to figure out if our NAT (if any) supports traversal
	 */
	private void checkNATTraversal(ControlConnection cc) {
		boolean needToCheck = false;
		for (EndPointMgr epMgr : mina.getNetMgr().getEndPointMgrs()) {
			if (!epMgr.natTraversalDecided()) {
				needToCheck = true;
				break;
			}
		}
		if (needToCheck) {
			ReqPublicDetails.Builder b = ReqPublicDetails.newBuilder();
			b.setFromNodeId(mina.getMyNodeId());
			cc.sendMessage("ReqPublicDetails", b.build());
		}
	}

	public boolean isShuttingDown() {
		return shuttingDown;
	}

	/**
	 * Updates our flow-control gamma values
	 * 
	 * @syncpriority 140
	 */
	public synchronized void updateGammas(Map<String, Float> gammas) {
		for (ControlConnection cc : cons.values()) {
			float gamma = (gammas.containsKey(cc.getNodeId()) ? gammas.get(cc.getNodeId()) : 0);
			cc.setBroadcastGamma(gamma);
			for (BCPair bcp : cc.getBCPairs()) {
				bcp.setGamma(gamma);
			}
		}
	}

	class ConnectAttempt extends Attempt {
		private Node node;
		private String nodeId;
		private EndPoint tryingEp;
		private List<EndPoint> triedEps;
		private boolean sendReqConnIfFail;

		public ConnectAttempt(int timeoutSecs, Node node, EndPoint tryingEp, List<EndPoint> triedEps,
				boolean sendReqConnIfFail) {
			super(mina.getExecutor(), timeoutSecs * 1000, "ConnectAttempt");
			this.node = node;
			nodeId = node.getId();
			this.tryingEp = tryingEp;
			if (triedEps == null)
				triedEps = new ArrayList<EndPoint>();
			this.triedEps = triedEps;
			this.sendReqConnIfFail = sendReqConnIfFail;
		}

		public void onTimeout() {
			log.info("Timeout waiting for connection from " + nodeId);
			connectAttempts.remove(nodeId);
			synchronized (CCMgr.this) {
				inProgressCons.remove(nodeId);
				waitingForCons.remove(nodeId);
			}
		}

		@Override
		protected void onFail() {
			synchronized (CCMgr.this) {
				inProgressCons.remove(nodeId);
				waitingForCons.remove(nodeId);
			}
		}

		public void tryNextMethodOrFail() {
			log.info("Attempt failed connecting to " + nodeId);
			connectAttempts.remove(nodeId);
			if (tryingEp == null) {
				failed();
				return;
			}
			triedEps.add(tryingEp);
			Attempt ca = (contingentAttempts.size() > 0) ? contingentAttempts.get(0) : null;
			if (contingentAttempts.size() > 1) {
				for (int i = 1; i < contingentAttempts.size(); i++) {
					ca.addContingentAttempt(contingentAttempts.get(i));
				}
			}
			CCMgr.this.makeCCTo(node, ca, sendReqConnIfFail, triedEps);
		}

		@Override
		public void onSuccess() {
			synchronized (CCMgr.this) {
				log.debug("Attempt successful connecting to " + nodeId);
				connectAttempts.remove(nodeId);
			}
			mina.getBadNodeList().markNodeAsGood(nodeId);
		}
	}

}
