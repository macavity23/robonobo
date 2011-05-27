package com.robonobo.mina.external;

public interface MinaListener {
	public void receptionCompleted(String streamId);
	public void receptionConnsChanged(String streamId);
	public void nodeConnected(ConnectedNode node);
	public void nodeDisconnected(ConnectedNode node);
}
