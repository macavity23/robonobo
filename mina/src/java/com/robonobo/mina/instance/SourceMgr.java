package com.robonobo.mina.instance;

import static com.robonobo.common.util.TimeUtil.*;

import java.util.*;

import org.apache.commons.logging.Log;

import com.robonobo.common.concurrent.*;
import com.robonobo.core.api.proto.CoreApi.Node;
import com.robonobo.mina.message.proto.MinaProtocol.DontWantSource;
import com.robonobo.mina.message.proto.MinaProtocol.ReqSourceStatus;
import com.robonobo.mina.message.proto.MinaProtocol.SourceStatus;
import com.robonobo.mina.message.proto.MinaProtocol.StreamStatus;
import com.robonobo.mina.message.proto.MinaProtocol.WantSource;
import com.robonobo.mina.network.ControlConnection;
import com.robonobo.mina.stream.StreamMgr;

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
	private Map<String, Map<String, SourceStatus>> readySources = new HashMap<String, Map<String, SourceStatus>>();
	/** Stream IDs that want sources */
	private Set<String> wantSources = new HashSet<String>();
	/** Information on possible sources, including when to query them next */
	private Map<String, SourceDetails> possSourcesById = new HashMap<String, SourceDetails>();
	/**
	 * Which sources to query next - this is not kept updated for performance, so there might be stale
	 * entries in here - possSourcesById contains authoritative next query times
	 */
	private Queue<PossibleSource> possSourceQ = new PriorityQueue<PossibleSource>();
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
	 * 
	 * @param tolerateDelay
	 *            false to send the request for sources out immediately (otherwise waits <5sec to batch requests
	 *            together)
	 */
	public void wantSources(String streamId, boolean tolerateDelay) {
		synchronized (this) {
			if (wantSources.contains(streamId))
				return;
			wantSources.add(streamId);
		}
		if (tolerateDelay)
			wsBatch.add(streamId);
		else {
			WantSource ws = WantSource.newBuilder().addStreamId(streamId).build();
			mina.getCCM().sendMessageToNetwork("WantSource", ws);
		}
	}

	public synchronized boolean wantsSource(String streamId) {
		return wantSources.contains(streamId);
	}

	public void dontWantSources(String streamId) {
		synchronized (this) {
			if (!wantSources.contains(streamId))
				return;
			wantSources.remove(streamId);
			readySources.remove(streamId);
		}
		// We don't send DontWantSources on shutdown, so don't bother batching
		DontWantSource dws = DontWantSource.newBuilder().addStreamId(streamId).build();
		mina.getCCM().sendMessageToNetwork("DontWantSource", dws);
	}

	public void gotSource(String streamId, Node source) {
		synchronized (this) {
			if (!wantSources.contains(streamId))
				return;
		}
		if (source.getId().equals(mina.getMyNodeId().toString()))
			return;
		if (mina.getBadNodeList().checkBadNode(source.getId())) {
			log.debug("Ignoring Bad source " + source.getId());
			return;
		}
		if (!mina.getNetMgr().canConnectTo(source)) {
			log.debug("Ignoring source " + source + " - cannot connect");
			return;
		}
		StreamMgr sm = mina.getSmRegister().getSM(streamId);
		if (sm == null)
			return;
		cacheSourceInitially(source, streamId);
		SourceDetails sd;
		synchronized (this) {
			sd = possSourcesById.get(source.getId());
		}
		queryStatus(sd, sm.tolerateDelay());
	}

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
			StreamMgr sm = mina.getSmRegister().getSM(streamStat.getStreamId());
			if (sm != null)
				sm.foundSource(sourceStat, streamStat);
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

	/** Must only be called from inside sync block! 
	 */
	private void setTimeout() {
		PossibleSource ps = possSourceQ.peek();
		if(ps == null) {
			queryTimeout.clear();
			return;
		}
		queryTimeout.set(msUntil(ps.nextQ));
	}
	
	private synchronized void cacheSourceUntil(Node node, String streamId, int waitMs, String reason) {
		Date nextQ = timeInFuture(waitMs);
		SourceDetails sd;
		if(log.isDebugEnabled())
			log.debug("Caching source "+node.getId()+" for stream "+streamId+" until "+getTimeFormat().format(nextQ));
		boolean addSourceToQ = false;
		if (possSourcesById.containsKey(node.getId())) {
			sd = possSourcesById.get(node.getId());
			if(nextQ.before(sd.nextQ)) {
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
		if(addSourceToQ) {
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
				if(ps.nextQ.after(now)) {
					setTimeout();
					return;
				}
				possSourceQ.remove();
				sd = possSourcesById.get(ps.nodeId);
				if(sd == null || sd.nextQ.after(now)) {
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
					if(log.isDebugEnabled())
						log.debug("Setting retry time for possible source "+source.getId()+" to "+getTimeFormat().format(sd.nextQ));
				}
			}
			queryStatus(sd, true);
		}
	}

	private void queryStatus(SourceDetails sd, boolean tolerateDelay) {
		if(log.isDebugEnabled())
			log.debug("Querying source "+sd.node.getId()+" for streams "+sd.streamIds);
		if (tolerateDelay) {
			ReqSourceStatusBatcher rssb;
			synchronized (this) {
				if (rssBatchers.containsKey(sd.node.getId()))
					rssb = rssBatchers.get(sd.node.getId());
				else {
					rssb = new ReqSourceStatusBatcher(sd.node);
					rssBatchers.put(sd.node.getId(), rssb);
				}
			}
			rssb.addAll(sd.streamIds);
		} else {
			ReqSourceStatus.Builder rssb = ReqSourceStatus.newBuilder();
			rssb.addAllStreamId(sd.streamIds);
			sendReqSourceStatus(sd.node, rssb);
		}
	}

	/**
	 * Called when this source is good to service us, but we are not ready or able to handle it
	 */
	public synchronized void cacheSourceUntilReady(SourceStatus sourceStat, StreamStatus streamStat) {
		if (!readySources.containsKey(streamStat.getStreamId()))
			readySources.put(streamStat.getStreamId(), new HashMap<String, SourceStatus>());
		readySources.get(streamStat.getStreamId()).put(sourceStat.getFromNode().getId(), sourceStat);
	}

	/**
	 * Returns the set of ready sources, and removes trace of them - if you want to cache them, add them again
	 */
	public synchronized Set<SourceStatus> getReadySources(String streamId) {
		Set<SourceStatus> result = new HashSet<SourceStatus>();
		if (readySources.containsKey(streamId))
			result.addAll(readySources.remove(streamId).values());
		return result;
	}

	/**
	 * Returns the set of ready nodes, but doesn't remove trace of them
	 */
	public synchronized Set<Node> getReadyNodes(String streamId) {
		Set<Node> result = new HashSet<Node>();
		for (SourceStatus ss : readySources.get(streamId).values()) {
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
			for (SourceStatus ss : readySources.get(streamId).values()) {
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
