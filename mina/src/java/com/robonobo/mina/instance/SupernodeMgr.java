package com.robonobo.mina.instance;

import java.util.*;

import com.robonobo.core.api.proto.CoreApi.Node;
import com.robonobo.mina.message.MessageHolder;
import com.robonobo.mina.message.proto.MinaProtocol.AdvSource;
import com.robonobo.mina.message.proto.MinaProtocol.DontWantSource;
import com.robonobo.mina.message.proto.MinaProtocol.UnAdvSource;
import com.robonobo.mina.message.proto.MinaProtocol.WantSource;
import com.robonobo.mina.util.StreamNodeMap;

// TODO Set up sync priorities in this class
public class SupernodeMgr {
	MinaInstance mina;
	StreamNodeMap sources;
	StreamNodeMap searchers;
	
	public SupernodeMgr(MinaInstance mina) {
		this.mina = mina;
		sources = new StreamNodeMap();
		searchers = new StreamNodeMap();
	}
	
	/**
	 * Returns map<streamid, list<source-nodedesc>>
	 */
	public synchronized Map<String, List<Node>> notifyWantSource(MessageHolder mh) {
		WantSource ws = (WantSource) mh.getMessage();
		Map<String, List<Node>> result = new HashMap<String, List<Node>>(); 
		for (String streamId : ws.getStreamIdList()) {
			searchers.addMapping(streamId, mh.getFromCC().getNode());
			result.put(streamId, sources.getNodes(streamId));
		}
		return result;
	}
	
	public synchronized void notifyDontWantSource(MessageHolder mh) {
		DontWantSource dws = (DontWantSource) mh.getMessage();
		for (String streamId : dws.getStreamIdList()) {
			searchers.removeMapping(streamId, mh.getFromCC().getNode());
		}
	}

	/**
	 * Returns map<streamid, list<searcher-nodedesc>>
	 */
	public synchronized Map<String, List<Node>> notifyAdvSource(MessageHolder mh) {
		AdvSource as = (AdvSource) mh.getMessage();
		Map<String, List<Node>> result = new HashMap<String, List<Node>>();
		for (String streamId : as.getStreamIdList()) {
			// Don't pass on the local attr (if any)
			Node sourceNode = Node.newBuilder().mergeFrom(mh.getFromCC().getNode()).setLocal(false).build();
			sources.addMapping(streamId, sourceNode);
			result.put(streamId, searchers.getNodes(streamId));
		}
		return result;
	}
	
	public synchronized void notifyUnAdvSource(MessageHolder mh) {
		UnAdvSource uas = (UnAdvSource) mh.getMessage();
		for (String streamId : uas.getStreamIdList()) {
			sources.removeMapping(streamId, mh.getFromCC().getNode());
		}
	}
	
	public synchronized void notifyDeadConnection(Node node) {
		sources.removeNode(node);
		searchers.removeNode(node);
	}

	/**
	 * @return a map of <streamid, list<node>>, the nodes who should be told about this source
	 */
	public synchronized Map<String, List<Node>> notifyDetailsChanged(Node node) {
		sources.updateNodeDetails(node);
		searchers.updateNodeDetails(node);
		Map<String, List<Node>> result = new HashMap<String, List<Node>>();
		for (String streamId : sources.getStreams(node.getId())) {
			result.put(streamId, searchers.getNodes(streamId));
		}
		return result;
	}
}
