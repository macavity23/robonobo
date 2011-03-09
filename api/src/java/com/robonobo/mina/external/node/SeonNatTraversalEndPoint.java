package com.robonobo.mina.external.node;

import java.net.InetAddress;

public class SeonNatTraversalEndPoint extends SeonEndPoint {

	public SeonNatTraversalEndPoint(InetAddress addr, int udpPort, int eonPort) {
		super(addr, udpPort, eonPort);
		url += "nt";
	}

}
