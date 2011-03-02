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


import java.io.Serializable;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.HashSet;
import java.util.Set;
import java.util.regex.PatternSyntaxException;

public class NetworkRange implements Serializable {
	private static final long serialVersionUID = 1L;
	private final Set[] incOctetVals;

	public NetworkRange(String pattern) throws PatternSyntaxException {
		String[] octetStrs = pattern.split("\\.");
		if(octetStrs.length != 4)
			throw new PatternSyntaxException("Could not parse pattern", pattern, 0);
		incOctetVals = new Set[4];
		for(int i = 0; i < 4; i++) {
			HashSet octetSet = new HashSet();
			// Each octet is either a number or *
			// TODO - allow ranges
			if(octetStrs[i].equals("*")) {
				for(int j = 0; j < 256; j++) {
					octetSet.add(Integer.valueOf(j));
				}
			} else {
				try {
					octetSet.add(Integer.valueOf(octetStrs[i]));
				} catch(NumberFormatException e) {
					throw new PatternSyntaxException("Could not parse pattern", pattern, 0);
				}
			}
			incOctetVals[i] = octetSet;
		}
	}

	public boolean isInRange(InetAddress addr) {
		if(!(addr instanceof Inet4Address))
			throw new RuntimeException("ipv4 only, sorry");
		String[] octetStrs = addr.getHostAddress().split("\\.");
		for(int i = 0; i < 4; i++) {
			int octetVal = Integer.parseInt(octetStrs[i]);
			if(!incOctetVals[i].contains(Integer.valueOf(octetVal)))
				return false;
		}
		return true;
	}
}
