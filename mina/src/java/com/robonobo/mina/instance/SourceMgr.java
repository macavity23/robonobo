package com.robonobo.mina.instance;

import static com.robonobo.common.util.TimeUtil.*;

import java.util.*;

import org.apache.commons.logging.Log;

import com.robonobo.common.concurrent.*;
import com.robonobo.core.api.proto.CoreApi.Node;
import com.robonobo.mina.instance.SourceMgr.ReqSourceStatusBatcher;
import com.robonobo.mina.message.proto.MinaProtocol.DontWantSource;
import com.robonobo.mina.message.proto.MinaProtocol.ReqSourceStatus;
import com.robonobo.mina.message.proto.MinaProtocol.SourceStatus;
import com.robonobo.mina.message.proto.MinaProtocol.StreamStatus;
import com.robonobo.mina.message.proto.MinaProtocol.WantSource;
import com.robonobo.mina.network.ControlConnection;

/**
 * Handles requesting and caching of source info
 * 
 * @author macavity
 * 
 */
public class SourceMgr {
	static final int SOURCE_CHECK_FREQ = 30; // Secs
	private MinaInstance mina;
	Log log;
	private Map<String, Set<SourceStatus>> readySources = new HashMap<String, Set<SourceStatus>>();
	/** Stream IDs that want sources */
	private Set<String> wantSources = new HashSet<String>();
	/** Information on possible sources, including when to query them next */
	private Map<String, SourceDetails> possSourcesById = new HashMap<String, SourceDetails>();
	/**
	 * Which sources to query next - this is not kept updated for performance, so there might be stale entries in here -
	 * possSourcesById contains authoritative next query times
	 */
	private Queue<PossibleSource> possSourceQ = new PriorityQueue<PossibleSource>();
	/**
	 * These are sources that have at least one endpoint, but that we can't connect to - we keep them around in case our connection status changes
	 */
	private Map<String, Set<Node>> unreachableSources = new HashMap<String, Set<Node>>();
	private Timeout queryTimeout;
	/** Batch up source requests, to avoid repeated requests to the same nodes */
	private WantSourceBatcher wsBatch;
	private Map<String, ReqSourceStatusBatcher> rssBatchers = new HashMap<String, ReqSourceStatusBatcher>();

	public SourceMgr(MinaInstance mina) {
		this.mina = mina;
		log = mina.getLogger(getClass());
		wsBatch = new WantSourceBatcher();
		queryTimeout = new Timeout(mina.getExecutor(), new CatchingRunnable() {
			public void doRun() throws Exception {
				querySources();
			}
		});
	}

	public void stop() {
		queryTimeout.cancel();
	}

	/**
	 * Tells the network we want sources
	 */
	public void wantSources(String sid) {
		synchronized (this) {
			if (wantSources.contains(sid))
				return;
			wantSources.add(sid);
		}
		if (mina.getBidStrategy().tolerateDelay(sid))
			wsBatch.add(sid);
		else {
			WantSource ws = WantSource.newBuilder().addStreamId(sid).build();
			mina.getCCM().sendMessageToNetwork("WantSource", ws);
		}
	}

	public synchronized List<String> sidsWantingSources() {
		List<String> result = new ArrayList<String>();
		result.addAll(wantSources);
		return result;
	}

	public void dontWantSources(String streamId) {
		synchronized (this) {
			if (!wantSources.contains(streamId))
				return;
			wantSources.remove(streamId);
			readySources.remove(streamId);
			unreachableSources.remove(streamId);
		}
		// We don't send DontWantSources on shutdown, so don't bother batching
		DontWantSource dws = DontWantSource.newBuilder().addStreamId(streamId).build();
		mina.getCCM().sendMessageToNetwork("DontWantSource", dws);
	}

