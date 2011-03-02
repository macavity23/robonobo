package com.robonobo.mina.message.handlers;

import java.io.IOException;
import java.io.InputStream;

import com.robonobo.mina.message.MessageHolder;
import com.robonobo.mina.message.proto.MinaProtocol.Bye;
import com.robonobo.mina.network.ControlConnection;

public class ByeHandler extends AbstractMessageHandler {

	@Override
	public void handleMessage(MessageHolder mh) {
		ControlConnection cc = mh.getFromCC();
		Bye b = (Bye) mh.getMessage();
		String logStr = "Node " + cc.getNodeId() + " saying goodbye";
		if(b.getReason() != null) logStr += " ('" + b.getReason() + "')";
		mina.getLogger(getClass()).info(logStr);
		cc.close();
	}

	@Override
	public Bye parse(String cmdName, InputStream is) throws IOException {
		return Bye.newBuilder().mergeFrom(is).build();
	}

}
