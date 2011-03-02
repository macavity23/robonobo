package com.robonobo.mina.message.handlers;

import java.io.IOException;
import java.io.InputStream;

import com.robonobo.mina.agoric.AuctionState;
import com.robonobo.mina.message.MessageHolder;
import com.robonobo.mina.message.proto.MinaProtocol.AuctionResult;
import com.robonobo.mina.network.ControlConnection;
import com.robonobo.mina.network.LCPair;

public class AuctionResultHandler extends AbstractMessageHandler {

	@Override
	public void handleMessage(MessageHolder mh) {
		AuctionResult ar = (AuctionResult) mh.getMessage();
		AuctionState as = new AuctionState(ar.getAuctionState());
		ControlConnection cc = mh.getFromCC();
		mina.getBuyMgr().newAuctionState(cc.getNodeId(), as);
		for (LCPair lcp : cc.getLCPairs()) {
			lcp.getSM().getBidStrategy().newAuctionState(cc.getNodeId(), as);
		}
	}

	@Override
	public AuctionResult parse(String cmdName, InputStream is) throws IOException {
		return AuctionResult.newBuilder().mergeFrom(is).build();
	}

}
