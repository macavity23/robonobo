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


import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class IP4Range {
	private Inet4Address baseAddr;
	private int numBits;
	
	/**
	 * @param addrAndBits EG 10.1.2.3/16
	 */
	public IP4Range(String addrAndBits) throws IOException {
		try {
			String addrStr = addrAndBits.substring(0, addrAndBits.indexOf("/"));
			baseAddr = (Inet4Address)InetAddress.getByName(addrStr);
			String bitsStr = addrAndBits.substring(addrAndBits.indexOf("/")+1);
			numBits = Integer.parseInt(bitsStr);
		} catch(Exception e) {
			throw new IOException("Error parsing string "+addrAndBits);
		}
	}
	
	public IP4Range(Inet4Address addr, int numBits) {
		this.baseAddr = addr;
		this.numBits = numBits;
	}
	
	public boolean contains(Inet4Address addr) {
		for(int i=0;i<numBits;i++) {
			int baseBit = getBitAt(baseAddr, i);
			int addrBit = getBitAt(addr, i);
			if(baseBit != addrBit)
				return false;
		}
		return true;
	}

	private int getBitAt(Inet4Address addr, int bitPos) {
		byte[] bytes = addr.getAddress();
		int byteIndex = bitPos / 8;
		int byteOffset = bitPos % 8;
		return ((bytes[byteIndex] & 0xff) >> byteOffset) & 0x1;
	}
	
	public static void main(String[] args) throws Exception {
		String rangeStr = "10.2.23.0/16";
		IP4Range range = new IP4Range(rangeStr);
		System.out.println("Testing range "+rangeStr);
		List toTest = new ArrayList();
		for(int i=0;i<8;i++) {
			for(int j=0;j<256;j++) {
				for(int k=0;k<256;k++) {
					toTest.add((Inet4Address)InetAddress.getByName("10."+i+"."+j+"."+k));
				}
			}
		}
		
		for (Iterator iterator = toTest.iterator(); iterator.hasNext();) {
			Inet4Address addr = (Inet4Address) iterator.next();
			if(range.contains(addr))
				System.out.println("Contains "+addr.getHostAddress());
			
		}
	}
	
	public Inet4Address getBaseAddr() {
		return baseAddr;
	}

	public int getNumBits() {
		return numBits;
	}
}