	/**
	 * @syncpriority 180
	 */
	public void gotSource(String streamId, Node source) {
		synchronized (this) {
			if (!wantSources.contains(streamId))
				return;
		}
		if (source.getId().equals(mina.getMyNodeId().toString()))
			return;
		ControlConnection cc = mina.getCCM().getCCWithId(source.getId());
		if (cc != null && cc.getLCPair(streamId) != null)
			return;
		if (mina.getBadNodeList().checkBadNode(source.getId())) {
			log.debug("Ignoring Bad source " + source.getId());
			return;
		}
		if (mina.getNetMgr().canConnectTo(source)) {
			cacheSourceInitially(source, streamId);
			if (log.isDebugEnabled())
				log.debug("Querying source " + source.getId() + " for stream " + streamId);
			if (mina.getBidStrategy().tolerateDelay(streamId)) {
				ReqSourceStatusBatcher rssb;
				synchronized (this) {
					if (rssBatchers.containsKey(source.getId()))
						rssb = rssBatchers.get(source.getId());
					else {
						rssb = new ReqSourceStatusBatcher(source);
						rssBatchers.put(source.getId(), rssb);
					}
				}
				rssb.add(streamId);
			} else {
				ReqSourceStatus.Builder rssb = ReqSourceStatus.newBuilder();
				rssb.addStreamId(streamId);
				sendReqSourceStatus(source, rssb);
			}
		} else {
			// We can't connect to them - if they have an endpoint, keep them about, our connection status might change
			// - in particular, we might find we can do nat traversal
			if (source.getEndPointCount() > 0) {
				log.debug("Keep unreachable source "+source.getId()+" in case our network connection changes");
				synchronized (this) {
					if(!unreachableSources.containsKey(streamId))
						unreachableSources.put(streamId, new HashSet<Node>());
					unreachableSources.get(streamId).add(source);
				}
			}
		}
	}

	/**
	 * Our endpoints have changed, we might be able to contact some sources we couldn't before
	 */
	public void networkDetailsChanged() {
		Map<String, Set<Node>> sourceCopy;
		synchronized (this) {
			sourceCopy = unreachableSources;
			unreachableSources = new HashMap<String, Set<Node>>();
		}
		for (String streamId : sourceCopy.keySet()) {
			for (Node node : sourceCopy.get(streamId)) {
				gotSource(streamId, node);
			}
		}
	}
	
	/**
	 * @syncpriority 200
	 */
	public void gotSourceStatus(SourceStatus sourceStat) {
		// Remove it from our list of waiting sources - sm.foundSource() might add it again
		synchronized (this) {
			String sourceId = sourceStat.getFromNode().getId();
			SourceDetails sd = possSourcesById.get(sourceId);
			if (sd != null) {
				for (StreamStatus ss : sourceStat.getSsList()) {
					sd.streamIds.remove(ss.getStreamId());
				}
				if (sd.streamIds.size() == 0)
					possSourcesById.remove(sourceId);
			}
		}
		for (StreamStatus streamStat : sourceStat.getSsList()) {
			synchronized (this) {
				if (!wantSources.contains(streamStat.getStreamId()))
					continue;
			}
			mina.getStreamMgr().foundSource(sourceStat, streamStat);
		}
	}

	/**
	 * Called when this source does not have a listener slot open, or else one is too expensive
	 */
	public void cacheSourceUntilAgoricsAcceptable(Node node, String streamId) {
		cacheSourceUntil(node, streamId, 1000 * mina.getConfig().getSourceAgoricsFailWaitTime(), "agorics unacceptable");
	}

	/** Called when this source does not enough data to serve us */
	public void cacheSourceUntilDataAvailable(Node node, String streamId) {
		cacheSourceUntil(node, streamId, 1000 * mina.getConfig().getSourceDataFailWaitTime(), "no useful data");
	}

	/**
	 * When a connection to a node dies unexpectedly, it might be network randomness between us and them, so wait for a
	 * while then retry them
	 */
	public void cachePossiblyDeadSource(Node node, String streamId) {
		cacheSourceUntil(node, streamId, 1000 * mina.getConfig().getDeadSourceQueryTime(), "network issue");
	}

