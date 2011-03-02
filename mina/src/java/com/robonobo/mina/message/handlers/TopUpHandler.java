package com.robonobo.mina.message.handlers;

import java.io.IOException;
import java.io.InputStream;

import com.robonobo.mina.message.MessageHolder;
import com.robonobo.mina.message.proto.MinaProtocol.TopUp;

public class TopUpHandler extends AbstractMessageHandler {

	@Override
	public void handleMessage(MessageHolder mh) {
		TopUp tu = (TopUp) mh.getMessage();
		mina.getSellMgr().topUpAccount(mh.getFromCC().getNodeId(), tu.getCurrencyToken().toByteArray());
	}

	@Override
	public TopUp parse(String cmdName, InputStream is) throws IOException {
		return TopUp.newBuilder().mergeFrom(is).build();
	}

}
