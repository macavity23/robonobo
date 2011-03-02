package com.robonobo.mina.message.handlers;

import java.io.IOException;
import java.io.InputStream;

import com.robonobo.mina.message.MessageHolder;
import com.robonobo.mina.message.proto.MinaProtocol.ReqPage;
import com.robonobo.mina.network.BCPair;
import com.robonobo.mina.network.ControlConnection;

public class ReqPageHandler extends AbstractMessageHandler {

	@Override
	public void handleMessage(MessageHolder mh) {
		ReqPage rp = (ReqPage) mh.getMessage();
		ControlConnection cc = mh.getFromCC();
		BCPair bcp = cc.getBCPair(rp.getStreamId());
		if(bcp == null)
			log.error("Error: received reqpage for unknown stream "+rp.getStreamId()+" from "+cc.getNodeId());
		else
			bcp.requestPages(rp.getRequestedPageList());
	}

	@Override
	public ReqPage parse(String cmdName, InputStream is) throws IOException {
		return ReqPage.newBuilder().mergeFrom(is).build();
	}

}
