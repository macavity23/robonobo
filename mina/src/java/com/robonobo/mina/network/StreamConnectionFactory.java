package com.robonobo.mina.network;

import com.robonobo.core.api.proto.CoreApi.EndPoint;
import com.robonobo.mina.util.MinaConnectionException;



public interface StreamConnectionFactory {
	public BroadcastConnection getBroadcastConnection(ControlConnection cc, EndPoint listeningEp) throws MinaConnectionException;
	public ListenConnection getListenConnection(ControlConnection cc) throws MinaConnectionException;
}
