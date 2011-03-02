package com.robonobo.mina.message.handlers;

import java.io.IOException;
import java.io.InputStream;

import com.robonobo.common.util.TextUtil;
import com.robonobo.core.api.StreamVelocity;
import com.robonobo.mina.message.MessageHolder;
import com.robonobo.mina.message.proto.MinaProtocol.Agorics;
import com.robonobo.mina.message.proto.MinaProtocol.AuctionStateMsg;
import com.robonobo.mina.message.proto.MinaProtocol.ReceivedBid;
import com.robonobo.mina.message.proto.MinaProtocol.SourceStatus;
import com.robonobo.mina.network.ControlConnection;
import com.robonobo.mina.network.LCPair;

public class SourceStatusHandler extends AbstractMessageHandler {

	@Override
	public void handleMessage(MessageHolder mh) {
		SourceStatus ss = (SourceStatus) mh.getMessage();
		ControlConnection cc = mh.getFromCC();
		if(!checkMsgOk(ss.getAuctionState())) {
			log.error("Invalid sourcestatus msg from "+cc.getNodeId()+": exiting");
			cc.close("You sent an invalid message");
			return;
		}
		String fromNodeId = ss.getFromNode().getId();
		Agorics agorics = ss.getAgorics();
		if (ss.getToNodeId().equals(mina.getMyNodeId())) {
			// This is for me
			if (agorics != null) {
				// Check agorics
				if (!mina.getConfig().isAgoric()) {
					log.debug("Not handling sourcestatus from " + fromNodeId + ": ss is agoric, and I am not");
					return;
				}
				if (!agorics.getCurrencyUrl().equals(mina.getMyAgorics().getCurrencyUrl())) {
					log.debug("Not handling sourcestatus from " + fromNodeId + ": using different currencies");
					return;
				}
				if (mina.getBuyMgr().getBestPaymentMethod(agorics) == null) {
					log.debug("Not handling sourcestatus from " + fromNodeId
							+ ": no mutually-acceptable payment methods");
					return;
				}
				if (agorics.getMinTopRate() > mina.getMyAgorics().getMinTopRate()) {
					log.debug("Not handling sourcestatus from " + fromNodeId + ": unacceptable minimum top rate");
					return;
				}
				if (agorics.getMinBid() > mina.getCurrencyClient().getMaxBid(StreamVelocity.MaxRate)) {
					log.debug("Not handling sourcestatus from " + fromNodeId + ": unacceptable min bid");
					return;
				}
			}
			// Pass this to all lcpairs and our sourceMgr
			for (LCPair lcp : cc.getLCPairs()) {
				lcp.notifySourceStatus(ss);
			}
			mina.getSourceMgr().gotSourceStatus(ss);
		} else if (mina.getConfig().isSupernode()) {
			// I am a supernode - forward this to its destination
			ControlConnection toCC = mina.getCCM().getCCWithId(ss.getToNodeId());
			if (toCC != null)
				toCC.sendMessage("SourceStatus", ss);
		}
	}

	@Override
	public SourceStatus parse(String cmdName, InputStream is) throws IOException {
		return SourceStatus.newBuilder().mergeFrom(is).build();
	}
	
	/**
	 * Catch any bugs here and don't pass them on, rather than have the supernode pass on a dodgy msg and have its connection killed
	 */
	private boolean checkMsgOk(AuctionStateMsg asm) {
		String youAre = asm.getYouAre();
		if(TextUtil.isEmpty(youAre))
			return true;
		for (ReceivedBid bid : asm.getBidList()) {
			if(bid.getListenerId().equals(youAre))
				return true;
		}
		return false;
	}
}
