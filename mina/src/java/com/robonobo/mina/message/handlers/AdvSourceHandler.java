package com.robonobo.mina.message.handlers;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.robonobo.common.util.TimeUtil;
import com.robonobo.core.api.proto.CoreApi.Node;
import com.robonobo.mina.message.MessageHolder;
import com.robonobo.mina.message.proto.MinaProtocol.AdvSource;
import com.robonobo.mina.message.proto.MinaProtocol.GotSource;
import com.robonobo.mina.network.ControlConnection;

public class AdvSourceHandler extends AbstractMessageHandler {

	@Override
	public void handleMessage(MessageHolder mh) {
		AdvSource as = (AdvSource) mh.getMessage();
		ControlConnection cc = mh.getFromCC();
		if (cc.isLocal()) {
			List<Node> sources = new ArrayList<Node>();
			sources.add(cc.getNodeDescriptor());
			for (String streamId : as.getStreamIdList()) {
				GotSource gs = GotSource.newBuilder().setStreamId(streamId).addAllNode(sources).build();
				MessageHolder gsMh = new MessageHolder("GotSource", gs, cc, TimeUtil.now());
				mina.getMessageMgr().getHandler("GotSource").handleMessage(gsMh);
			}
		}

		if (mina.getConfig().isSupernode()) {
			// Don't pass on the local attr to searchers
			Node sourceNode = Node.newBuilder(cc.getNodeDescriptor()).setLocal(false).build();
			Map<String, List<Node>> searcherMap = mina.getSupernodeMgr().notifyAdvSource(mh);
			for (String streamId : as.getStreamIdList()) {
				List<Node> searchers = searcherMap.get(streamId);
				if (searchers.size() > 0) {
					GotSource gs = GotSource.newBuilder().setStreamId(streamId).addNode(sourceNode).build();
					for (Node node : searchers) {
						// Don't send advert back to sender
						if (!node.getId().equals(cc.getNodeId())) {
							ControlConnection sendCC = mina.getCCM().getCCWithId(node.getId());
							if (sendCC != null)
								sendCC.sendMessage("GotSource", gs);
						}
					}
				}
			}
		}
	}

	@Override
	public AdvSource parse(String cmdName, InputStream is) throws IOException {
		return AdvSource.newBuilder().mergeFrom(is).build();
	}

}
