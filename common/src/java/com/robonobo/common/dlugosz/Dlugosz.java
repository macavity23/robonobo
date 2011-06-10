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

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;

import com.robonobo.common.exceptions.Errot;
import com.robonobo.common.io.PeekableInputStream;

/**
 * For encoding longs as a variable-length sequence of bytes ; see {project}/dox/dlugosz.html
 * 
 * @author macavity
 */
public class Dlugosz {
	private final static long[] byteThresholds = { 127L, 16383L, 2097151L, 134217727L, 34359738367L, 1099511627775L,
			1099511627775L, 1099511627775L, Long.MAX_VALUE };

	/**
	 * Encoded number will be written to the buffer at its current position
	 * 
	 * @throws NumberFormatException
	 *             If num < 0
	 */
	public static void encode(long num, ByteBuffer dst) throws NumberFormatException {
		if (num < 0)
			throw new NumberFormatException("num must be positive");
		encode(num, bytesToEncode(num), dst);
	}

	/**
	 * New bytebuffer will be returned containing just the encoded number, with the position set to 0
	 */
	public static ByteBuffer encode(long num) throws NumberFormatException {
		ByteBuffer result = ByteBuffer.allocate(bytesToEncode(num));
		encode(num, result);
		result.position(0);
		return result;
	}

	public static int bytesToEncode(long num) {
		if (num < 0)
			throw new NumberFormatException("num must be positive");
		for (int i = 0; i < byteThresholds.length; i++) {
			if (num <= byteThresholds[i])
				return (i + 1);
		}
		// Can't happen
		throw new Errot();
	}

	private static void encode(long num, int encLen, ByteBuffer buf) {
		// This could be reduced in size with a decrementing for loop, but I
		// think this way is clearer
		switch (encLen) {
		case 1:
			buf.put(byteAtOffset(num, 0));
			break;
		case 2:
			buf.put((byte) ((2 << 6) | byteAtOffset(num, 8)));
			buf.put(byteAtOffset(num, 0));
			break;
		case 3:
			buf.put((byte) ((6 << 5) | byteAtOffset(num, 16)));
			buf.put(byteAtOffset(num, 8));
			buf.put(byteAtOffset(num, 0));
			break;
		case 4:
			buf.put((byte) (((byte) (28 << 3)) | byteAtOffset(num, 24)));
			buf.put(byteAtOffset(num, 16));
			buf.put(byteAtOffset(num, 8));
			buf.put(byteAtOffset(num, 0));
			break;
		case 5:
			buf.put((byte) ((29 << 3) | byteAtOffset(num, 32)));
			buf.put(byteAtOffset(num, 24));
			buf.put(byteAtOffset(num, 16));
			buf.put(byteAtOffset(num, 8));
			buf.put(byteAtOffset(num, 0));
			break;
		case 6:
			buf.put((byte) (248 & 0xff));
			buf.put(byteAtOffset(num, 32));
			buf.put(byteAtOffset(num, 24));
			buf.put(byteAtOffset(num, 16));
			buf.put(byteAtOffset(num, 8));
			buf.put(byteAtOffset(num, 0));
			break;
		case 9:
			buf.put((byte) (249 & 0xff));
			buf.put(byteAtOffset(num, 56));
			buf.put(byteAtOffset(num, 48));
			buf.put(byteAtOffset(num, 40));
			buf.put(byteAtOffset(num, 32));
			buf.put(byteAtOffset(num, 24));
			buf.put(byteAtOffset(num, 16));
			buf.put(byteAtOffset(num, 8));
			buf.put(byteAtOffset(num, 0));
			break;
		default: // Can't happen
			throw new Errot();
		}
	}

	/**
	 * Reads one long value from the channel. Increments the channel's position by the number of bytes used by the
	 * returned value.
	 */
	public static long readLong(ByteChannel chan) throws IOException {
		ByteBuffer fbBuf = ByteBuffer.allocate(1);
		chan.read(fbBuf);

		// prepare to read the same byte
		fbBuf.flip();
		byte firstByte = fbBuf.get();
		int encLen = encLenFromFirstByte(firstByte);

		// rewind for reading again
		fbBuf.flip();
		if (encLen == 1)
			return readLong(fbBuf);

		ByteBuffer buf = ByteBuffer.allocate(encLen);
		buf.put(fbBuf);
		chan.read(buf);
		buf.flip();
		return readLong(buf);
	}

	/**
	 * Returns true iff the supplied buffer contains a complete dlugosz num, starting at the buffer's current position.
	 * The buffer's position is left unchanged.
	 */
	public static boolean startsWithCompleteNum(ByteBuffer buf) {
		if (buf.remaining() == 0)
			return false;
		int origPos = buf.position();
		byte firstByte = buf.get();
		int encLen = encLenFromFirstByte(firstByte);
		buf.position(origPos);
		return (buf.remaining() >= encLen);
	}

	/**
	 * Returns true iff the supplied stream contains a complete dlugosz num. The stream's position is left unchanged.
	 */
	public static boolean startsWithCompleteNum(PeekableInputStream is) throws IOException {
		if (is.available() == 0)
			return false;
		byte firstByte = (byte) is.peek();
		int encLen = encLenFromFirstByte(firstByte);
		return (is.available() >= encLen);
	}

