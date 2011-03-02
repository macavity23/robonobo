package com.robonobo.mina.network;

import com.robonobo.core.api.proto.CoreApi.EndPoint;


public interface ListenConnection {
	// Not many methods here, this class just punts pages up to the lcpair
	public void setLCPair(LCPair lcPair);
	public EndPoint getEndPoint();
	public void close();
	/** bytes per sec */
	public int getFlowRate();
}
