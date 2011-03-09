package com.robonobo.mina.stream;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.commons.logging.Log;

import com.robonobo.common.concurrent.CatchingRunnable;
import com.robonobo.common.exceptions.SeekInnerCalmException;
import com.robonobo.core.api.StreamVelocity;
import com.robonobo.core.api.proto.CoreApi.Node;
import com.robonobo.mina.agoric.AuctionState;
import com.robonobo.mina.agoric.BuyMgr;
import com.robonobo.mina.external.FoundSourceListener;
import com.robonobo.mina.external.StreamingDetails;
import com.robonobo.mina.external.buffer.Page;
import com.robonobo.mina.external.buffer.PageBuffer;
import com.robonobo.mina.external.buffer.StreamPosition;
import com.robonobo.mina.instance.MinaInstance;
import com.robonobo.mina.message.proto.MinaProtocol.SourceStatus;
import com.robonobo.mina.message.proto.MinaProtocol.StreamStatus;
import com.robonobo.mina.message.proto.MinaProtocol.UnAdvSource;
import com.robonobo.mina.network.BCPair;
import com.robonobo.mina.network.ConnectionPair;
import com.robonobo.mina.network.LCPair;
import com.robonobo.mina.stream.bidstrategy.BidStrategy;
import com.robonobo.mina.util.MinaConnectionException;

/**
 * Main class for dealing with stream broadcast and reception. One per stream.
 * 
 * @author macavity
 * @syncpriority 200
 */
public class StreamMgr {
	Random rand = new Random();
	protected Log log;
	protected MinaInstance mina;
	protected String streamId;
	protected PageBuffer pageBuf;
	protected StreamConnsMgr streamConns;
	protected PageRequestMgr prm;
	protected boolean broadcasting = false;
	protected boolean receiving = false;
	protected boolean rebroadcasting = false;
	/** Keep note of this here so we can tell our pagebuffer */
	protected long totalPages;
	private Set<FoundSourceListener> listeners = new HashSet<FoundSourceListener>();
	int priority;
	BidStrategy bidStrategy;
	int bFlowRate, lFlowRate;
	long lastFRUpdate = 0;

	/**
	 * Don't use this ctor - use SMRegister.getOrCreateCM() instead
	 */
	public StreamMgr(MinaInstance mina, String streamId, PageBuffer pageBuf) {
		this.mina = mina;
		log = mina.getLogger(getClass());
		this.streamId = streamId;
		this.pageBuf = pageBuf;
		streamConns = new StreamConnsMgr(this);
		prm = new PageRequestMgr(this);
		createBidStrategy();
	}

	/** Creates the bid strategy based on the class specified in the config file */
	private void createBidStrategy() {
		try {
			bidStrategy = (BidStrategy) Class.forName(mina.getConfig().getBidStrategyClass()).newInstance();
		} catch (Exception e) {
			throw new SeekInnerCalmException(e);
		}
		bidStrategy.setStreamMgr(this);
	}

	/**
	 * @syncpriority 200
	 */
	public synchronized void abort() {
		streamConns.abort();
		mina.getSmRegister().unregisterSM(streamId);
	}

	/**
	 * Checks to see if we still want sources, and if we don't, notifies the
	 * network
	 * 
	 * @syncpriority 200
	 */
	private boolean checkWantingSources() {
		// Don't do anything if we are shutting down, when we disconnect the
		// supernode will figure it out
		if (mina.getCCM().isShuttingDown())
			return false;
		boolean wantingSources = false;
		synchronized (this) {
			wantingSources = ((listeners.size() != 0) || receiving);
		}
		if (!wantingSources)
			mina.getSourceMgr().dontWantSources(streamId);
		return wantingSources;
	}

	/** true if we're willing to put up with a ~5s delay on requesting sources */
	public boolean tolerateDelay() {
		StreamVelocity sv = getStreamVelocity();
		if (sv == null || sv == StreamVelocity.LowestCost)
			return true;
		return false;
	}

	/**
	 * @syncpriority 200
	 */
	public synchronized void addFoundSourceListener(FoundSourceListener listener) {
		boolean sendWantSources = (listeners.size() == 0);
		listeners.add(listener);
		if(sendWantSources)
			mina.getSourceMgr().wantSources(streamId, tolerateDelay());
	}

	/**
	 * @syncpriority 200
	 */
	public synchronized void removeFoundSourceListener(FoundSourceListener listener) {
		listeners.remove(listener);
		checkWantingSources();
	}

	/**
	 * @syncpriority 180
	 */
	public Set<Node> getKnownSources() {
		Set<Node> result = mina.getSourceMgr().getReadyNodes(streamId);
		for (ConnectionPair lcp : streamConns.getAllListenConns()) {
			result.add(lcp.getCC().getNode());
		}
		return result;
	}

