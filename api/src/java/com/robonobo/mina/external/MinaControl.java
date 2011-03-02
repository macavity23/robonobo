package com.robonobo.mina.external;

import java.util.List;
import java.util.Map;
import java.util.Set;

import com.robonobo.core.api.CurrencyClient;
import com.robonobo.core.api.StreamVelocity;
import com.robonobo.core.api.TransferSpeed;
import com.robonobo.core.api.proto.CoreApi.Node;
import com.robonobo.mina.external.buffer.PageBuffer;

public interface MinaControl {
	public void start() throws MinaException;

	public void stop() throws MinaException;

	/**
	 * Stops this Mina node immediately, without gracefully shutting down
	 * connections. Use for network simulation ONLY!
	 */
	public void abort();

	public void addMinaListener(MinaListener listener);

	public void addNodeLocator(NodeLocator locator);

	public void startBroadcast(String streamId, PageBuffer pb);

	public void stopBroadcast(String streamId);

	public void startReception(String streamId, PageBuffer pb, StreamVelocity sv);

	public void stopReception(String streamId);

	public List<String> getMyEndPointUrls();

	public List<ConnectedNode> getConnectedNodes();

	public List<String> getConnectedSources(String streamId);
	
	public boolean isConnectedToSupernode();
	
	public String getMyNodeId();

	public boolean isStarted();

	public int numSources(String streamId);
	
	public Set<String> getSources(String streamId);
	
	public void addFoundSourceListener(String streamId, FoundSourceListener listener);

	public void removeFoundSourceListener(String streamId, FoundSourceListener listener);

	public Set<Node> getKnownSources(String streamId);

	public PageBuffer getPageBuffer(String streamId);

	public Map<String, TransferSpeed> getTransferSpeeds();
	
	public void clearStreamPriorities();
	
	public void setStreamPriority(String streamId, int priority);

	/**
	 * The streamvelocity dictates how fast we want this stream (slower = cheaper)
	 */
	public void setStreamVelocity(String streamId, StreamVelocity sv);

	/**
	 * Sets streamvelocity for all streams except the supplied one 
	 */
	public void setAllStreamVelocitiesExcept(String streamId, StreamVelocity sv);
	
	public void setCurrencyClient(CurrencyClient client);

	public abstract void removeNodeFilter(NodeFilter nf);

	public abstract void addNodeFilter(NodeFilter nf);

	public abstract void setHandoverHandler(HandoverHandler handler);
}