	private void cacheSourceInitially(Node node, String streamId) {
		cacheSourceUntil(node, streamId, 1000 * mina.getConfig().getInitialSourceQueryTime(), "initial query");
	}

	/**
	 * Must only be called from inside sync block!
	 */
	private void setTimeout() {
		PossibleSource ps = possSourceQ.peek();
		if (ps == null) {
			queryTimeout.clear();
			return;
		}
		queryTimeout.set(msUntil(ps.nextQ));
	}

	private synchronized void cacheSourceUntil(Node node, String streamId, int waitMs, String reason) {
		Date nextQ = timeInFuture(waitMs);
		SourceDetails sd;
		if (log.isDebugEnabled())
			log.debug("Caching source " + node.getId() + " for stream " + streamId + " until "
					+ getTimeFormat().format(nextQ));
		boolean addSourceToQ = false;
		if (possSourcesById.containsKey(node.getId())) {
			sd = possSourcesById.get(node.getId());
			if (nextQ.before(sd.nextQ)) {
				sd.nextQ = nextQ;
				addSourceToQ = true;
			}
			if (!sd.streamIds.contains(streamId))
				sd.streamIds.add(streamId);
		} else {
			sd = new SourceDetails(node, nextQ, waitMs * 2);
			sd.streamIds.add(streamId);
			addSourceToQ = true;
			possSourcesById.put(node.getId(), sd);
		}
		// This might result in duplicate entries in possSourceQ, but we live with that by checking the nextQ time in
		// possSourcesById when we pop off possSourceQ
		if (addSourceToQ) {
			possSourceQ.add(new PossibleSource(node.getId(), nextQ));
			setTimeout();
		}
	}

	/** Query all sources whose time has come */
	private void querySources() {
		while (true) {
			SourceDetails sd;
			synchronized (this) {
				if (possSourceQ.size() == 0)
					return;
				PossibleSource ps = possSourceQ.peek();
				Date now = now();
				if (ps.nextQ.after(now)) {
					setTimeout();
					return;
				}
				possSourceQ.remove();
				sd = possSourcesById.get(ps.nodeId);
				if (sd == null || sd.nextQ.after(now)) {
					// Duff entry in possSourceQ, just continue
					continue;
				}
				Node source = sd.node;
				possSourcesById.remove(source.getId());
				// Check that we still want sources for all these streams
				boolean wantIt = false;
				for (String sid : sd.streamIds) {
					if (wantSources.contains(sid)) {
						wantIt = true;
						break;
					}
				}
				if (!wantIt)
					continue;
				if (sd.retries < mina.getConfig().getSourceQueryRetries()) {
					// Re-add it again in case it doesn't answer - if it does, it'll get removed
					sd.retries = sd.retries + 1;
					sd.nextQ = timeInFuture(sd.retryAfterMs);
					sd.retryAfterMs = sd.retryAfterMs * 2;
					possSourceQ.add(new PossibleSource(source.getId(), sd.nextQ));
					possSourcesById.put(source.getId(), sd);
					if (log.isDebugEnabled())
						log.debug("Setting retry time for possible source " + source.getId() + " to "
								+ getTimeFormat().format(sd.nextQ));
				}
			}
			String sourceId = sd.node.getId();
			if (log.isDebugEnabled())
				log.debug("Querying source " + sourceId + " for streams " + sd.streamIds);
			ReqSourceStatusBatcher rssb;
			synchronized (this) {
				if (rssBatchers.containsKey(sourceId))
					rssb = rssBatchers.get(sourceId);
				else {
					rssb = new ReqSourceStatusBatcher(sd.node);
					rssBatchers.put(sourceId, rssb);
				}
			}
			rssb.addAll(sd.streamIds);
		}
	}

	/**
	 * Called when this source is good to service us, but we are not ready or able to handle it
	 */
	public synchronized void cacheSourceUntilReady(SourceStatus sourceStat, StreamStatus streamStat) {
		if (!readySources.containsKey(streamStat.getStreamId()))
			readySources.put(streamStat.getStreamId(), new HashSet<SourceStatus>());
		readySources.get(streamStat.getStreamId()).add(sourceStat);
	}

