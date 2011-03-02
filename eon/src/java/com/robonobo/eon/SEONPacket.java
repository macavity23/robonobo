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
import java.util.List;


public class SEONPacket extends EONPacket {
	/** Our sequence numbers go up to 2^32-1, so we store them in longs - check against this value to ensure they are kosher */
	public static final long MAX_SEQNUM = 4294967295L; // 2^32-1
	public static final int MAX_SACK_BLOCKS = 3;
	private EonSocketAddress sourceEP, destEP;
	private long seqNum, ackNum; // NOTE: although these are typed long,
	// their max value is 2^32
	private boolean syn, ack, rst, fin;
	private long[] sackBegins;
	private long[] sackEnds;
	private ByteBuffer payload;

	public SEONPacket(EonSocketAddress sourceEndPoint, EonSocketAddress destEndPoint, ByteBuffer payload) {
		sourceEP = sourceEndPoint;
		destEP = destEndPoint;
		this.payload = payload;
		ack = rst = syn = fin = false;
	}

	public EonSocketAddress getDestSocketAddress() {
		return destEP;
	}

	public long[] getSackBegins() {
		return sackBegins;
	}
	
	public long[] getSackEnds() {
		return sackEnds;
	}
	
	public void setSackBlocks(long[] begins, long[] ends) {
		if(begins.length != ends.length)
			throw new IllegalArgumentException("begins must have the same length as ends");
		sackBegins = begins;
		sackEnds = ends;
	}
	
	public void setSackBlocks(List<Long> begins, List<Long> ends) {
		if(begins.size() != ends.size())
			throw new IllegalArgumentException("begins must have the same length as ends");
		int numBlocks = Math.min(begins.size(), MAX_SACK_BLOCKS);
		sackBegins = new long[numBlocks];
		sackEnds = new long[numBlocks];
		for(int i=0;i<numBlocks;i++) {
			sackBegins[i] = begins.get(i);
			sackEnds[i] = ends.get(i);
		}
	}
	
	// Internal Properties
	public EonSocketAddress getSourceSocketAddress() {
		return sourceEP;
	}

	public void setDestSocketAddress(EonSocketAddress endPoint) {
		destEP = endPoint;
	}

	public void setSourceSocketAddress(EonSocketAddress endPoint) {
		sourceEP = endPoint;
	}

	public void toByteBuffer(ByteBuffer buf) {
		// Source Port
		buf.put((byte) ((sourceEP.getEonPort() & 0xFF00) >> 8));
		buf.put((byte) (sourceEP.getEonPort() & 0xFF));
		// Destination Port
		buf.put((byte) ((destEP.getEonPort() & 0xFF00) >> 8));
		buf.put((byte) (destEP.getEonPort() & 0xFF));
		// Protocol
		buf.put((byte) 0x2); // S-EON is protocol 2
		// Flags
		int flagsAndSack = 0;
		if (ack)
			flagsAndSack += 0x80;
		if (rst)
			flagsAndSack += 0x40;
		if (syn)
			flagsAndSack += 0x20;
		if (fin)
			flagsAndSack += 0x10;
		// Number of sack regions goes in bits 4-5 of byte 5
		int numSackRegions = (sackBegins == null) ? 0 : sackBegins.length;
		if(numSackRegions > MAX_SACK_BLOCKS)
			numSackRegions = MAX_SACK_BLOCKS;
		if (numSackRegions > 0)
			flagsAndSack += ((numSackRegions & 0x3) << 2);
		buf.put((byte) flagsAndSack);
		// Sequence Number
		buf.put((byte) ((seqNum >> 24) & 0xFF));
		buf.put((byte) ((seqNum >> 16) & 0xFF));
		buf.put((byte) ((seqNum >> 8) & 0xFF));
		buf.put((byte) ((seqNum) & 0xFF));
		// Ack Number
		buf.put((byte) ((ackNum >> 24) & 0xFF));
		buf.put((byte) ((ackNum >> 16) & 0xFF));
		buf.put((byte) ((ackNum >> 8) & 0xFF));
		buf.put((byte) ((ackNum) & 0xFF));
		// Sack blocks
		for (int i = 0; i < numSackRegions; i++) {
			long begin = sackBegins[i];
			buf.put((byte) ((begin >> 24) & 0xFF));
			buf.put((byte) ((begin >> 16) & 0xFF));
			buf.put((byte) ((begin >> 8) & 0xFF));
			buf.put((byte) ((begin) & 0xFF));
			long end = sackEnds[i];
			buf.put((byte) ((end >> 24) & 0xFF));
			buf.put((byte) ((end >> 16) & 0xFF));
			buf.put((byte) ((end >> 8) & 0xFF));
			buf.put((byte) ((end) & 0xFF));
		}
		// Packet payload
		if (payload != null) {
			payload.position(0);
			buf.put(payload);
		}
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("SEONPacket: ");
		sb.append("Source ").append(sourceEP.toString()).append(", Dest ").append(destEP.toString()).append(" ");
		sb.append("SN ").append(String.valueOf(seqNum)).append(" AN ").append(String.valueOf(ackNum)).append(" ");
		if (isSYN()) {
			sb.append("SYN ");
		}
		if (isACK()) {
			sb.append("ACK ");
		}
		if (isRST()) {
			sb.append("RST ");
		}
		if (isFIN()) {
			sb.append("FIN ");
		}
		if (payload != null && payload.limit() > 0) {
			sb.append(payload.limit()).append(" bytes data");
		} else {
			sb.append("no data");
		}
		if (sackBegins != null && sackBegins.length > 0) {
			sb.append(" SACK[");
			for (int i = 0; i < sackBegins.length; i++) {
				if (i > 0)
					sb.append(":");
				sb.append(sackBegins[i]).append(",").append(sackEnds[i]);
			}
			sb.append("]");
		}
		return sb.toString();
	}

	public long getAckNumber() {
		return ackNum;
	}

	@Override
	public ByteBuffer getPayload() {
		return payload;
	}

	public int getPayloadSize() {
		if(payload == null)
			return 0;
		return payload.limit();
	}
	
	public int getProtocol() {
		return 2; // S-EON is Protocol 2
	}

	public long getSequenceNumber() {
		return seqNum;
	}

	public boolean isACK() {
		return ack;
	}

	public boolean isFIN() {
		return fin;
	}

	public boolean isRST() {
		return rst;
	}

	public boolean isSYN() {
		return syn;
	}

	public void setACK(boolean ack) {
		this.ack = ack;
	}

	public void setAckNumber(long n) {
		if (n > MAX_SEQNUM)
			throw new IllegalArgumentException("ack number cannot be greater than 2^32-1");
		if (n < 0)
			throw new IllegalArgumentException("ack number cannot be less than 0");
		ackNum = n;
	}

	public void setPayload(ByteBuffer payload) {
		this.payload = payload;
	}

	public void setFIN(boolean fin) {
		this.fin = fin;
	}

	public void setRST(boolean rst) {
		this.rst = rst;
	}

	public void setSequenceNumber(long n) {
		if (n > MAX_SEQNUM)
			throw new IllegalArgumentException("sequence number cannot be greater than 2^32-1");
		if (n < 0)
			throw new IllegalArgumentException("sequence number cannot be less than 0");
		seqNum = n;
	}

	public void setSYN(boolean syn) {
		this.syn = syn;
	}
}
