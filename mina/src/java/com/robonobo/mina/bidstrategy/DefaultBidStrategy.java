package com.robonobo.mina.bidstrategy;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.robonobo.mina.agoric.AuctionState;
import com.robonobo.mina.agoric.ReceivedBidComparator;
import com.robonobo.mina.message.proto.MinaProtocol.Agorics;
import com.robonobo.mina.message.proto.MinaProtocol.Bid;
import com.robonobo.mina.message.proto.MinaProtocol.BidUpdate;
import com.robonobo.mina.message.proto.MinaProtocol.ReceivedBid;
import com.robonobo.mina.network.ControlConnection;

/**
 * A plain, straightforward and probably highly suboptimal bidding strategy.
 * 
 * TODO RealTime adjustment - measure how fast we are receiving the stream, and
 * adjust bidding appropriately
 * 
 * @author macavity
 * 
 */
public class DefaultBidStrategy extends BidStrategy {
	/**
	 * TCP-LP suggests gamma of 0.15 means there is no conflict between streams,
	 * so no point bidding higher than this
	 */
	static final double MAX_OVERPAY = 100d / 15;
	// We can only reduce our bid as the first bid in any auction
	Map<String, Boolean> canReduceBid = Collections.synchronizedMap(new HashMap<String, Boolean>());

	/**
	 * @syncpriority 170
	 */
	@Override
	public double getOpeningBid(String nodeId) {
		AuctionState as = getAuctionState(nodeId);
		Agorics ag = getAgorics(nodeId);
		List<ReceivedBid> bids = as.getBids();

		// We should never get here if our max is above their min, but check
		// just in case
		if (ag.getMinBid() > maxBid(nodeId))
			return 0;

		// If there are no bidders, just bid the minimum
		if (bids.size() == 0)
			return ag.getMinBid();

		return getPreferredBid(nodeId, bids, ag.getIncrement());
	}

	/**
	 * @syncpriority 170
	 */
	@Override
	public double getAnsweringBid(String nodeId, BidUpdate bu) {
		AuctionState as = getAuctionState(nodeId);
		Agorics ag = getAgorics(nodeId);
		// The bid update is provided separately to the existing
		// auctionstate to allow a more advanced strategy to adjust its
		// decisions. We are dumb, so we just look at the bid update
		
		// If there are no bidders, just bid the minimum
		if (bu.getListenerIdCount() == 0)
			return ag.getMinBid();

		double myNewBid = getPreferredBid(nodeId, cleanBids(bu), ag.getIncrement());
		if (myNewBid == as.getLastSentBid()) {
			// No bid
			return 0;
		}
		if (myNewBid < as.getLastSentBid()) {
			if(canReduceBid.get(nodeId)) {
				canReduceBid.put(nodeId, false);
				return myNewBid;
			}
			return 0;
		}
		return myNewBid;
	}

	@Override
	public void newAuctionState(String nodeId, AuctionState as) {
		canReduceBid.put(nodeId, true);
	}

	@Override
	public void cleanup(String sellerNodeId) {
		canReduceBid.remove(sellerNodeId);
	}
	
	@Override
	public boolean worthConnectingTo(String sid, AuctionState as) {
		// TODO When we are measuring stream reception speed, we might not
		// want/need to connect to new sources
		return true;
	}

	private double getPreferredBid(String nodeId, List<ReceivedBid> bids, double minIncrement) {
		// We want to be MAX_OVERPAY times the next highest bidder, or failing
		// that as high as we're willing to go
		double maxBid = maxBid(nodeId);
		double highestCurBid = bids.get(bids.size() - 1).getBid();
		double overpayBid = highestCurBid * MAX_OVERPAY;
		if (maxBid < overpayBid)
			return maxBid;
		return overpayBid;
	}

	private double maxBid(String nodeId) {
		ControlConnection cc = mina.getCCM().getCCWithId(nodeId);
		if(cc == null)
			return 0;
		return mina.getCurrencyClient().getMaxBid(cc.highestVelocity());
	}

	/**
	 * @syncpriority 170
	 */
	private AuctionState getAuctionState(String nodeId) {
		return mina.getBuyMgr().getAuctionState(nodeId);
	}

	/**
	 * @syncpriority 170
	 */
	private Agorics getAgorics(String nodeId) {
		return mina.getBuyMgr().getAgorics(nodeId);
	}

	private List<ReceivedBid> cleanBids(BidUpdate bu) {
		List<ReceivedBid> result = new ArrayList<ReceivedBid>();
		for(int i=0;i<bu.getListenerIdCount();i++) {
			ReceivedBid.Builder bb = ReceivedBid.newBuilder();
			String lId = bu.getListenerId(i);
			double bidAmt = bu.getBidAmount(i);
			// Don't include own bid
			if (lId.equals(bu.getYouAre()))
				continue;
			bb.setBid(bidAmt);
			bb.setListenerId(lId);
			result.add(bb.build());
		}
		Collections.sort(result, new ReceivedBidComparator());
		return result;
	}
}
