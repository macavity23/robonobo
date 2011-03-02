package com.robonobo.mina.message.handlers;

import static com.robonobo.common.util.NumberUtil.*;

import java.io.IOException;
import java.io.InputStream;

import com.robonobo.mina.agoric.SellMgr;
import com.robonobo.mina.message.MessageHolder;
import com.robonobo.mina.message.proto.MinaProtocol.Bid;
import com.robonobo.mina.network.ControlConnection;

public class BidHandler extends AbstractMessageHandler {

	@Override
	public void handleMessage(MessageHolder mh) {
		Bid b = (Bid) mh.getMessage();
		ControlConnection cc = mh.getFromCC();
		SellMgr sellMgr = mina.getSellMgr();
		if(sellMgr.haveActiveAccount(cc.getNodeId())) {
			if(dblEq(b.getAmount(), 0d)) 
				sellMgr.removeBidder(cc.getNodeId());
			else
				sellMgr.bid(cc.getNodeId(), b.getAmount());
		} else
			sellMgr.msgPendingActiveAccount(mh);
	}

	@Override
	public Bid parse(String cmdName, InputStream is) throws IOException {
		return Bid.newBuilder().mergeFrom(is).build();
	}

}
