package com.robonobo.core.api.model;

public class CloudTrack extends Track {
	private int numSources;
	
	public CloudTrack(Stream stream, int numSources) {
		super(stream);
		transferStatus = new CloudTransferStatus(numSources);
		this.numSources = numSources;
	}
	
	public CloudTrack(CloudTrack t) {
		super(t);
		numSources = t.numSources;
	}
	
	@Override
	public CloudTrack clone() {
		return new CloudTrack(this);
	}
	
	public int getNumSources() {
		return numSources;
	}
}
