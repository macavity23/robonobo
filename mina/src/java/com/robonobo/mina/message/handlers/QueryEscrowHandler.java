package com.robonobo.mina.message.handlers;

import java.io.IOException;
import java.io.InputStream;

import com.robonobo.mina.message.MessageHolder;
import com.robonobo.mina.message.proto.MinaProtocol.AdvEscrow;
import com.robonobo.mina.message.proto.MinaProtocol.QueryEscrow;

public class QueryEscrowHandler extends AbstractMessageHandler {

	@Override
	public void handleMessage(MessageHolder mh) {
		QueryEscrow qe = (QueryEscrow) mh.getMessage();
		if(mina.getEscrowProvider() != null) {
			AdvEscrow.Builder aeb = AdvEscrow.newBuilder();
			aeb.setFee(mina.getEscrowProvider().getEscrowFee());
			aeb.setOpeningBalance(mina.getEscrowProvider().getOpeningBalance());
			mh.getFromCC().sendMessage("AdvEscrow", aeb.build());
		}
	}

	@Override
	public QueryEscrow parse(String cmdName, InputStream is) throws IOException {
		return QueryEscrow.newBuilder().mergeFrom(is).build();
	}

}