	public Set<String> getSourceNodeIds() {
		Set<String> result = mina.getSourceMgr().getReadyNodeIds(streamId);
		for (ConnectionPair lcp : streamConns.getAllListenConns()) {
			result.add(lcp.getCC().getNode().getId());
		}
		return result;
	}

	/**
	 * @syncpriority 160
	 */
	public int numSources() {
		int result = mina.getSourceMgr().numReadySources(streamId);
		result += streamConns.getNumListenConns();
		return result;
	}
	
	public StreamStatus buildStreamStatus(String toNodeId) {
		StreamStatus.Builder bldr = StreamStatus.newBuilder();
		if (toNodeId != null) {
			bldr.setToNodeId(toNodeId);
			bldr.setFromNodeId(mina.getMyNodeId());
		}
		bldr.setStreamId(streamId);
		if (pageBuf.getTotalPages() > 0)
			bldr.setTotalPages(pageBuf.getTotalPages());
		StreamPosition sp = pageBuf.getStreamPosition();
		bldr.setLastContiguousPage(sp.getLastContiguousPage());
		if (sp.getPageMap() > 0)
			bldr.setPageMap(sp.getPageMap());
		return bldr.build();
	}

	public void foundSource(SourceStatus sourceStat, StreamStatus streamStat) {
		String fromNodeId = sourceStat.getFromNode().getId();
		String sourceId = fromNodeId;
		if (streamStat.getTotalPages() != 0) {
			long totalPages = streamStat.getTotalPages();
			if (pageBuf == null)
				this.totalPages = totalPages;
			else {
				if (pageBuf.getTotalPages() <= 0)
					pageBuf.setTotalPages(totalPages);
				else {
					if (pageBuf.getTotalPages() != totalPages) {
						log.error("Node " + sourceId + " advertised total pages " + totalPages + " for stream "
								+ streamId + ", but I have already recorded " + pageBuf.getTotalPages());
						return;
					}
				}
			}
		}

		if (streamConns.haveListenConnWithId(sourceId))
			return;

		// If we're not receiving, then we're just caching sources to pass to an external listener - so cache it
		if (!receiving) {
			mina.getSourceMgr().cacheSourceUntilReady(sourceStat, streamStat);
			notifyListenersOfSource(sourceId);
			return;
		}

		// Not listening to this node - let's see if we should
		if (mina.getConfig().isAgoric()) {
			AuctionState as = new AuctionState(sourceStat.getAuctionState());
			if (!bidStrategy.worthConnectingTo(as)) {
				log.debug("Not connecting to node " + sourceId + " for " + streamId + " - bid strategy says no");
				mina.getSourceMgr().cacheSourceUntilAgoricsAcceptable(sourceStat.getFromNode(), streamId);
				return;
			}
			if(!mina.getBuyMgr().canListenTo(as, getStreamVelocity())) {
				log.debug("Not connecting to node " + sourceId + " for " + streamId + " - no available listener slots at my price level");
				mina.getSourceMgr().cacheSourceUntilAgoricsAcceptable(sourceStat.getFromNode(), streamId);
				return;
			}
		}

		// Check that they have data useful to us
		if (pageBuf != null) {
			StreamPosition sp = new StreamPosition(streamStat.getLastContiguousPage(), streamStat.getPageMap());
			if(!prm.isUsefulSource(sp)) {
				// Useless at the moment - cache them and ask again later
				mina.getSourceMgr().cacheSourceUntilDataAvailable(sourceStat.getFromNode(), streamId);
				return;
			}
		}

		// This is a useful source - let our listeners know
		notifyListenersOfSource(sourceId);

		if (streamConns.getNumListenConns() >= mina.getConfig().getMaxSources()) {
			mina.getSourceMgr().cacheSourceUntilReady(sourceStat, streamStat);
			return;
		}
		try {
			streamConns.makeListenConnectionTo(sourceStat);
		} catch (MinaConnectionException e) {
			log.error("Caught MinaConnectionException when attempting to connect to " + sourceId);
		}
	}

	private void notifyListenersOfSource(final String sourceId) {
		final FoundSourceListener[] lisArr;
		synchronized (this) {
			lisArr = new FoundSourceListener[listeners.size()];
			listeners.toArray(lisArr);			
		}
		mina.getExecutor().execute(new CatchingRunnable() {
			public void doRun() throws Exception {
				for (FoundSourceListener listener : lisArr) {
					listener.foundBroadcaster(streamId, sourceId);
				}
			}
		});
	}

	public MinaInstance getMinaInstance() {
		return mina;
	}

	public PageBuffer getPageBuffer() {
		return pageBuf;
	}

	public PageRequestMgr getPRM() {
		return prm;
	}

