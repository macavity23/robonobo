package com.robonobo.mina.stream.bidstrategy;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;

import com.robonobo.common.concurrent.CatchingRunnable;
import com.robonobo.core.api.StreamVelocity;
import com.robonobo.mina.agoric.AuctionState;
import com.robonobo.mina.instance.MinaInstance;
import com.robonobo.mina.message.proto.MinaProtocol.BidUpdate;
import com.robonobo.mina.stream.StreamMgr;

public abstract class BidStrategy {
	protected MinaInstance mina;
	protected StreamMgr sm;
	private ScheduledFuture<?> checkTask;
	protected StreamVelocity streamVelocity;
	protected Log log;

	public BidStrategy() {
	}

	public void start() {
	}

	public void stop() {
	}

	public void setStreamMgr(StreamMgr sm) {
		this.sm = sm;
		mina = sm.getMinaInstance();
		log = mina.getLogger(getClass());
	}

	public void setStreamVelocity(StreamVelocity streamVelocity) {
		boolean changed = !(streamVelocity.equals(this.streamVelocity));
		this.streamVelocity = streamVelocity;
	}
	
	public StreamVelocity getStreamVelocity() {
		return streamVelocity;
	}
	
	/**
	 * Is it worth connecting to this guy? 
	 */
	public abstract boolean worthConnectingTo(AuctionState as);
	
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
