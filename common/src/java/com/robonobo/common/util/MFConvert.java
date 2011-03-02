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


/*
 * This source code file is Copyright 2003-2004 Ray Hilton / Will Morton / Rana Singh. All rights reserved.
 * Unauthorised duplication of this file is expressly forbidden without prior
 * written permission.
 */
public class MFConvert
{
	private MFConvert()
	{
	}

	public static String toString(byte[] arr) throws IllegalArgumentException
	{
		return toString(arr, 0, arr.length);
	}

	public static String toString(byte[] arr, int length)
			throws IllegalArgumentException
	{
		return toString(arr, 0, length);
	}

	public static String toString(byte[] arr, int start, int length)
			throws IllegalArgumentException
	{
		if ((start + length) > arr.length)
		{
			throw new IllegalArgumentException(
					"Specified length is longer than supplied array");
		}
		if (length < 0)
		{
			throw new IllegalArgumentException("Supplied length is negative");
		}

		char[] ca = new char[length];
		for (int i = start; i < length; i++)
		{
			ca[i] = (char) arr[i];
		}

		return new String(ca);
	}

	public static byte[] toByteArray(char[] arr)
	{
		byte[] ba = new byte[arr.length];
		for (int i = 0; i < arr.length; i++)
		{
			ba[i] = (byte) arr[i];
		}

		return ba;
	}

	public static long longFromLittleEndianByteArray(byte[] buffer, int firstPos)
	{
		if (firstPos + 8 > buffer.length)
			throw new IllegalArgumentException("buffer is not big enough");

		// c++ QWORD == Java long == 64bit (signed)

		long result = 0;
		for(int i=0;i<8;i++)
		{
			int shiftBy = i*8;
			result |= (long)( buffer[firstPos+i] & 0xff ) << shiftBy; 
		}

		return result;
	}
		

	public static void writeLongToLittleEndianByteArray(long val, byte[] buffer, int start)
	{
		for(int i=0;i<8;i++)
		{
			int shiftBy = i*8;
			buffer[start+i] = (byte)((val >> shiftBy) & 0xff);
		}
	}

	public static int intFromLittleEndianByteArray(byte[] buffer, int firstPos)
	{
		if (firstPos + 4 > buffer.length)
			throw new IllegalArgumentException("buffer is not big enough");

		// c++ DWORD == Java int == 32bit (signed)

		int result = 0;
		for(int i=0;i<4;i++)
		{
			int shiftBy = i*8;
			result |= ( buffer[firstPos+i] & 0xff ) << shiftBy; 
		}

		return result;
	}
		
	public static void writeIntToLittleEndianByteArray(int val, byte[] buffer, int start)
	{
		for(int i=0;i<4;i++)
		{
			int shiftBy = i*8;
			buffer[start+i] = (byte)((val >> shiftBy) & 0xff);
		}
	}

	public static short shortFromLittleEndianByteArray(byte[] buffer, int firstPos)
	{
		if (firstPos + 2 > buffer.length)
			throw new IllegalArgumentException("buffer is not big enough");

		// c++ WORD == Java short == 16bit (signed)
		
		int low = buffer[firstPos] & 0xff;
		int high = buffer[firstPos+1] & 0xff;
		
		return (short)((high << 8) | low);
	}
	   
	public static void writeShortToLittleEndianByteArray(short val, byte[] buffer, int start)
	{
		for(int i=0;i<2;i++)
		{
			int shiftBy = i*8;
			buffer[start+i] = (byte)((val >> shiftBy) & 0xff);
		}
	}

	public static String byteAsBits(byte b)
	{
		StringBuffer sb = new StringBuffer();
		for(int i=0;i<8;i++)
		{
			sb.append((b >> i) & 0x1);
		}
		
		return sb.toString();
	}
	
    public static byte[] intToByteArray(int value) {
        byte[] b = new byte[4];
        for (int i = 0; i < 4; i++) {
            int offset = (b.length - 1 - i) * 8;
            b[i] = (byte) ((value >>> offset) & 0xFF);
        }
        return b;
    }
    
    /**
     * Convert the byte array to an int.
     *
     * @param b The byte array
     * @return The integer
     */
    public static int byteArrayToInt(byte[] b) {
        return byteArrayToInt(b, 0);
    }

    /**
     * Convert the byte array to an int starting from the given offset.
     *
     * @param b The byte array
     * @param offset The array offset
     * @return The integer
     */
    public static int byteArrayToInt(byte[] b, int offset) {
        int value = 0;
        for (int i = 0; i < 4; i++) {
            value|= (b[i + offset] & 0x000000FF);
            if(i<3) value<<=8;
        }
        return value;
    }

}