	/**
	 * Bytes per second
	 */
	public int getBroadcastingFlowRate() {
		checkAndUpdateFlowRates();
		return bFlowRate;
	}
	
	/**
	 * Bytes per second
	 */
	public int getListeningFlowRate() {
		checkAndUpdateFlowRates();
		return lFlowRate;
	}

	private void checkAndUpdateFlowRates() {
		// If it's been more than a second, fire off a thread to update the flow rate
		// Avoids repeated iteration over connections, and nary a synch block in sight
		// This means this flowrate data might be out of date, but this shouldn't matter
		long now = System.currentTimeMillis();
		if((now - lastFRUpdate) > 1000l) {
			lastFRUpdate = now;
			mina.getExecutor().execute(new CatchingRunnable() {
				public void doRun() throws Exception {
					int bfr = 0;
					BCPair[] bcps = getStreamConns().getBroadcastConns();
					for (int i = 0; i < bcps.length; i++) {
						bfr += bcps[i].getFlowRate();
					}
					bFlowRate = bfr;
					int lfr = 0;
					LCPair[] lcps = getStreamConns().getAllListenConns();
					for (int i = 0; i < lcps.length; i++) {
						lfr += lcps[i].getFlowRate();
					}
					lFlowRate = lfr;
				}
			});
		}
	}
	
	public StreamConnsMgr getStreamConns() {
		return streamConns;
	}

	public String getStreamId() {
		return streamId;
	}

	/**
	 * @syncpriority 160
	 */
	public StreamingDetails getStreamingDetails() {
		return streamConns.getStreamingDetails();
	}

	/**
	 * @syncpriority 200
	 */
	public void receivePage(Page p) {
		synchronized (this) {
			// We might have already stopped receiving, in which case the
			// pagebuf will be closed
			if (!isReceiving())
				return;
			try {
				pageBuf.putPage(p);
			} catch (IOException e) {
				log.error("Error putting page into buffer", e);
				stop();
				return;
			}
		}
		prm.notifyPageReceived(p.getPageNumber());
		updateStreamStatus();
		if(isReceiving() && !isRebroadcasting())
			startRebroadcast();
		if (pageBuf.getLastContiguousPage() == (pageBuf.getTotalPages() - 1))
			receptionCompleted();
	}

	/**
	 * @syncpriority 160
	 */
	private void updateStreamStatus() {
		StreamStatus ss = buildStreamStatus(null);
		for (BCPair bcPair : streamConns.getBroadcastConns()) {
			bcPair.getCC().sendMessage("StreamStatus", ss);
		}
	}

	public boolean isBroadcasting() {
		return broadcasting;
	}

	public boolean isRebroadcasting() {
		return rebroadcasting;
	}

	public boolean isReceiving() {
		return receiving;
	}

	/**
	 * @syncpriority 200
	 */
	public void notifyDeadConnection(ConnectionPair pair) {
		synchronized (this) {
			streamConns.removeConnectionPair(pair);
			if (pair instanceof LCPair) {
				prm.notifyDeadConnection(pair.getCC().getNodeId());
				if (streamConns.getNumListenConns() < mina.getConfig().getMaxSources())
					requestCachedSources();
				bidStrategy.cleanup(pair.getCC().getNodeId());
				// Make note of them in case they come back
				LCPair lcp = (LCPair) pair;
				mina.getSourceMgr().cachePossiblyDeadSource(lcp.getCC().getNode(), streamId);
			}
			if (streamConns.getNumBroadcastConns() == 0 && streamConns.getNumListenConns() == 0) {
				try {
					pageBuf.sleep();
				} catch (IOException e) {
					log.error("Error sleeping page buffer", e);
					stop();
				}
			}
		}
		if (isReceiving())
			mina.getEventMgr().fireReceptionConnsChanged(streamId);
	}

	/**
	 * Returns a list of node ids
	 */
	public synchronized List<String> getConnectedSources() {
		ConnectionPair[] lcs = streamConns.getAllListenConns();
		List<String> result = new ArrayList<String>(lcs.length);
		for (int i = 0; i < lcs.length; i++) {
			result.add(lcs[i].getCC().getNodeId());
		}
		return result;
	}

	/**
	 * @syncpriority 200
	 */
	public void requestCachedSources() {
		if (!receiving)
			return;
		Set<SourceStatus> sources = mina.getSourceMgr().getReadySources(streamId);
		for (SourceStatus sourceStat : sources) {
			// This is a bit kludgy - get the streamstatus that applies to us
			StreamStatus streamStat = null;
			for (StreamStatus testSs : sourceStat.getSsList()) {
				if (testSs.getStreamId().equals(streamId)) {
					streamStat = testSs;
					break;
				}
			}
			if (streamStat == null)
				throw new SeekInnerCalmException();
			foundSource(sourceStat, streamStat);
		}
	}

