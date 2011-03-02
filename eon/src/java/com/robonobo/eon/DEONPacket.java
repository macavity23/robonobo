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

import com.robonobo.common.exceptions.SeekInnerCalmException;

public class DEONPacket extends EONPacket {

	// region Private Fields
	private EonSocketAddress sourceEP, destEP;
	private ByteBuffer payload;

	// region Constructors
	public DEONPacket(EonSocketAddress sourceEndPoint, EonSocketAddress destEndPoint, ByteBuffer payload) {
		sourceEP = sourceEndPoint;
		destEP = destEndPoint;
		this.payload = payload;
	}

	// Internal Properties
	public EonSocketAddress getSourceSocketAddress() {
		return sourceEP;
	}

	public void setSourceSocketAddress(EonSocketAddress endPoint) {
		sourceEP = endPoint;
	}

	public EonSocketAddress getDestSocketAddress() {
		return destEP;
	}

	public void setDestSocketAddress(EonSocketAddress endPoint) {
		destEP = endPoint;
	}

	public int getProtocol() {
		return 1; // D-EON is Protocol 1
	}

	@Override
	public ByteBuffer getPayload() {
		return payload;
	}

	@Override
	public int getPayloadSize() {
		if(payload == null)
			return 0;
		return payload.limit();
	}
	
	public void setPayload(ByteBuffer payload) {
		this.payload = payload;
	}

	public void toByteBuffer(ByteBuffer buf) {
		if (sourceEP == null || destEP == null)
			throw new SeekInnerCalmException();
		// Source Port
		buf.put((byte) ((sourceEP.getEonPort() & 0xFF00) >> 8));
		buf.put((byte) (sourceEP.getEonPort() & 0xFF));
		// Destination Port
		buf.put((byte) ((destEP.getEonPort() & 0xFF00) >> 8));
		buf.put((byte) (destEP.getEonPort() & 0xFF));
		// Protocol
		buf.put((byte) 0x1); // D-EON is protocol 1
		// Packet payload
		if (payload != null) {
			payload.position(0);
			buf.put(payload);
		}
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("DEONPacket: ");
		sb.append("Source ").append(sourceEP.toString()).append(", Dest ").append(destEP.toString()).append(" ");

		if (payload != null && payload.limit() > 0) {
			sb.append(payload.limit()).append(" bytes data");
		} else {
			sb.append("no data");
		}

		return sb.toString();
	}
}