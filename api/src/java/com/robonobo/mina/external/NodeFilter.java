package com.robonobo.mina.external;

import com.robonobo.core.api.proto.CoreApi.Node;

public interface NodeFilter {
	public String getFilterName();
	public boolean acceptNode(Node node);
}
