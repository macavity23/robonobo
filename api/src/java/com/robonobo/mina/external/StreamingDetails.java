package com.robonobo.mina.external;

public class StreamingDetails {
	private String streamUri;
	private StreamingNode[] receivingFromNodes;
	private StreamingNode[] sendingToNodes;
	private long bytesDownloaded;
	
	public StreamingDetails(String streamUri) {
		this.streamUri = streamUri;
	}

	public StreamingNode[] getReceivingFromNodes() {
		return receivingFromNodes;
	}

	public void setReceivingFromNodes(StreamingNode[] receivingFromNodes) {
		this.receivingFromNodes = receivingFromNodes;
	}

	public StreamingNode[] getSendingToNodes() {
		return sendingToNodes;
	}

	public void setSendingToNodes(StreamingNode[] sendingToNodes) {
		this.sendingToNodes = sendingToNodes;
	}

	public String getStreamUri() {
		return streamUri;
	}

	public long getBytesDownloaded() {
		return bytesDownloaded;
	}
	
	public void setBytesDownloaded(long bytesDownloaded) {
		this.bytesDownloaded = bytesDownloaded;
	}
}
