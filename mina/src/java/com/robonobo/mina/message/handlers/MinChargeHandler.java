package com.robonobo.mina.message.handlers;

import java.io.IOException;
import java.io.InputStream;

import com.robonobo.mina.message.MessageHolder;
import com.robonobo.mina.message.proto.MinaProtocol.MinCharge;

public class MinChargeHandler extends AbstractMessageHandler {

	@Override
	public void handleMessage(MessageHolder mh) {
		MinCharge mc = (MinCharge) mh.getMessage();
		if(mina.getConfig().isAgoric())
			mina.getBuyMgr().minCharge(mh.getFromCC().getNodeId(), mc.getAmount());
	}

	@Override
	public MinCharge parse(String cmdName, InputStream is) throws IOException {
		return MinCharge.newBuilder().mergeFrom(is).build();
	}

}