	public void setPageBuffer(PageBuffer pageBuf) {
		this.pageBuf = pageBuf;
		if (pageBuf.getTotalPages() <= 0)
			pageBuf.setTotalPages(totalPages);
	}

	public void setPRM(PageRequestMgr prm) {
		this.prm = prm;
	}

	/**
	 * @syncpriority 200
	 */
	public synchronized void startBroadcast() {
		if (broadcasting)
			throw new SeekInnerCalmException();
		log.debug("Starting pagebuffer for " + streamId);
		pageBuf.start();
		broadcasting = true;
		log.info("Starting broadcast of " + pageBuf.getTotalPages() + " pages for " + streamId);
		// If we're already rebroadcasting, this means that we've completed
		// reception and are now a broadcaster - so we don't need to announce
		// ourselves again
		if (rebroadcasting)
			rebroadcasting = false;
		else
			mina.getStreamAdvertiser().advertiseStream(streamId);
	}

	/**
	 * @syncpriority 200
	 */
	public synchronized void startRebroadcast() {
		if (rebroadcasting)
			throw new SeekInnerCalmException();
		rebroadcasting = true;
		log.info("Beginning rebroadcast for stream " + streamId);
		mina.getStreamAdvertiser().advertiseStream(streamId);
	}

	/**
	 * @syncpriority 200
	 */
	public synchronized void startReception() {
		if (receiving)
			throw new SeekInnerCalmException();
		// If we're already finished, just start rebroadcasting
		if (pageBuf.isComplete())
			startRebroadcast();
		else {
			receiving = true;
			requestCachedSources();
			mina.getSourceMgr().wantSources(streamId, tolerateDelay());
		}
	}

	/**
	 * @syncpriority 200
	 */
	public synchronized void stop() {
		log.debug("SM for " + streamId + " stopping");
		if (broadcasting)
			stopBroadcast();
		if (rebroadcasting)
			stopRebroadcast();
		if (receiving)
			stopReception();
		listeners.clear();
		checkWantingSources();
		streamConns.closeAll();
		mina.getSmRegister().unregisterSM(streamId);
		if (pageBuf != null) {
			pageBuf.stop();
			pageBuf.close();
		}
	}

	/**
	 * @syncpriority 200
	 */
	public synchronized void stopBroadcast() {
		if (!broadcasting)
			throw new SeekInnerCalmException();
		broadcasting = false;
		deadvertiseStream();
		streamConns.closeAllBroadcastConns();
	}

	/**
	 * @syncpriority 200
	 */
	public synchronized void stopRebroadcast() {
		if (!rebroadcasting)
			throw new SeekInnerCalmException();
		rebroadcasting = false;
		log.info("Ceasing rebroadcast for stream " + streamId);
		// If we are broadcasting, it means we completed our reception and
		// became a broadcaster - so keep our broadcast conns alive, and don't
		// de-advertise ourselves
		if (!broadcasting) {
			streamConns.closeAllBroadcastConns();
			deadvertiseStream();
		}
	}

	private void deadvertiseStream() {
		// Don't deadvertise if we're shutting down
		if (!mina.getCCM().isShuttingDown()) {
			List<String> streamIds = new ArrayList<String>();
			streamIds.add(streamId);
			UnAdvSource uas = UnAdvSource.newBuilder().addAllStreamId(streamIds).build();
			mina.getCCM().sendMessageToNetwork("UnAdvSource", uas);
		}
	}

	/**
	 * @syncpriority 200
	 */
	public synchronized void stopReception() {
		log.info("Stopping reception for stream " + streamId);
		if (!receiving) // Leftover thread
			return;
		receiving = false;
		// If we're still wanting to hear about sources, keep track of the ones we're receiving from
		if(checkWantingSources()) {
			for (LCPair lcp : streamConns.getAllListenConns()) {
				mina.getSourceMgr().cacheSourceUntilReady(lcp.getLastSourceStatus(), lcp.getLastStreamStatus());
			}
		}
		streamConns.closeAllListenConns();
	}

	private void receptionCompleted() {
		// Huzzah
		log.info("Completed reception of " + getStreamId());
		// If we want to become a broadcaster when reception is completed
		// (probable), then there needs to be a receptionlistener that calls
		// startbroadcast in response to this event
		mina.getEventMgr().fireReceptionCompleted(getStreamId());
		stopReception();
	}

	public int getPriority() {
		return priority;
	}

	public void setPriority(int priority) {
		this.priority = priority;
	}

	public BidStrategy getBidStrategy() {
		return bidStrategy;
	}

	public void setStreamVelocity(StreamVelocity sv) {
		bidStrategy.setStreamVelocity(sv);
	}

	public StreamVelocity getStreamVelocity() {
		return bidStrategy.getStreamVelocity();
	}
}
