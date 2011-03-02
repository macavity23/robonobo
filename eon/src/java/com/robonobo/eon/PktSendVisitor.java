package com.robonobo.eon;


public interface PktSendVisitor {
	// How many bytes we can send now - updates after sendPkt is called
	public int bytesAvailable();
	// Send this packet now
	public void sendPkt(EONPacket pkt) throws EONException;
}
