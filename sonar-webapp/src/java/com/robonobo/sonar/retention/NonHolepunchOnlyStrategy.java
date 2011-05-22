package com.robonobo.sonar.retention;

import com.robonobo.core.api.proto.CoreApi.EndPoint;
import com.robonobo.core.api.proto.CoreApi.Node;

public class NonHolepunchOnlyStrategy implements RetentionStrategy {
	@Override
	public boolean shouldRetainNode(Node node) {
		for (EndPoint ep : node.getEndPointList()) {
			if(!ep.getUrl().endsWith(";nt"))
				return true;
		}
		return false;
	}

}
