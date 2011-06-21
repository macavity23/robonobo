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
	
	@Override
	public int compareTo(TransferStatus o) {
		if(o instanceof SharingTransferStatus)
			return -1;
		if(o instanceof CloudTransferStatus)
			return 1;
		if(o instanceof DownloadingTransferStatus) {
			DownloadingTransferStatus od = (DownloadingTransferStatus) o;
			return numSources - od.numSources;
		}
		// Should never get here
		return 0;
	}
}
