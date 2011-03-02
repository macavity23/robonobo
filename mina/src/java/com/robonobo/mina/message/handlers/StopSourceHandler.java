package com.robonobo.mina.message.handlers;

import java.io.IOException;
import java.io.InputStream;

import com.robonobo.mina.message.MessageHolder;
import com.robonobo.mina.message.proto.MinaProtocol.StopSource;
import com.robonobo.mina.network.BCPair;
import com.robonobo.mina.network.LCPair;

public class StopSourceHandler extends AbstractMessageHandler {

	@Override
	public void handleMessage(MessageHolder mh) {
		StopSource ss = (StopSource) mh.getMessage();
		BCPair bcp = mh.getFromCC().getBCPair(ss.getStreamId());
		if(bcp != null)
			bcp.die(false);
		else
			log.error("Node "+mh.getFromCC().getNodeId()+" sent StopSource for stream "+ss.getStreamId()+", but I have no bcp for that stream");
	}

	@Override
	public StopSource parse(String cmdName, InputStream is) throws IOException {
		return StopSource.newBuilder().mergeFrom(is).build();
	}

}
