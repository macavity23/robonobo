package com.robonobo.sonar.retention;

import com.robonobo.core.api.proto.CoreApi.Node;

/**
 * Of the many nodes that send their details to us, which ones should we keep to send to other nodes?
 * @author macavity
 *
 */
public interface RetentionStrategy {
	public boolean shouldRetainNode(Node node);
}
