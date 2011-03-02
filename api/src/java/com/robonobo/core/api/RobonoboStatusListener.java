package com.robonobo.core.api;

import com.robonobo.mina.external.ConnectedNode;

public interface RobonoboStatusListener {
	public void roboStatusChanged();
	public void connectionAdded(ConnectedNode node);
	public void connectionLost(ConnectedNode node);
}
