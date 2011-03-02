package com.robonobo.mina.external.node;

import java.net.InetAddress;

import com.robonobo.core.api.proto.CoreApi.EndPoint;

public class EonEndPoint {
	protected int udpPort;
	protected int eonPort;
	protected InetAddress address;
	protected String url;

	/** Does only a quick check against the protocol, doesn't check the url components */
	public static boolean isEonUrl(String url) {
		int colPos = url.indexOf(":");
		if(colPos < 0)
			return false;
		String protocol = url.substring(0, colPos);
		if(!protocol.equals("mina-eon"))
			return false;
		return true;
	}
	
	public EonEndPoint(String url) {
		this.url = url;
		int colPos = url.indexOf(':');
		if(colPos < 0)
			throw new RuntimeException("Error in url "+url);
		String protocol = url.substring(0, colPos);
		if(!protocol.equals("mina-eon"))
			throw new RuntimeException("Invalid eon url "+url);
		String urlStr = url.substring(colPos+1, url.length() - ((url.endsWith("/")) ? 1 : 0));
		// Grab the last : and substring, rather than just splitting, to allow ipv6 addresses
		colPos = urlStr.lastIndexOf(':');
		String addrAndUdp = urlStr.substring(0, colPos);
		String eonPortStr = urlStr.substring(colPos+1);
		colPos = addrAndUdp.lastIndexOf(':');
		String addrStr = addrAndUdp.substring(0, colPos);
		String udpPortStr = addrAndUdp.substring(colPos+1);
		try {
			address = InetAddress.getByName(addrStr);
			udpPort = Integer.parseInt(udpPortStr);
			eonPort = Integer.parseInt(eonPortStr);
		} catch(Exception e) {
			throw new RuntimeException("Invalid eon url "+url);
		}
	}

	public EonEndPoint(InetAddress addr, int udpPort, int eonPort) {
		this.udpPort = udpPort;
		this.eonPort = eonPort;
		this.address = addr;
		url = "mina-eon:"+addr.getHostAddress()+":"+udpPort+":"+eonPort;
	}

	public boolean equals(Object obj) {
		if(obj instanceof EonEndPoint)
			return hashCode() == obj.hashCode();
		return false;
	}

	public int hashCode() {
		return getClass().getName().hashCode() ^ udpPort ^ eonPort;
	}
	
	public int getEonPort() {
		return eonPort;
	}

	public int getUdpPort() {
		return udpPort;
	}

	public InetAddress getAddress() {
		return address;
	}
	
	public String getUrl() {
		return url;
	}
	
	@Override
	public String toString() {
		return getAddress().getHostAddress()+":"+getUdpPort()+":"+getEonPort();
	}
	
	public EndPoint toMsg() {
		return EndPoint.newBuilder().setUrl(url).build();
	}
}
