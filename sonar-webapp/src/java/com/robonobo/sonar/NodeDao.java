package com.robonobo.sonar;

import java.util.List;

import com.robonobo.core.api.proto.CoreApi.Node;

public interface NodeDao {
	public List<Node> getAllSupernodes(Node except);

	public void deleteAllNodes();

	public void deleteDuplicateNodes(Node n);

	public void saveNode(Node n);
	
	public void deleteNodesOlderThan(long maxAgeMs);
}