package com.robonobo.mina.external;

import java.util.List;

import org.apache.http.client.HttpClient;

import com.robonobo.core.api.proto.CoreApi.Node;

public interface NodeLocator {
	public List<Node> locateSuperNodes(Node node);
	public void setHttpClient(HttpClient client);
}
