package com.robonobo.mina.message.handlers;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;

import com.google.protobuf.GeneratedMessage;
import com.robonobo.core.api.proto.CoreApi.Node;
import com.robonobo.mina.message.MessageHolder;
import com.robonobo.mina.message.proto.MinaProtocol.GotSource;
import com.robonobo.mina.message.proto.MinaProtocol.MyDetailsChanged;
import com.robonobo.mina.network.ControlConnection;

public class MyDetailsChangedHandler extends AbstractMessageHandler {

	@Override
	public void handleMessage(MessageHolder mh) {
		MyDetailsChanged mdc = (MyDetailsChanged) mh.getMessage();
		Node n = mdc.getNode();
		if(!mh.getFromCC().updateDetails(n))
			return;
		if(mina.getConfig().isSupernode()) {
			Map<String, List<Node>> seekersToUpdate = mina.getSupernodeMgr().notifyDetailsChanged(n);
			for (String streamId : seekersToUpdate.keySet()) {
				GotSource gs = GotSource.newBuilder().setStreamId(streamId).addNode(n).build();
				List<Node> seekers = seekersToUpdate.get(streamId);
				for (Node sn : seekers) {
					ControlConnection cc = mina.getCCM().getCCWithId(sn.getId());
					if(cc != null)
						cc.sendMessage("GotSource", gs);
				}
			}
		}
	}

	@Override
	public GeneratedMessage parse(String cmdName, InputStream is) throws IOException {
		return MyDetailsChanged.newBuilder().mergeFrom(is).build();
	}

}
