package com.robonobo.mina.network;


public interface BroadcastConnection {
	public void setBCPair(BCPair bcPair);
	public void addPageToQ(long pageNum, int auctionStatus);
	public void close();
	/** bytes per sec */
	public int getFlowRate();
	/** gamma is the flow control measure, 0 <= gamma <= 1.  See seon dox */
	public void setGamma(float gamma);
}
