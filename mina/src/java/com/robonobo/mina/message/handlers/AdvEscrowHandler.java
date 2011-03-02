package com.robonobo.mina.message.handlers;

import java.io.IOException;
import java.io.InputStream;

import com.robonobo.mina.message.MessageHolder;
import com.robonobo.mina.message.proto.MinaProtocol.AdvEscrow;

public class AdvEscrowHandler extends AbstractMessageHandler {

	@Override
	public void handleMessage(MessageHolder mh) {
		AdvEscrow ae = (AdvEscrow) mh.getMessage();
		if(mina.getEscrowMgr() != null)
			mina.getEscrowMgr().gotAdvEscrow(mh.getFromCC().getNodeId(), ae);
	}

	@Override
	public AdvEscrow parse(String cmdName, InputStream is) throws IOException {
		return AdvEscrow.newBuilder().mergeFrom(is).build();
	}

}
