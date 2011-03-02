package com.robonobo.common.util;
/*
 * Robonobo Common Utils
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


import java.net.InetAddress;
import java.util.*;

public class PublicNetworkDefinition {
	private Set extraPublicRanges = new HashSet();
	private Set extraPrivateRanges = new HashSet();
	
	public PublicNetworkDefinition() {
		// TODO Auto-generated constructor stub
	}
	
	public void addExtraPublicRange(NetworkRange rng) {
		extraPublicRanges.add(rng);
	}
	public void addExtraPrivateRange(NetworkRange rng) {
		extraPrivateRanges.add(rng);
	}
	
	public boolean addrIsPublic(InetAddress addr) {
		for(Iterator iter = extraPrivateRanges.iterator(); iter.hasNext();) {
			NetworkRange rng = (NetworkRange) iter.next();
			if(rng.isInRange(addr))
				return false;
		}
		for(Iterator iter = extraPublicRanges.iterator(); iter.hasNext();) {
			NetworkRange rng = (NetworkRange) iter.next();
			if(rng.isInRange(addr))
				return true;
		}
		return !(addr.isSiteLocalAddress());
	}
}
