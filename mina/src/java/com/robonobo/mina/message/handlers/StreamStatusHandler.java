package com.robonobo.mina.message.handlers;

import java.io.IOException;
import java.io.InputStream;

import com.robonobo.mina.message.MessageHolder;
import com.robonobo.mina.message.proto.MinaProtocol.StreamStatus;
import com.robonobo.mina.network.LCPair;

public class StreamStatusHandler extends AbstractMessageHandler {

	@Override
	public void handleMessage(MessageHolder mh) {
		StreamStatus ss = (StreamStatus) mh.getMessage();
		for (LCPair lcp : mh.getFromCC().getLCPairs()) {
			if(lcp.getSM().getStreamId().equals(ss.getStreamId()))
				lcp.notifyStreamStatus(ss);
		}
	}

	@Override
	public StreamStatus parse(String cmdName, InputStream is) throws IOException {
		return StreamStatus.newBuilder().mergeFrom(is).build();
	}

}
