package com.robonobo.mina.message.handlers;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import com.robonobo.core.api.proto.CoreApi.Node;
import com.robonobo.mina.message.MessageHolder;
import com.robonobo.mina.message.proto.MinaProtocol.ReqSourceStatus;
import com.robonobo.mina.message.proto.MinaProtocol.SourceStatus;
import com.robonobo.mina.message.proto.MinaProtocol.StreamStatus;
import com.robonobo.mina.network.ControlConnection;
import com.robonobo.mina.stream.StreamMgr;

public class ReqSourceStatusHandler extends AbstractMessageHandler {

	@Override
	public void handleMessage(MessageHolder mh) {
		ReqSourceStatus rss = (ReqSourceStatus) mh.getMessage();
		if (rss.getToNodeId().equals(mina.getMyNodeId()) || rss.getToNodeId().length() == 0) {
			// This is for me
			if (!mina.getConfig().isAgoric())
				return;
			// If there is no node set in the message, this is from the other end of this CC
			Node reqNode = rss.hasFromNode() ? rss.getFromNode() : mh.getFromCC().getNodeDescriptor();
			
			// If there were any stream ids attached, send
			// corresponding StreamStatus msgs
			List<StreamStatus> streamStatList = new ArrayList<StreamStatus>();
			for(String streamId : rss.getStreamIdList()) {
				StreamMgr sm = mina.getSmRegister().getSM(streamId);
				if (sm == null || (!sm.isBroadcasting() && !sm.isRebroadcasting())) {
					log.error(reqNode.getId() + " sent me ReqStreamStatus for " + streamId + ", but I am not broadcasting that stream");
					continue;
				}
				streamStatList.add(sm.buildStreamStatus(null));
			}
			ControlConnection ccToNode = mina.getCCM().getCCWithId(reqNode.getId());
			SourceStatus ss = mina.getSellMgr().buildSourceStatus(reqNode, streamStatList);
			if (ccToNode != null)
				ccToNode.sendMessage("SourceStatus", ss);
			else
				mina.getCCM().sendMessageToSupernodes("SourceStatus", ss);
		} else if (mina.getConfig().isSupernode()) {
			// I am a supernode - forward this to its destination
			ControlConnection cc = mina.getCCM().getCCWithId(rss.getToNodeId());
			if (cc != null)
				cc.sendMessage("ReqSourceStatus", rss);
		}

	}

	@Override
	public ReqSourceStatus parse(String cmdName, InputStream is) throws IOException {
		return ReqSourceStatus.newBuilder().mergeFrom(is).build();
	}

}
