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

import java.nio.ByteBuffer;

public abstract class EONPacket {
	public static final int EON_PROTOCOL_DEON = 1;
	public static final int EON_PROTOCOL_SEON = 2;
	
	// Note: this won't set the IP Endpoints - do that externally
	public static EONPacket parse(ByteBuffer buf) {
		int numBytes = buf.remaining();
		int sourcePort, destPort, protocol;
		EonSocketAddress sourceEP, destEP;
		// All EON packets are at least 40 bits long
		if (numBytes < 5)
			return null;
		// Source Port is bytes 0-1
		byte b0 = buf.get();
		byte b1 = buf.get();
		sourcePort = (b0 << 8) + b1;
		sourceEP = new EonSocketAddress(null, sourcePort);
		// Dest Port is bytes 2-3
		byte b2 = buf.get();
		byte b3 = buf.get();
		destPort = (b2 << 8) + b3;
		destEP = new EonSocketAddress(null, destPort);
		// Protocol is byte 4
		protocol = buf.get();
		if (protocol == 1) {
			// D-EON packet
			ByteBuffer payload = buf.slice();
			DEONPacket thisPacket = new DEONPacket(sourceEP, destEP, payload);
			return thisPacket;
		} else if (protocol == 2) {
			// S-EON packet - must be at least 128 bits long
			if (numBytes < 14)
				return null;
			boolean ack, rst, syn, fin;
			long seqNum, ackNum;
			// Flags are the first 4 bits of byte 5
			byte b5 = buf.get();
			ack = (b5 & 0x80) > 0 ? true : false;
			rst = (b5 & 0x40) > 0 ? true : false;
			syn = (b5 & 0x20) > 0 ? true : false;
			fin = (b5 & 0x10) > 0 ? true : false;
			// Number of SACK regions is bits 4-5 of byte 5
			int numSackRegions = (b5 & 0xC) >> 2;
			// Sequence number is bytes 6-9
			seqNum = 0l;
			seqNum |= buf.get() & 0xFF;
			seqNum <<= 8;
			seqNum |= buf.get() & 0xFF;
			seqNum <<= 8;
			seqNum |= buf.get() & 0xFF;
			seqNum <<= 8;
			seqNum |= buf.get() & 0xFF;
			// Acknowledgement number is bytes 10-13
			ackNum = 0l;
			ackNum |= buf.get() & 0xFF;
			ackNum <<= 8;
			ackNum |= buf.get() & 0xFF;
			ackNum <<= 8;
			ackNum |= buf.get() & 0xFF;
			ackNum <<= 8;
			ackNum |= buf.get() & 0xFF;
			// Up to 3 Sack regions
			long[] begins = null, ends = null;
			if (numSackRegions > 0) {
				begins = new long[numSackRegions];
				ends = new long[numSackRegions];
				for (int i = 0; i < numSackRegions; i++) {
					long begin = 0l;
					begin |= buf.get() & 0xFF;
					begin <<= 8;
					begin |= buf.get() & 0xFF;
					begin <<= 8;
					begin |= buf.get() & 0xFF;
					begin <<= 8;
					begin |= buf.get() & 0xFF;
					long end = 0l;
					end |= buf.get() & 0xFF;
					end <<= 8;
					end |= buf.get() & 0xFF;
					end <<= 8;
					end |= buf.get() & 0xFF;
					end <<= 8;
					end |= buf.get() & 0xFF;
					begins[i] = begin;
					ends[i] = end;
				}
			}
			ByteBuffer payload = buf.slice();
			SEONPacket thisPacket = new SEONPacket(sourceEP, destEP, payload);
			thisPacket.setACK(ack);
			thisPacket.setRST(rst);
			thisPacket.setSYN(syn);
			thisPacket.setFIN(fin);
			if (numSackRegions > 0)
				thisPacket.setSackBlocks(begins, ends);
			thisPacket.setSequenceNumber(seqNum);
			thisPacket.setAckNumber(ackNum);
			return thisPacket;
		} else {
			// Unknown protocol
			return null;
		}
	}

	public abstract EonSocketAddress getDestSocketAddress();

	public abstract EonSocketAddress getSourceSocketAddress();

	public abstract void setDestSocketAddress(EonSocketAddress endPoint);

	public abstract void setSourceSocketAddress(EonSocketAddress endPoint);

	public abstract void toByteBuffer(ByteBuffer buf);

	public abstract int getProtocol();

	public abstract ByteBuffer getPayload();
	
	public abstract int getPayloadSize();	
}