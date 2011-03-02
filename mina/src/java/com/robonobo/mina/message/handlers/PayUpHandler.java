package com.robonobo.mina.message.handlers;

import java.io.IOException;
import java.io.InputStream;

import com.robonobo.mina.message.MessageHolder;
import com.robonobo.mina.message.proto.MinaProtocol.PayUp;

public class PayUpHandler extends AbstractMessageHandler {

	@Override
	public void handleMessage(MessageHolder mh) {
		PayUp pu = (PayUp) mh.getMessage();
		mina.getBuyMgr().gotPayUpDemand(mh.getFromCC().getNodeId(), pu.getBalance());
	}

	@Override
	public PayUp parse(String cmdName, InputStream is) throws IOException {
		return PayUp.newBuilder().mergeFrom(is).build();
	}

}
