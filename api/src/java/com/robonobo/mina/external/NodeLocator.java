package com.robonobo.mina.external;

import java.util.List;
import com.robonobo.core.api.proto.CoreApi.Node;

public interface NodeLocator {
	public List<Node> locateSuperNodes(Node node);
	public List<Node> locatePublicNodes();
}
