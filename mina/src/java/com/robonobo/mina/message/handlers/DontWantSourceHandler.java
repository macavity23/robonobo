package com.robonobo.mina.message.handlers;

import java.io.IOException;
import java.io.InputStream;

import com.robonobo.mina.message.MessageHolder;
import com.robonobo.mina.message.proto.MinaProtocol.DontWantSource;
import com.robonobo.mina.network.ControlConnection;

public class DontWantSourceHandler extends AbstractMessageHandler {

	@Override
	public void handleMessage(MessageHolder mh) {
		ControlConnection cc = mh.getFromCC();
		DontWantSource dws = (DontWantSource) mh.getMessage();
		if(mina.getConfig().isSupernode())
			mina.getSupernodeMgr().notifyDontWantSource(mh);
	}

	@Override
	public DontWantSource parse(String cmdName, InputStream is) throws IOException {
		return DontWantSource.newBuilder().mergeFrom(is).build();
	}

}
