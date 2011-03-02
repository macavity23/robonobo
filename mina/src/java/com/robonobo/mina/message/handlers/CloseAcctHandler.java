package com.robonobo.mina.message.handlers;

import java.io.IOException;
import java.io.InputStream;

import com.robonobo.mina.message.MessageHolder;
import com.robonobo.mina.message.proto.MinaProtocol.CloseAcct;

public class CloseAcctHandler extends AbstractMessageHandler {

	@Override
	public void handleMessage(MessageHolder mh) {
		if(mina.getConfig().isAgoric())
			mina.getSellMgr().closeAccount(mh.getFromCC().getNodeId());
	}

	@Override
	public CloseAcct parse(String cmdName, InputStream is) throws IOException {
		return CloseAcct.newBuilder().mergeFrom(is).build();
	}

}
