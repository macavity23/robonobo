package com.robonobo.mina.message.handlers;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import com.robonobo.core.api.proto.CoreApi.Node;
import com.robonobo.mina.message.MessageHolder;
import com.robonobo.mina.message.proto.MinaProtocol.GotSource;
import com.robonobo.mina.message.proto.MinaProtocol.WantSource;
import com.robonobo.mina.network.ControlConnection;
import com.robonobo.mina.stream.StreamMgr;

public class WantSourceHandler extends AbstractMessageHandler {

	@Override
	public void handleMessage(MessageHolder mh) {
		WantSource ws = (WantSource) mh.getMessage();
		ControlConnection cc = mh.getFromCC();

		if (cc.isLocal()) {
			for (String streamId : ws.getStreamIdList()) {
				GotSource.Builder gsb = GotSource.newBuilder().addNode(mina.getNetMgr().getDescriptorForTalkingTo(cc.getNodeDescriptor(), true));
				StreamMgr sm = mina.getSmRegister().getSM(streamId);
				if (sm != null)
					cc.sendMessage("GotSource", gsb.setStreamId(streamId).build());
			}
		}

		if (mina.getConfig().isSupernode()) {
			Map<String, List<Node>> sourceMap = mina.getSupernodeMgr().notifyWantSource(mh);
			for (String streamId : ws.getStreamIdList()) {
				List<Node> sources = sourceMap.get(streamId);
				GotSource.Builder gsb = GotSource.newBuilder().setStreamId(streamId).addAllNode(sources);
				// Add me!
				StreamMgr sm = mina.getSmRegister().getSM(streamId);
				if (sm != null && (sm.isBroadcasting() || sm.isRebroadcasting()))
					gsb.addNode(mina.getNetMgr().getDescriptorForTalkingTo(mh.getFromCC().getNodeDescriptor(), mh.getFromCC().isLocal()));
				if (gsb.getNodeCount() > 0)
					cc.sendMessage("GotSource", gsb.build());
			}
		}
	}

	@Override
	public WantSource parse(String cmdName, InputStream is) throws IOException {
		return WantSource.newBuilder().mergeFrom(is).build();
	}

}
