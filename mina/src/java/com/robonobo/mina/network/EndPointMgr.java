package com.robonobo.mina.network;

import java.util.List;

import com.robonobo.core.api.proto.CoreApi.EndPoint;
import com.robonobo.core.api.proto.CoreApi.Node;
import com.robonobo.mina.external.HandoverHandler;
import com.robonobo.mina.instance.MinaInstance;
import com.robonobo.mina.message.proto.MinaProtocol.PublicDetails;

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
	 * alreadyTriedEps.
	 * @param indirectAllowed Are we allowed to ask them to connect to us based on this ep (eg nat traversal) 
	 * @return The newly-created cc, or null if no connection can be made using this endpoint
	 */
	public ControlConnection connectTo(Node node, List<EndPoint> alreadyTriedEps, boolean indirectAllowed);

	/** Locates local nodes (those reachable without a supernode) */
	public void locateLocalNodes();

	public void setMina(MinaInstance mina);
	
	public void configUpdated();
	
	public void setHandoverHandler(HandoverHandler handler);
	
	/** Have we decided if we can holepunch through NAT yet? */
	public boolean natTraversalDecided();
	
	/** Advised of my public details so I can decide if we can holepunch through NAT.
	 * @return true if I now have an extra endpoint to advertise, false otherwise */
	public boolean advisePublicDetails(PublicDetails publicDetails, EndPoint source);
}
