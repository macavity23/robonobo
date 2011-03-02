package com.robonobo.core.api;

import java.util.Map;

public interface TransferSpeedListener {
	public void newTransferSpeeds(Map<String, TransferSpeed> speedsByStream, Map<String, TransferSpeed> speedsByNode);
}
