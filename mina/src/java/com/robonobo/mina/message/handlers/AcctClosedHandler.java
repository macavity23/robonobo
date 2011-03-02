package com.robonobo.mina.message.handlers;

import java.io.IOException;
import java.io.InputStream;

import com.robonobo.mina.message.MessageHolder;
import com.robonobo.mina.message.proto.MinaProtocol.AcctClosed;

public class AcctClosedHandler extends AbstractMessageHandler {

	@Override
	public void handleMessage(MessageHolder mh) {
		AcctClosed ac = (AcctClosed) mh.getMessage();
		if(mina.getConfig().isAgoric())
			mina.getBuyMgr().accountClosed(mh.getFromCC().getNodeId(), ac.getCurrencyToken().toByteArray());
	}

	@Override
	public AcctClosed parse(String cmdName, InputStream is) throws IOException {
		return AcctClosed.newBuilder().mergeFrom(is).build();
	}

}
