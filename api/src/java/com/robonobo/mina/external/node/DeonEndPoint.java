package com.robonobo.mina.external.node;

import java.net.InetAddress;

public class DeonEndPoint extends EonEndPoint {
	DeonEndPoint(InetAddress addr, int udpPort, int eonPort) {
		this.udpPort = udpPort;
		this.eonPort = eonPort;
		this.address = addr;
		// This next line will get stringbufferized at runtime
		url = "deon:"+addr.getHostAddress()+":"+udpPort+":"+eonPort;
	}

}