	/**
	 * Returns the set of ready sources, and removes trace of them - if you want to cache them, add them again
	 */
	public synchronized Set<SourceStatus> getReadySources(String streamId) {
		Set<SourceStatus> result = new HashSet<SourceStatus>();
		if (readySources.containsKey(streamId))
			result.addAll(readySources.remove(streamId));
		return result;
	}

	/**
	 * Returns the set of ready nodes, but doesn't remove trace of them
	 */
	public synchronized Set<Node> getReadyNodes(String streamId) {
		Set<Node> result = new HashSet<Node>();
		for (SourceStatus ss : readySources.get(streamId)) {
			result.add(ss.getFromNode());
		}
		return result;
	}

	/**
	 * Returns the set of ready nodes, but doesn't remove trace of them
	 */
	public synchronized Set<String> getReadyNodeIds(String streamId) {
		Set<String> result = new HashSet<String>();
		if (readySources.containsKey(streamId)) {
			for (SourceStatus ss : readySources.get(streamId)) {
				result.add(ss.getFromNode().getId());
			}
		}
		return result;
	}

	public synchronized int numReadySources(String streamId) {
		if (!readySources.containsKey(streamId))
			return 0;
		return readySources.get(streamId).size();
	}

	private void sendReqSourceStatus(Node source, ReqSourceStatus.Builder sourceBldr) {
		ControlConnection cc = mina.getCCM().getCCWithId(source.getId());
		// Use the right descriptor depending on whether they're a
		// local conn or not
		if (cc != null) {
			cc.sendMessage("ReqSourceStatus", sourceBldr.build());
		} else {
			sourceBldr.setFromNode(mina.getNetMgr().getDescriptorForTalkingTo(source, false));
			sourceBldr.setToNodeId(source.getId());
			mina.getCCM().sendMessageToSupernodes("ReqSourceStatus", sourceBldr.build());
		}
	}

	class PossibleSource implements Comparable<PossibleSource> {
		String nodeId;
		Date nextQ;

		public PossibleSource(String nodeId, Date nextQ) {
			this.nodeId = nodeId;
			this.nextQ = nextQ;
		}

		@Override
		public int compareTo(PossibleSource o) {
			return nextQ.compareTo(o.nextQ);
		}
	}

	/**
	 * A source that we do not need now, but which we might need in a bit
	 */
	class SourceDetails {
		Node node;
		Set<String> streamIds = new HashSet<String>();
		Date nextQ;
		int retryAfterMs;
		int retries = 0;

		public SourceDetails(Node node, Date nextQ, int retryAfterMs) {
			this.node = node;
			this.nextQ = nextQ;
			this.retryAfterMs = retryAfterMs;
		}
	}

	class WantSourceBatcher extends Batcher<String> {
		WantSourceBatcher() {
			super(mina.getConfig().getSourceRequestBatchTime(), mina.getExecutor());
		}

		@Override
		protected void runBatch(Collection<String> streamIds) {
			WantSource ws = WantSource.newBuilder().addAllStreamId(streamIds).build();
			mina.getCCM().sendMessageToNetwork("WantSource", ws);
		}
	}

	/**
	 * Use UniqueBatcher here as if we get multiple GotSources for the same node in quick succession, we'll have
	 * duplicate stream ids
	 * 
	 * @author macavity
	 * 
	 */
	class ReqSourceStatusBatcher extends UniqueBatcher<String> {
		Node source;

		ReqSourceStatusBatcher(Node source) {
			super(mina.getConfig().getSourceRequestBatchTime(), mina.getExecutor());
			this.source = source;
		}

		@Override
		protected void runBatch(Collection<String> streamIdCol) {
			synchronized (SourceMgr.this) {
				rssBatchers.remove(source.getId());
			}
			ReqSourceStatus.Builder rssb = ReqSourceStatus.newBuilder();
			rssb.addAllStreamId(streamIdCol);
			sendReqSourceStatus(source, rssb);
		}
	}

}
