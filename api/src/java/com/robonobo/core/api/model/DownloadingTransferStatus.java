package com.robonobo.core.api.model;


public class DownloadingTransferStatus implements TransferStatus {
	private int numSources;

	public DownloadingTransferStatus(int numSources) {
		this.numSources = numSources;
	}

	public int getNumSources() {
		return numSources;
	}
	
	public void setNumSources(int numSources) {
		this.numSources = numSources;
	}
}