	/**
	 * Reads one long value from the buffer. Increments the buffer's position by the number of bytes used by the
	 * returned value.
	 */
	public static long readLong(ByteBuffer buf) throws IOException {
		byte firstByte = buf.get();
		long result = 0;
		int encLen = encLenFromFirstByte(firstByte);
		switch (encLen) {
		case 1:
			result = offsetByteAsLong(firstByte, 0);
			break;
		case 2:
			result = offsetByteAsLong((byte) (firstByte & 63), 8);
			result += offsetByteAsLong(buf.get(), 0);
			break;
		case 3:
			result = offsetByteAsLong((byte) (firstByte & 31), 16);
			result += offsetByteAsLong(buf.get(), 8);
			result += offsetByteAsLong(buf.get(), 0);
			break;
		case 4:
			result = offsetByteAsLong((byte) (firstByte & 7), 24);
			for (int i = 0; i < 3; i++) {
				result += offsetByteAsLong(buf.get(), (2 - i) * 8);
			}
			break;
		case 5:
			result = offsetByteAsLong((byte) (firstByte & 7), 32);
			for (int i = 0; i < 4; i++) {
				result += offsetByteAsLong(buf.get(), (3 - i) * 8);
			}
			break;
		case 6:
			for (int i = 0; i < 5; i++) {
				result += offsetByteAsLong(buf.get(), (4 - i) * 8);
			}
			break;
		case 9:
			for (int i = 0; i < 8; i++) {
				result += offsetByteAsLong(buf.get(), (7 - i) * 8);
			}
			break;
		default: // Can't happen
			throw new Errot();
		}

		return result;
	}

	/**
	 * Reads one long value from the buffer. Increments the buffer's position by the number of bytes used by the
	 * returned value.
	 */
	public static long readLong(PeekableInputStream is) throws IOException {
		byte firstByte = readByte(is);
		long result = 0;
		int encLen = encLenFromFirstByte(firstByte);
		switch (encLen) {
		case 1:
			result = offsetByteAsLong(firstByte, 0);
			break;
		case 2:
			result = offsetByteAsLong((byte) (firstByte & 63), 8);
			result += offsetByteAsLong(readByte(is), 0);
			break;
		case 3:
			result = offsetByteAsLong((byte) (firstByte & 31), 16);
			result += offsetByteAsLong(readByte(is), 8);
			result += offsetByteAsLong(readByte(is), 0);
			break;
		case 4:
			result = offsetByteAsLong((byte) (firstByte & 7), 24);
			for (int i = 0; i < 3; i++) {
				result += offsetByteAsLong(readByte(is), (2 - i) * 8);
			}
			break;
		case 5:
			result = offsetByteAsLong((byte) (firstByte & 7), 32);
			for (int i = 0; i < 4; i++) {
				result += offsetByteAsLong(readByte(is), (3 - i) * 8);
			}
			break;
		case 6:
			for (int i = 0; i < 5; i++) {
				result += offsetByteAsLong(readByte(is), (4 - i) * 8);
			}
			break;
		case 9:
			for (int i = 0; i < 8; i++) {
				result += offsetByteAsLong(readByte(is), (7 - i) * 8);
			}
			break;
		default: // Can't happen
			throw new Errot();
		}

		return result;
	}

	private static byte readByte(PeekableInputStream is) throws IOException {
		return (byte) is.read();
	}

	private static int encLenFromFirstByte(byte firstByte) {
		if (((firstByte >> 7) & 1) == 0) {
			return 1;
		} else if (((firstByte >> 6) & 1) == 0) {
			return 2;
		} else if (((firstByte >> 5) & 1) == 0) {
			return 3;
		} else if (((firstByte >> 3) & 3) == 0) {
			return 4;
		} else if (((firstByte >> 4) & 1) == 0) {
			return 5;
		} else if ((firstByte & 7) == 0) {
			return 6;
		} else if (firstByte == (byte) (249 & 0xff)) {
			return 9;
		} else
			throw new NumberFormatException("Found invalid first byte " + (firstByte & 0xff));
	}

	private static byte byteAtOffset(long src, int bitOffset) {
		return (byte) ((src >> bitOffset) & 0xff);
	}

	private static long offsetByteAsLong(byte b, int bitOffset) {
		return ((long) (b & 0xff)) << bitOffset;
	}
	//
	// private static String byteAsBits(byte b) {
	// StringBuffer sb = new StringBuffer();
	// for(int i=7;i>=0;i--) {
	// if(((b >> i) & 1) > 0)
	// sb.append("1");
	// else
	// sb.append("0");
	// }
	// return sb.toString();
	// }

	// public static void main(String[] args) throws Exception {
	// long[] testNums = { 1, 2,
	// 126, 127, 128,
	// 16382, 16383, 16384,
	// 2097150, 2097151, 2097152,
	// 134217726, 134217727, 134217728,
	// 34359738366L, 34359738367L, 34359738368L,
	// 1099511627774L, 1099511627775L, 1099511627776L,
	// Long.MAX_VALUE -1, Long.MAX_VALUE };
	// int[] sizes = {1, 1,
	// 1, 1, 2,
	// 2, 2, 3,
	// 3, 3, 4,
	// 4, 4, 5,
	// 5, 5, 6,
	// 6, 6, 9,
	// 9, 9 };
	// for (int i = 0; i < testNums.length; i++) {
	// long testNum = testNums[i];
	// int expectedSize = sizes[i];
	// ByteBuffer result = encode(testNum);
	// if(result.capacity() != expectedSize)
	// throw new RuntimeException("Failed: encoded wrong number of bytes");
	// FileChannel fc = new RandomAccessFile("c:\\macavity\\temp\\flarp.rand",
	// "rw").getChannel();
	// result.flip();
	// fc.write(result);
	// fc.position(0);
	// long inverse = readLong(fc);
	// if(fc.position() != result.capacity())
	// throw new RuntimeException("Failed: read wrong number of bytes");
	// if(inverse != testNum)
	// throw new RuntimeException("Failed");
	// fc.close();
	// }
	// System.out.println("Success");
	// }
}
