package com.robonobo.eon;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;



public class SEONConnectionChannel implements ByteChannel {
	private SEONConnection conn;
	
	public SEONConnectionChannel(SEONConnection conn) {
		this.conn = conn;
	}

	public int read(ByteBuffer buf) throws IOException {
		int posBefore = buf.position();
		try {
			conn.read(buf);
		} catch (EONException e) {
			throw new IOException("Caught EONException: "+e.getMessage());
		}
		return buf.position() - posBefore;
	}

	public void close() throws IOException {
		conn.close();
	}

	public boolean isOpen() {
		return conn.isOpen();
	}

	public int write(ByteBuffer buf) throws IOException {
		int posBefore = buf.position();
		try {
			conn.send(buf);
		} catch (EONException e) {
			throw new IOException("Caught EONException: "+e.getMessage());
		}
		return buf.position() - posBefore;
	}
}
