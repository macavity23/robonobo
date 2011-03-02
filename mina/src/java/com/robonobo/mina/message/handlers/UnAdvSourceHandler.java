package com.robonobo.mina.message.handlers;

import java.io.IOException;
import java.io.InputStream;

import com.robonobo.mina.message.MessageHolder;
import com.robonobo.mina.message.proto.MinaProtocol.UnAdvSource;
import com.robonobo.mina.network.ControlConnection;

public class UnAdvSourceHandler extends AbstractMessageHandler {

	@Override
	public void handleMessage(MessageHolder mh) {
		UnAdvSource uas = (UnAdvSource) mh.getMessage();
		ControlConnection cc = mh.getFromCC();
		if (mina.getConfig().isSupernode())
			mina.getSupernodeMgr().notifyUnAdvSource(mh);
	}

	@Override
	public UnAdvSource parse(String cmdName, InputStream is) throws IOException {
		return UnAdvSource.newBuilder().mergeFrom(is).build();
	}

}
