package com.robonobo.core.api.model;

public class CloudTrack extends Track {
	private int numSources;
	
	public CloudTrack(Stream stream, int numSources) {
		super(stream);
		transferStatus = new CloudTransferStatus(numSources);
		this.numSources = numSources;
	}
	
	public int getNumSources() {
		return numSources;
	}
}
