package com.robonobo.mina.message.handlers;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.logging.Log;

import com.robonobo.mina.message.MessageHolder;
import com.robonobo.mina.message.proto.MinaProtocol.StartSource;
import com.robonobo.mina.network.BCPair;
import com.robonobo.mina.network.ControlConnection;

public class StartSourceHandler extends AbstractMessageHandler {

	@Override
	public void handleMessage(MessageHolder mh) {
		StartSource ss = (StartSource) mh.getMessage();
		ControlConnection cc = mh.getFromCC();
		String sid = ss.getStreamId();
		BCPair bcp = cc.getBCPair(sid);
		if(bcp != null) {
			// Errot - best thing is to shut everything down, let them start again
			Log log = mina.getLogger(getClass());
			log.error("Error: asked to start providing stream "+sid+" to "+cc.getNodeId()+", but I am already broadcasting that stream to them");
			bcp.die();
			return;
		}
		if(mina.getConfig().isAgoric() && !mina.getSellMgr().haveActiveAccount(cc.getNodeId())) {
			Log log = mina.getLogger(getClass());
			log.error("Error: asked to start providing stream "+sid+" to "+cc.getNodeId()+", but they do not have an active account");
			cc.close("You asked for a stream without opening an account");
			return;
		}
		mina.getStreamMgr().broadcastTo(sid, cc, ss.getEp(), ss.getPageList());
	}

	@Override
	public StartSource parse(String cmdName, InputStream is) throws IOException {
		return StartSource.newBuilder().mergeFrom(is).build();
	}

}
