package com.robonobo.mina.network;

import java.util.List;

import com.robonobo.core.api.proto.CoreApi.EndPoint;
import com.robonobo.core.api.proto.CoreApi.Node;
import com.robonobo.mina.external.HandoverHandler;
import com.robonobo.mina.instance.MinaInstance;

public interface EndPointMgr {
	public void start() throws Exception;

	public void stop();

	/**
	 * If this guy is asking for service, which endpoint should I send to him
	 * (null to not advertise to him)?
	 */
	public EndPoint getEndPointForTalkingTo(Node node);

	/** The endpoint I am listening on - public version (null if none) */
	public EndPoint getPublicEndPoint();

	/** Local version */
	public EndPoint getLocalEndPoint();

	/** Can I connect to this guy? */
	public boolean canConnectTo(Node node);

	/**
	 * Connect to the specified node, but don't use any of the endpoints in
	 * alreadyTriedEps. Returns null if no connection can be made using this
	 * endpoint
	 */
	public ControlConnection connectTo(Node node, List<EndPoint> alreadyTriedEps);

	/** Locates local nodes (those reachable without a supernode) */
	public void locateLocalNodes();

	public void setMina(MinaInstance mina);
	
	public void configUpdated();
	
	public void setHandoverHandler(HandoverHandler handler);
}
