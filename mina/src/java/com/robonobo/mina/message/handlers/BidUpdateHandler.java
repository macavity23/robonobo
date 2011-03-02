package com.robonobo.mina.message.handlers;

import java.io.IOException;
import java.io.InputStream;

import com.robonobo.common.exceptions.SeekInnerCalmException;
import com.robonobo.mina.message.MessageHolder;
import com.robonobo.mina.message.proto.MinaProtocol.Bid;
import com.robonobo.mina.message.proto.MinaProtocol.BidUpdate;
import com.robonobo.mina.message.proto.MinaProtocol.NoBid;
import com.robonobo.mina.network.ControlConnection;
import com.robonobo.mina.network.LCPair;

public class BidUpdateHandler extends AbstractMessageHandler {

	@Override
	public void handleMessage(MessageHolder mh) {
		BidUpdate bu = (BidUpdate) mh.getMessage();
		ControlConnection cc = mh.getFromCC();
		// Notify the streammgr for every stream we are receiving from, get their bid - get the highest one, send it back
		LCPair[] pairs = cc.getLCPairs();
		double answeringBid = 0;
		for (LCPair pair : pairs) {
			double thisBid = pair.getSM().getBidStrategy().getAnsweringBid(cc.getNodeId(), bu);
			if(thisBid > answeringBid)
				answeringBid = thisBid;
		}
		// If we are replying with the same bid, just send a NoBid instead
		if(answeringBid == 0 || answeringBid == myBid(bu))
			cc.sendMessage("NoBid", NoBid.getDefaultInstance());
		else
			cc.sendMessage("Bid", Bid.newBuilder().setAmount(answeringBid).build());
		mina.getBuyMgr().sentBid(cc.getNodeId(), answeringBid);
		// Update our stored bids
		mina.getBuyMgr().bidUpdate(cc.getNodeId(), bu);
	}

	@Override
	public BidUpdate parse(String cmdName, InputStream is) throws IOException {
		return BidUpdate.newBuilder().mergeFrom(is).build();
	}

	private double myBid(BidUpdate bu) {
		if(!bu.hasYouAre())
			return 0d;
		String myListenerId = bu.getYouAre();
		int myIdx = -1;
		for(int i=0;i<bu.getListenerIdCount();i++) {
			if(myListenerId.equals(bu.getListenerId(i))) {
				myIdx = i;
				break;
			}
		}
		if(myIdx < 0)
			throw new SeekInnerCalmException();
		return bu.getBidAmount(myIdx);
	}
}
