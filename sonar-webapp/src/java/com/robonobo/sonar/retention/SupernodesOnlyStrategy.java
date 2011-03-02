package com.robonobo.sonar.retention;

import com.robonobo.core.api.proto.CoreApi.Node;


public class SupernodesOnlyStrategy implements RetentionStrategy {

	public boolean shouldRetainNode(Node node) {
		return node.getSupernode();
	}
}
