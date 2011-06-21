package com.robonobo.mina.agoric;

import static com.robonobo.common.util.TextUtil.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import com.robonobo.common.exceptions.Errot;
import com.robonobo.common.util.Modulo;
import com.robonobo.common.util.TextUtil;
import com.robonobo.mina.message.proto.MinaProtocol.AuctionStateMsg;
import com.robonobo.mina.message.proto.MinaProtocol.ReceivedBid;

/**
 * This class is a wrapper around the AuctionStateMsg protocol buffer bean.  Not thread-safe.
 * 
 * @author macavity
 */
public class AuctionState {
	/** Used to keep track of status numbers, which are modulo 64 */
	public static Modulo INDEX_MOD;
	static {
		INDEX_MOD = new Modulo(64);
	}

	/**
	 * Millisecs. If > 0, time until bids are open. If =0, bids are open.
	 */
	int bidsOpen;
	/** The listener id of the recipient of this status */
	String youAre = null;
	/**
	 * An incrementing value that allows us to track changes in the auction
	 * state, to track costs of pages. Modulo 64.
	 */
	int index;
	/**
	 * The last-sent bid - note, this isn't serialized, it's just used locally
	 * to keep tabs on the auction
	 */
	double lastSentBid;
	/** The time this state was received - this isn't serialized either */
	Date timeReceived;

	List<ReceivedBid> bids = new ArrayList<ReceivedBid>();
	/** This is not serialized, just allows quick retrieval of my bid */
	int myBidIndex = -1;
	/** Max number of listeners with flow rate > 0 that the source can support */
	int maxRunningListeners;
	/** Underlying message */
	AuctionStateMsg asm;

	public AuctionState(AuctionStateMsg asm) {
		this.asm = asm;
		bidsOpen = asm.hasBidsOpen() ? asm.getBidsOpen() : 0;
		youAre = asm.getYouAre();
		index = asm.getIndex();
		maxRunningListeners = asm.getMaxRunningListeners();
		for (ReceivedBid recBid : asm.getBidList()) {
			bids.add(recBid);
		}
		Collections.sort(bids, new ReceivedBidComparator());
	}

	@Override
	public String toString() {
		return "AuctionState["+asm.toString()+"]";
	}
	
	public double getMyBid() {
		if (isEmpty(youAre))
			return 0;
		calcMyBidIndex();
		// Bug huntin
		if(myBidIndex < 0)
			throw new Errot("myBidIndex == -1, asm: "+asm);
		return bids.get(myBidIndex).getBid();
	}

	public double getTopBid() {
		if(bids.size() == 0)
			return 0d;
		return bids.get(bids.size()-1).getBid();
	}
	
	public boolean amITopBid() {
		if(bids.size() == 0)
			return false;
		calcMyBidIndex();
		return (myBidIndex == bids.size() - 1);
	}

	private void calcMyBidIndex() {
		if (myBidIndex < 0) {
			for (int i = 0; i < bids.size(); i++) {
				if (bids.get(i).getListenerId().equals(youAre)) {
					myBidIndex = i;
					return;
				}
			}
		}
	}

	public int getBidsOpen() {
		return bidsOpen;
	}

	public List<ReceivedBid> getBids() {
		return bids;
	}

	public String getYouAre() {
		return youAre;
	}

	public double getLastSentBid() {
		return lastSentBid;
	}

	public Date getTimeReceived() {
		return timeReceived;
	}

	public int getIndex() {
		return index;
	}

	public void setLastSentBid(double lastSentBid) {
		this.lastSentBid = lastSentBid;
	}

	public void setTimeReceived(Date timeReceived) {
		this.timeReceived = timeReceived;
	}

	public void setBids(List<ReceivedBid> bids) {
		this.bids = bids;
	}

	public int getMaxRunningListeners() {
		return maxRunningListeners;
	}
}
