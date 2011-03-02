/**
 * 
 */
package com.robonobo.mina.external;

public class StreamingNode {
	public String nodeId;
	public boolean complete;
	public boolean connected;
	public int bytesPerSec;

	public StreamingNode(String nodeId, int bytesPerSec) {
		this.nodeId = nodeId;
		this.bytesPerSec = bytesPerSec;
	}

	public int getBytesPerSec() {
		return bytesPerSec;
	}

	public String getNodeId() {
		return nodeId;
	}

	public boolean isComplete() {
		return complete;
	}

	public boolean isConnected() {
		return connected;
	}

	public void setBytesPerSec(int bytesPerSec) {
		this.bytesPerSec = bytesPerSec;
	}

	public void setComplete(boolean complete) {
		this.complete = complete;
	}

	public void setConnected(boolean connected) {
		this.connected = connected;
	}

	public void setNodeId(String nodeId) {
		this.nodeId = nodeId;
	}
}