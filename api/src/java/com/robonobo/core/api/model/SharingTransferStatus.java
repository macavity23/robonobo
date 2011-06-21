package com.robonobo.core.api.model;

public class SharingTransferStatus implements TransferStatus {
	@Override
	public String toString() {
		return "Sharing";
	}
	
	@Override
	public int compareTo(TransferStatus ts) {
		if(ts instanceof SharingTransferStatus)
			return 0;
		return 1;
	}
}
