package com.robonobo.mina.message.handlers;

import java.io.IOException;
import java.io.InputStream;

import com.robonobo.mina.message.MessageHolder;
import com.robonobo.mina.message.proto.MinaProtocol.NoBid;

public class NoBidHandler extends AbstractMessageHandler {

	@Override
	public void handleMessage(MessageHolder mh) {
		mina.getSellMgr().noBid(mh.getFromCC().getNodeId());
	}

	@Override
	public NoBid parse(String cmdName, InputStream is) throws IOException {
		// Even though NoBid has no properties, read the message from the stream or it'll be left in a screwy state
		return NoBid.newBuilder().mergeFrom(is).build();
	}

}
