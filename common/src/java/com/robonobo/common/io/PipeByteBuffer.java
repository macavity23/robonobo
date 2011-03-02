package com.robonobo.common.io;
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
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;

import com.robonobo.common.util.TextUtil;

/**
 * This ByteBuffer acts as a pipe, a source OutputStream delivers bytes to one end, and a 
 * sink InputStream reads them from the other end.  The buffer dynamically grows and shrinks
 * as is needed.
 * 
 * There is a maxBufferSize, if the buffer attempts to go above this, an IOException is thrown.
 * 
 * This source code file is Copyright 2003-2008 Ray Hilton / Will Morton. All
 * rights reserved. Unauthorised duplication of this file is expressly forbidden
 * without prior written permission.
 */
public class PipeByteBuffer {
	private static final int DEFAULT_SIZE = 16;
	private byte[] buf;
	private int firstIndex;
	private int bytesInBuffer;
	private boolean closed;
	private int maxBufferSize = 2 * 1024 * 1024; // 2Mb

	public PipeByteBuffer() {
		this(new byte[DEFAULT_SIZE]);
		bytesInBuffer = 0;
	}

	public PipeByteBuffer(byte[] arr) {
		bytesInBuffer = arr.length;
		buf = new byte[bytesInBuffer];
		System.arraycopy(arr, 0, buf, 0, bytesInBuffer);
		firstIndex = 0;
	}

	public PipeByteBuffer(int maxBufferSize) {
		this();
		this.maxBufferSize = maxBufferSize;
		bytesInBuffer = 0;
	}

	public synchronized long getLength() {
		return bytesInBuffer;
	}

	public synchronized long getCurrentBufferSize() {
		return buf.length;
	}

	public boolean isClosed() {
		return closed;
	}

	public PipeInputStream getSink() {
		return new PipeInputStream();
	}

	public PipeOutputStream getSource() {
		return new PipeOutputStream();
	}

	public class PipeInputStream extends InputStream {
		public int read() throws IOException {
			synchronized(PipeByteBuffer.this) {
				// Block if no data (unless we're closed)
				while(bytesInBuffer < 1) {
					if(closed) {
						return -1;
					}
					try {
						PipeByteBuffer.this.wait();
					} catch(InterruptedException e) {
						throw new IOException("Blocking read was interrupted");
					}
				}

				byte b = buf[firstIndex];
				firstIndex = (firstIndex + 1) % buf.length;
				bytesInBuffer--;

				// check to see if we need to shrink
				// Disabled shrinking for the mo - leave it at max size
				//shrinkBuffer();

				PipeByteBuffer.this.notifyAll();
				return b;
			}
		}
		
		public int read(byte[] readArr, int off, int len) throws IOException {
			synchronized(PipeByteBuffer.this) {
				// Block if no data (unless we're closed)
				while(bytesInBuffer < 1) {
					if(closed) {
						return -1;
					}
					try {
						PipeByteBuffer.this.wait();
					} catch(InterruptedException e) {
						throw new IOException("Blocking read was interrupted");
					}
				}
				
				int numToRead = len;
				if(numToRead > bytesInBuffer)
					numToRead = bytesInBuffer;
				for(int i=0;i<numToRead;i++) {
					readArr[off+i] = buf[firstIndex];
					firstIndex = (firstIndex + 1) % buf.length;
					bytesInBuffer--;
				}
				PipeByteBuffer.this.notifyAll();
				return numToRead;
			}
		}
		
		public int read(byte[] b) throws IOException {
			return read(b, 0, b.length);
		}
		
		public void close() throws IOException {
			super.close();
			PipeByteBuffer.this.close();
		}
	}

	public class PipeOutputStream extends OutputStream {
		public void write(int arg0) throws IOException {
			synchronized(PipeByteBuffer.this) {
				if(closed)
					throw new IOException("Pipe is closed");

				// If our buffer is getting large, wait until someone reads some data
				while(!canBuffer(1)) {
					if(closed)
						return;
					try {
						PipeByteBuffer.this.wait();
					} catch(InterruptedException e) {
						throw new IOException("Blocking write was interrupted");
					}
				}
				while(1 > (buf.length - bytesInBuffer)) {
					expandBuffer();
				}

				int thisIndex = (firstIndex + bytesInBuffer) % buf.length;
				buf[thisIndex] = (byte) (arg0 & 0xff);
				bytesInBuffer++;

				PipeByteBuffer.this.notifyAll();
			}
		}
		
		public void write(byte[] writeArr, int off, int len) throws IOException {
			synchronized(PipeByteBuffer.this) {
				if(closed)
					throw new IOException("Pipe is closed");

				// If our buffer is getting large, wait until someone reads some data
				while(!canBuffer(len)) {
					if(closed)
						return;
					try {
						PipeByteBuffer.this.wait();
					} catch(InterruptedException e) {
						throw new IOException("Blocking write was interrupted");
					}
				}
				while(len > (buf.length - bytesInBuffer)) {
					expandBuffer();
				}
				
				for(int i=0;i<len;i++) {
					int thisIndex = (firstIndex + bytesInBuffer) % buf.length;
					buf[thisIndex] = writeArr[off+i];
					bytesInBuffer++;
				}
				
				PipeByteBuffer.this.notifyAll();
			}
		}
		
		public void write(byte[] b) throws IOException {
			write(b, 0, b.length);
		}
		
		public void close() throws IOException {
			super.close();
			PipeByteBuffer.this.close();
		}
	}

	/**
	 * Indicates that no more writing will be done to the PipeStream,
	 * and that future read()s
	 * should return 0 when the stream is empty
	 */
	public synchronized void close() {
		closed = true;
		notifyAll();
	}

	/**
	 * clears the buffer and resets the buffer size.
	 *
	 */
	public synchronized void clear() {
		bytesInBuffer = 0;
		firstIndex = 0;
		buf = new byte[DEFAULT_SIZE];
	}

	private boolean canBuffer(int numBytes) {
		return ((bytesInBuffer + numBytes) <= maxBufferSize);
	}
	private void expandBuffer() throws IOException {
		// We just double the size and copy the contents to the new
		// buffer
		if(buf.length == 0) {
			buf = new byte[DEFAULT_SIZE];
			firstIndex = 0;
			return;
		}

		byte[] newBuf = new byte[buf.length * 2];
		for(int i = 0; i < bytesInBuffer; i++) {
			int thisIndex = (firstIndex + i) % buf.length;
			newBuf[i] = buf[thisIndex];
		}

		firstIndex = 0;
		buf = newBuf;
	}

	public int getMaxBufferSize() {
		return maxBufferSize;
	}

	public void setMaxBufferSize(int maxBufferSize) {
		this.maxBufferSize = maxBufferSize;
	}

	public synchronized void printBufferState(PrintStream out) {
		out.println("firstIndex="+firstIndex+", length="+bytesInBuffer);
		for(int i=0;i<buf.length;i++) {
			out.print(TextUtil.rightPad(Integer.toString(i), 4));
		}
		out.println();
		for(int i=0;i<buf.length;i++) {
			out.print(TextUtil.rightPad(Byte.toString(buf[i]), 4));
		}
		out.println();
	}
}