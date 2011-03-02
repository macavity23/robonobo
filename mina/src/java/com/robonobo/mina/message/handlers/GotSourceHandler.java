package com.robonobo.mina.message.handlers;

import java.io.IOException;
import java.io.InputStream;

import com.robonobo.core.api.proto.CoreApi.Node;
import com.robonobo.mina.message.MessageHolder;
import com.robonobo.mina.message.proto.MinaProtocol.GotSource;

public class GotSourceHandler extends AbstractMessageHandler {

	@Override
	public void handleMessage(MessageHolder mh) {
		GotSource gs = (GotSource) mh.getMessage();
		for(Node node : gs.getNodeList()) {
			mina.getSourceMgr().gotSource(gs.getStreamId(), node);
		}
	}

	@Override
	public GotSource parse(String cmdName, InputStream is) throws IOException {
		return GotSource.newBuilder().mergeFrom(is).build();
	}
}
