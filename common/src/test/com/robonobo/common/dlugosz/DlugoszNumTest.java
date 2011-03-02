package com.robonobo.common.dlugosz;

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


import java.nio.ByteBuffer;

import junit.framework.TestCase;

import com.robonobo.common.dlugosz.Dlugosz;

public class DlugoszNumTest extends TestCase {

	public void testShortByte() throws Exception {
		ByteBuffer b = Dlugosz.encode(127);
		b.rewind();
		assertEquals(0x7F,b.get());
		b.rewind();
		assertEquals(127,Dlugosz.readLong(b));
	}
	
	public void testNumber128() throws Exception {
		ByteBuffer b = Dlugosz.encode(128);
		b.rewind();
		assertEquals(0x80,b.get() & 0xff);
		assertEquals(0x80,b.get() &0xff);
		b.rewind();
		assertEquals(128,Dlugosz.readLong(b));
	}
	
	public void testNumber1() throws Exception {
		ByteBuffer b = Dlugosz.encode(1);
		b.rewind();
		assertEquals(1,b.get());
		b.rewind();
		assertEquals(1,Dlugosz.readLong(b));
	}
	
	public void testNumber5() throws Exception {
		ByteBuffer b = Dlugosz.encode(5);
		b.rewind();
		assertEquals(5,b.get());
		b.rewind();
		assertEquals(5,Dlugosz.readLong(b));
	}
	
	public void testNumber20() throws Exception {
		ByteBuffer b = Dlugosz.encode(20);
		b.rewind();
		assertEquals(0x14,b.get());

		b.rewind();
		assertEquals(20,Dlugosz.readLong(b));
	}
	
	public void testNumber200() throws Exception {
		ByteBuffer b = Dlugosz.encode(200);
		b.rewind();
		assertEquals(0x80,b.get() & 0xff);
		assertEquals(0xc8,b.get() & 0xff);

		b.rewind();
		assertEquals(200,Dlugosz.readLong(b));
	}
		
	public void testNumber400() throws Exception {
		ByteBuffer b = Dlugosz.encode(400);
		b.rewind();
		assertEquals(0x81,b.get() & 0xff);
		assertEquals(0x90,b.get() & 0xff);

		b.rewind();
		assertEquals(400,Dlugosz.readLong(b));
	}
	
	public void testNumber10000() throws Exception {
		ByteBuffer b = Dlugosz.encode(10000);
		b.rewind();
		assertEquals(0xa7,b.get() & 0xff);
		assertEquals(0x10,b.get() & 0xff);

		b.rewind();
		assertEquals(10000,Dlugosz.readLong(b));
	}
	
	public void testNumber16384() throws Exception {
		ByteBuffer b = Dlugosz.encode(16384);
		b.rewind();
		assertEquals(0xc0,b.get() & 0xff);
		assertEquals(0x40,b.get() & 0xff);
		assertEquals(0x00,b.get() & 0xff);

		b.rewind();
		assertEquals(16384,Dlugosz.readLong(b));
	}
	

	public void testNumber2000000() throws Exception {
		ByteBuffer b = Dlugosz.encode(2000000);
		b.rewind();
		assertEquals(0xde,b.get() & 0xff);
		assertEquals(0x84,b.get() & 0xff);
		assertEquals(0x80,b.get() & 0xff);
		
		b.rewind();
		assertEquals(2000000,Dlugosz.readLong(b));
	}

}
