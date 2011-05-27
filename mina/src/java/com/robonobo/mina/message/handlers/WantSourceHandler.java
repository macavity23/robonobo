package com.robonobo.mina.message.handlers;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import com.robonobo.core.api.proto.CoreApi.Node;
import com.robonobo.mina.instance.StreamMgr;
import com.robonobo.mina.message.MessageHolder;
import com.robonobo.mina.message.proto.MinaProtocol.GotSource;
import com.robonobo.mina.message.proto.MinaProtocol.WantSource;
import com.robonobo.mina.network.ControlConnection;

public class WantSourceHandler extends AbstractMessageHandler {

	@Override
	public void handleMessage(MessageHolder mh) {
		WantSource ws = (WantSource) mh.getMessage();
		ControlConnection cc = mh.getFromCC();

		if (cc.isLocal()) {
			for (String sid : ws.getStreamIdList()) {
				if (mina.getStreamMgr().isBroadcasting(sid) || mina.getStreamMgr().isRebroadcasting(sid)) {
					GotSource.Builder gsb = GotSource.newBuilder().addNode(
							mina.getNetMgr().getDescriptorForTalkingTo(cc.getNode(), true));
					cc.sendMessage("GotSource", gsb.setStreamId(sid).build());
				}
			}
		}

		if (mina.getConfig().isSupernode()) {
			Map<String, List<Node>> sourceMap = mina.getSupernodeMgr().notifyWantSource(mh);
			for (String sid : ws.getStreamIdList()) {
				List<Node> sources = sourceMap.get(sid);
				GotSource.Builder gsb = GotSource.newBuilder().setStreamId(sid).addAllNode(sources);
				if (mina.getStreamMgr().isBroadcasting(sid) || mina.getStreamMgr().isRebroadcasting(sid))
					gsb.addNode(mina.getNetMgr().getDescriptorForTalkingTo(mh.getFromCC().getNode(),
							mh.getFromCC().isLocal()));
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
