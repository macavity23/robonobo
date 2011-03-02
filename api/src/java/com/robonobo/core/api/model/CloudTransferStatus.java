package com.robonobo.core.api.model;

import static com.robonobo.common.util.TextUtil.numItems;

public class CloudTransferStatus implements TransferStatus {
	int numSources;

	public CloudTransferStatus(int numSources) {
		this.numSources = numSources;
	}

	public int getNumSources() {
		return numSources;
	}

	public void setNumSources(int numSources) {
		this.numSources = numSources;
	}

	public String toString() {
		if(numSources == 0)
			return "Finding sources...";
		return numItems(numSources, "source");
	}
}
