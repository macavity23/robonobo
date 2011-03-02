package com.robonobo.mina.message.handlers;

import java.io.IOException;
import java.io.InputStream;

import com.google.protobuf.GeneratedMessage;
import com.robonobo.mina.message.MessageHolder;

public class EscrowFinishedHandler extends AbstractMessageHandler {

	@Override
	public void handleMessage(MessageHolder mh) {
	}

	@Override
	public GeneratedMessage parse(String cmdName, InputStream is) throws IOException {
		return null;
	}

}
