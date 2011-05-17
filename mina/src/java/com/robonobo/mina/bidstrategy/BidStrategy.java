package com.robonobo.mina.bidstrategy;

import java.util.*;
import java.util.concurrent.ScheduledFuture;

import org.apache.commons.logging.Log;

import com.robonobo.core.api.StreamVelocity;
import com.robonobo.mina.agoric.AuctionState;
import com.robonobo.mina.instance.MinaInstance;
import com.robonobo.mina.message.proto.MinaProtocol.BidUpdate;
import com.robonobo.mina.network.LCPair;

public abstract class BidStrategy {
	protected MinaInstance mina;
	private ScheduledFuture<?> checkTask;
	protected Map<String, StreamVelocity> streamVelocities = Collections.synchronizedMap(new HashMap<String, StreamVelocity>());
	protected Log log;

	public BidStrategy() {
	}

	public void start() {
	}

	public void stop() {
	}

	public void setMinaInstance(MinaInstance mina) {
		this.mina = mina;
		log = mina.getLogger(getClass());
	}

	public void setStreamVelocity(String sid, StreamVelocity sv) {
		streamVelocities.put(sid, sv);
	}
	
	public StreamVelocity getStreamVelocity(String sid) {
		return streamVelocities.get(sid);
	}
	
	/** true if we're willing to put up with a ~5s delay on requesting sources */
	public boolean tolerateDelay(String sid) {
		StreamVelocity sv = getStreamVelocity(sid);
		if (sv == null || sv == StreamVelocity.LowestCost)
			return true;
		return false;
	}
	
	/**
	 * Is it worth connecting to this guy? 
	 */
	public abstract boolean worthConnectingTo(String sid, AuctionState as);
	
	public abstract double getOpeningBid(String toNodeId);
	
	/**
	 * @return the amount to bid, or else 0 for no bid
	 */
	public abstract double getAnsweringBid(String sellerNodeId, BidUpdate bu);
	
	/**
	 * Auction has finished
	 */
	public abstract void newAuctionState(String sellerNodeId, AuctionState as);
	
	/**
	 * We are no longer receiving from this guy, so remove any trace of him
	 */
	public abstract void cleanup(String sellerNodeId);
}
