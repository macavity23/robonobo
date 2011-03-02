package com.robonobo.mina.external;

public interface MinaListener {
	public void minaStarted(MinaControl mina);
	public void minaStopped(MinaControl mina);
	public void broadcastStarted(String streamId);
	public void broadcastStopped(String streamId);
	public void receptionStarted(String streamId);
	public void receptionStopped(String streamId);
	public void receptionCompleted(String streamId);
	public void receptionConnsChanged(String streamId);
	public void nodeConnected(ConnectedNode node);
	public void nodeDisconnected(ConnectedNode node);
}
