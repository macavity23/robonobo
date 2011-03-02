package com.robonobo.mina.message.handlers;

import java.io.IOException;
import java.io.InputStream;

import com.google.protobuf.GeneratedMessage;
import com.robonobo.mina.message.MessageHolder;
import com.robonobo.mina.message.proto.MinaProtocol.SourceStopping;
import com.robonobo.mina.message.proto.MinaProtocol.StopSource;
import com.robonobo.mina.network.BCPair;
import com.robonobo.mina.network.LCPair;

public class SourceStoppingHandler extends AbstractMessageHandler {

	@Override
	public void handleMessage(MessageHolder mh) {
		SourceStopping ss = (SourceStopping) mh.getMessage();
		LCPair lcp = mh.getFromCC().getLCPair(ss.getStreamId());
		if(lcp != null)
			lcp.die(false);
		else
			log.error("Node "+mh.getFromCC().getNodeId()+" sent SourceStopping for stream "+ss.getStreamId()+", but I have no lcp for that stream");
	}

	@Override
	public SourceStopping parse(String cmdName, InputStream is) throws IOException {
		return SourceStopping.newBuilder().mergeFrom(is).build();
	}

}
