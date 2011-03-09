package com.robonobo.mina.external.node;

import java.net.InetAddress;

public class SeonEndPoint extends EonEndPoint {
	public SeonEndPoint(InetAddress addr, int udpPort, int eonPort) {
		this.udpPort = udpPort;
		this.eonPort = eonPort;
		this.address = addr;
		// This next line will get stringbufferized at runtime
		url = "seon:"+addr.getHostAddress()+":"+udpPort+":"+eonPort+";";
	}
}
