package com.robonobo.mina.message.handlers;

import java.io.IOException;
import java.io.InputStream;

import com.google.protobuf.GeneratedMessage;
import com.robonobo.mina.instance.MinaInstance;
import com.robonobo.mina.message.MessageHolder;
import com.robonobo.mina.message.proto.MinaProtocol.PublicDetails;
import com.robonobo.mina.message.proto.MinaProtocol.ReqPublicDetails;
import com.robonobo.mina.network.ControlConnection;

public class ReqPublicDetailsHandler extends AbstractMessageHandler {
	@Override
	public void handleMessage(MessageHolder mh) {
		PublicDetails.Builder b = PublicDetails.newBuilder();
		ControlConnection cc = mh.getFromCC();
		b.addUrl(cc.getTheirEp().getUrl());
		cc.sendMessage("PublicDetails", b.build());
	}

	@Override
	public GeneratedMessage parse(String cmdName, InputStream is) throws IOException {
		return ReqPublicDetails.newBuilder().mergeFrom(is).build();
	}
}
