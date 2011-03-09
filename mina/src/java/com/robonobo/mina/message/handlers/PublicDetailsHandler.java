package com.robonobo.mina.message.handlers;

import java.io.IOException;
import java.io.InputStream;

import com.google.protobuf.GeneratedMessage;
import com.robonobo.mina.message.MessageHolder;
import com.robonobo.mina.message.proto.MinaProtocol.PublicDetails;
import com.robonobo.mina.network.EndPointMgr;

public class PublicDetailsHandler extends AbstractMessageHandler {
	@Override
	public void handleMessage(MessageHolder mh) {
		PublicDetails pd = (PublicDetails) mh.getMessage();
		// As a result of these newly-returned public details for us, can we now say we support NAT traversal?
		boolean newEndpoint = false;
		for (EndPointMgr epMgr : mina.getNetMgr().getEndPointMgrs()) {
			newEndpoint |= epMgr.advisePublicDetails(pd, mh.getFromCC().getTheirEp());
		}
		if(newEndpoint)
			mina.getNetMgr().readvertiseEndpoints();
	}

	@Override
	public GeneratedMessage parse(String cmdName, InputStream is) throws IOException {
		return PublicDetails.newBuilder().mergeFrom(is).build();
	}

}
