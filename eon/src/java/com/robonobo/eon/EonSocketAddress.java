package com.robonobo.eon;

/*
 * Eye-Of-Needle
 * Copyright (C) 2008 Will Morton (macavity@well.com) & Ray Hilton (ray@wirestorm.net)

 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

import java.net.*;

public class EonSocketAddress {
	int eonPort;
	int udpPort;
	InetAddress address;

	public EonSocketAddress(int port, int eonPort) throws UnknownHostException {
		this(Inet4Address.getLocalHost(), port, eonPort);
	}

	public EonSocketAddress(InetAddress address, int port, int eonPort) {
		setAddress(address);
		setUdpPort(port);
		setEonPort(eonPort);
	}

	public EonSocketAddress(InetSocketAddress socketAddress, int eonPort) {
		if (socketAddress != null) {
			setAddress(socketAddress.getAddress());
			setUdpPort(socketAddress.getPort());
		}
		setEonPort(eonPort);
	}

	public EonSocketAddress(String address, int port, int eonPort) throws UnknownHostException {
		this(Inet4Address.getByName(address), port, eonPort);
	}

	public int getEonPort() {
		return eonPort;
	}

	public void setEonPort(int eonPort) {
		if (eonPort > 65535 || eonPort < 0)
			throw new IllegalArgumentException("Ports must be between 0 and 65535");

		this.eonPort = eonPort;
	}

	public String toString() {
		String addrStr = (address == null) ? "0.0.0.0" : address.getHostAddress();
		// This next line gets optimised to stringbuffer at runtime
		return addrStr + ":" + getUdpPort() + ":" + getEonPort();
	}

	public InetSocketAddress getInetSocketAddress() {
		return new InetSocketAddress(getAddress(), getUdpPort());
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof EonSocketAddress)) {
			return false;
		}

		EonSocketAddress otherEP = (EonSocketAddress) obj;
		if (getAddress().equals(otherEP.getAddress()) && getUdpPort() == otherEP.getUdpPort()
				&& getEonPort() == otherEP.getEonPort()) {
			return true;
		} else {
			return false;
		}
	}

	public int hashCode() {
		{
			// Cast this to a long in case it overflows
			long hash = (long) getAddress().hashCode();

			hash += (long) getUdpPort() + (long) getEonPort();
			if (hash > (long) Integer.MAX_VALUE) {
				// Overflow - take it back within range
				long overflow = (long) Integer.MAX_VALUE - hash;
				hash = (long) Integer.MIN_VALUE + overflow - 1;
			}

			return (int) hash;
		}

	}

	public InetAddress getAddress() {
		return address;
	}

	public void setAddress(InetAddress address) {
		this.address = address;
	}

	public int getUdpPort() {
		return udpPort;
	}

	public void setUdpPort(int port) {
		if (port > 65535 || port <= 0)
			throw new IllegalArgumentException("Ports must be between 1 and 65535");

		this.udpPort = port;
	}
}
