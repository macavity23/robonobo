package com.robonobo.mina.agoric;

import java.util.Comparator;

import com.robonobo.mina.message.proto.MinaProtocol.ReceivedBid;

public class ReceivedBidComparator implements Comparator<ReceivedBid> {
	public int compare(ReceivedBid b1, ReceivedBid b2) {
		return Double.compare(b1.getBid(), b2.getBid());
	}
}
