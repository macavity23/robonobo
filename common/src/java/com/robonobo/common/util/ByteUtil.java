package com.robonobo.common.util;

import java.io.*;
import java.nio.ByteBuffer;

public class ByteUtil {
	public static CharSequence showFirstBytes(ByteBuffer buf, int numBytes) {
		if (numBytes > buf.remaining())
			throw new IllegalArgumentException("Not enough bytes in buffer");
		StringBuffer sb = new StringBuffer();
		for (int i = 0; i < numBytes; i++) {
			if (i > 0)
				sb.append(", ");
			sb.append("0x").append(0xff & buf.get(buf.position() + i));
		}
		return sb;
	}

	/** Prints out the contents of the buffer. Only useful for debugging */
	public static void printBuf(ByteBuffer buf, StringBuffer sb) {
		if (buf.array().length == 0)
			return;
		byte lastb = buf.array()[buf.arrayOffset()];
		int numTimes = 1;
		for (int i = 1; i < buf.limit(); i++) {
			byte b = buf.array()[buf.arrayOffset() + i];
			if (b == lastb)
				numTimes++;
			else {
				sb.append("(").append(numTimes).append(" x ").append("0x").append(Integer.toHexString(lastb)).append(") ");
				lastb = b;
				numTimes = 1;
			}
		}
		sb.append("(").append(numTimes).append(" x ").append("0x").append(Integer.toHexString(lastb)).append(") ");
	}

	/** Dumps the contents of the input stream into the output stream, then closes both */
	public static void streamDump(InputStream is, OutputStream os) throws IOException {
		streamDump(is, os, true);
	}

	public static void streamDump(InputStream is, OutputStream os, boolean close) throws IOException {
		byte[] buf = new byte[1024];
		int numRead;
		while ((numRead = is.read(buf)) > 0)
			os.write(buf, 0, numRead);
		if (close) {
			is.close();
			os.close();
		}
	}
}
