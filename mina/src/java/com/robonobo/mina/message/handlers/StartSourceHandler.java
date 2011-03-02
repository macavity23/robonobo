package com.robonobo.mina.message.handlers;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.logging.Log;

import com.robonobo.mina.message.MessageHolder;
import com.robonobo.mina.message.proto.MinaProtocol.StartSource;
import com.robonobo.mina.network.BCPair;
import com.robonobo.mina.network.ControlConnection;
import com.robonobo.mina.stream.StreamMgr;

public class StartSourceHandler extends AbstractMessageHandler {

	@Override
	public void handleMessage(MessageHolder mh) {
		StartSource ss = (StartSource) mh.getMessage();
		ControlConnection cc = mh.getFromCC();
		String streamId = ss.getStreamId();
		BCPair bcp = cc.getBCPair(streamId);
		if(bcp != null) {
			// Errot - best thing is to shut everything down, let them start again
			Log log = mina.getLogger(getClass());
			log.error("Error: asked to start providing stream "+streamId+" to "+cc.getNodeId()+", but I am already broadcasting that stream to them");
			bcp.die();
			return;
		}
		StreamMgr sm = mina.getSmRegister().getSM(streamId);
		if(sm != null) {
			if(mina.getConfig().isAgoric() && !mina.getSellMgr().haveActiveAccount(cc.getNodeId())) {
				Log log = mina.getLogger(getClass());
				log.error("Error: asked to start providing stream "+streamId+" to "+cc.getNodeId()+", but they do not have an active account");
				cc.close("You asked for a stream without opening an account");
				return;
			}
			sm.getStreamConns().makeBroadcastConnectionTo(cc, ss.getEp(), ss.getPageList());
		}
	}

	@Override
	public StartSource parse(String cmdName, InputStream is) throws IOException {
		return StartSource.newBuilder().mergeFrom(is).build();
	}

}
