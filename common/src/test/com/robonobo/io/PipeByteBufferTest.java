package com.robonobo.io;
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

import junit.framework.TestCase;

import com.robonobo.common.io.PipeByteBuffer;

public class PipeByteBufferTest extends TestCase
{

	public void testBufferGrows() throws IOException {
		
		// initial capacity 1000
		PipeByteBuffer p = new PipeByteBuffer(1000);
		
		// write 1000 bytes
		for(int i = 0; i < 1000; i++) {
			p.getSource().write(255);
		}
		
		// check buffer size
		assertEquals(1000, p.getCurrentBufferSize());
		
		// add one more byte
		p.getSource().write(255);
		
		// check buffer size
		assertEquals(2000, p.getCurrentBufferSize());
	}
	
	public void testBufferShrinks() throws IOException {
		
		// initial capacity 1000
		PipeByteBuffer p = new PipeByteBuffer(1000);
		
		// write 1000 bytes
		for(int i = 0; i < 1000; i++) {
			p.getSource().write(255);
		}
		
		// check buffer size
		assertEquals(1000, p.getCurrentBufferSize());
		
		// read half the bytes
		for(int i = 0; i < 500; i++) {
			p.getSink().read();
		}
		
		// check buffer size
		assertEquals(1000, p.getCurrentBufferSize());
		
		// read one more byte
		p.getSink().read();
		
		// check buffer size
		assertEquals(500, p.getCurrentBufferSize());
	}
	
	public void testBufferThrowsExceptionAtMaxBufferSize() throws IOException {
			
		// initial capacity 1000
		PipeByteBuffer p = new PipeByteBuffer(1000);
		p.setMaxBufferSize(1800);		// less than 2*1000;
		
		// write 1000 bytes
		for(int i = 0; i < 1000; i++) {
			p.getSource().write(255);
		}
		
		// check buffer size
		assertEquals(1000, p.getCurrentBufferSize());
		
		// write one more
		try {
			p.getSource().write(255);
		}
		catch(IOException e) {
			// expected
			assertTrue(e.getMessage().startsWith("PipeByteBuffer"));
			return;
		}
		
		fail();
	}
	
}
