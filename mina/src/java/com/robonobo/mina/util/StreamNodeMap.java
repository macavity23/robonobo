package com.robonobo.mina.util;

import java.util.*;

import com.robonobo.core.api.proto.CoreApi;
import com.robonobo.core.api.proto.CoreApi.Node;

/**
 * Keeps a bidirectional mapping between streams and nodes
 */
public class StreamNodeMap {
	Map<String, Node> nodesById = new HashMap<String, Node>();
	Map<String, Set<String>> streamMap = new HashMap<String, Set<String>>();
	Map<String, Set<String>> nodeMap = new HashMap<String, Set<String>>();

	public StreamNodeMap() {
		streamMap = new HashMap<String, Set<String>>();
		nodeMap = new HashMap<String, Set<String>>();
	}

	public synchronized void addMapping(String streamId, Node node) {
		String nodeId = node.getId();
		nodesById.put(nodeId, node);
		Set<String> nodeIdsForThisStream = streamMap.get(streamId);
		if (nodeIdsForThisStream == null) {
			nodeIdsForThisStream = new HashSet<String>();
			streamMap.put(streamId, nodeIdsForThisStream);
		}
		nodeIdsForThisStream.add(nodeId);

		Set<String> streamsForThisNode = nodeMap.get(nodeId);
		if (streamsForThisNode == null) {
			streamsForThisNode = new HashSet<String>();
			nodeMap.put(nodeId, streamsForThisNode);
		}
		streamsForThisNode.add(streamId);
	}

	public synchronized void removeMapping(String streamId, Node node) {
		String nodeId = node.getId();
		Set<String> nodeIdsForThisStream = streamMap.get(streamId);
		if (nodeIdsForThisStream != null) {
			nodeIdsForThisStream.remove(nodeId);
			if (nodeIdsForThisStream.size() == 0)
				streamMap.remove(streamId);
		}

		Set<String> streamsForThisNode = nodeMap.get(nodeId);
		if (streamsForThisNode != null) {
			streamsForThisNode.remove(streamId);
			if (streamsForThisNode.size() == 0)
				nodeMap.remove(nodeId);
		}
	}

	public synchronized void removeNode(Node node) {
		String nodeId = node.getId();
		Set<String> streamsForThisNode = nodeMap.get(nodeId);
		if (streamsForThisNode != null) {
			for (String streamId : streamsForThisNode) {
				Set<String> nodeIdsForThisStream = streamMap.get(streamId);
				if (nodeIdsForThisStream != null) {
					nodeIdsForThisStream.remove(nodeId);
					if(nodeIdsForThisStream.size() == 0)
						streamMap.remove(streamId);
				}
			}
			nodeMap.remove(nodeId);
		}
		nodesById.remove(nodeId);
	}

	public synchronized List<Node> getNodes(String streamId) {
		Set<String> nodesForThisStream = streamMap.get(streamId);
		List<Node> result = new ArrayList<Node>();
		if (nodesForThisStream != null) {
			for (String nodeId : nodesForThisStream) {
				result.add(nodesById.get(nodeId));
			}
		}
		return result;
	}

	public synchronized List<String> getStreams(String nodeId) {
		Set<String> streamsForThisNode = nodeMap.get(nodeId);
		List<String> result = new ArrayList<String>();
		if(streamsForThisNode != null)
			result.addAll(streamsForThisNode);
		return result;
	}
	
	public synchronized Set<String> getAllStreams() {
		Set<String> streams = new HashSet<String>();
		streams.addAll(streamMap.keySet());
		return streams;
	}

	public synchronized void updateNodeDetails(Node n) {
		nodesById.put(n.getId(), n);
	}
	
	public synchronized void clear() {
		streamMap.clear();
		nodeMap.clear();
	}
}
