package com.robonobo.mina.message.handlers;

import java.io.IOException;
import java.io.InputStream;

import com.robonobo.mina.message.MessageHolder;
import com.robonobo.mina.message.proto.MinaProtocol.Ping;
import com.robonobo.mina.message.proto.MinaProtocol.Pong;

public class PingHandler extends AbstractMessageHandler {

	@Override
	public void handleMessage(MessageHolder mh) {
		Ping p = (Ping) mh.getMessage();
		mh.getFromCC().sendMessage("Pong", Pong.newBuilder().setPingId(p.getPingId()).build());
	}

	@Override
	public Ping parse(String cmdName, InputStream is) throws IOException {
		return Ping.newBuilder().mergeFrom(is).build();
	}

}
