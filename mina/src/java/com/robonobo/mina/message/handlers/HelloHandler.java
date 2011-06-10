package com.robonobo.mina.message.handlers;

import java.io.IOException;
import java.io.InputStream;

import com.robonobo.common.exceptions.Errot;
import com.robonobo.mina.message.MessageHolder;
import com.robonobo.mina.message.proto.MinaProtocol.Hello;

public class HelloHandler extends AbstractMessageHandler {

	@Override
	public void handleMessage(MessageHolder mh) {
		// Can't happen
		throw new Errot();
	}

	@Override
	public Hello parse(String cmdName, InputStream is) throws IOException {
		return Hello.newBuilder().mergeFrom(is).build();
	}

}
