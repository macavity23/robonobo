package com.robonobo.mina.message.handlers;

import java.io.IOException;
import java.io.InputStream;

import com.robonobo.mina.message.MessageHolder;
import com.robonobo.mina.message.proto.MinaProtocol.Pong;

public class PongHandler extends AbstractMessageHandler {

	@Override
	public void handleMessage(MessageHolder mh) {
		Pong p = (Pong) mh.getMessage();
		mh.getFromCC().notifyPong(p);
	}

	@Override
	public Pong parse(String cmdName, InputStream is) throws IOException {
		return Pong.newBuilder().mergeFrom(is).build();
	}

}
