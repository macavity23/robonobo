 /* 
  *  `gnu.iou' 
  *  Copyright (C) 2006 John Pritchard.
  *
  *  This program is free software; you can redistribute it and/or
  *  modify it under the terms of the GNU General Public License as
  *  published by the Free Software Foundation; either version 2 of
  *  the License, or (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  *  General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program; if not, write to the Free Software
  *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA
  *  02111-1307 USA
  */

package gnu.iou.fil ;

/**
 *
 * 
 * @author John Pritchard (jdp@syntelos.com)
 * 
 * @see gnu.iou.bbi
 * @see fio
 */
public class fid
    extends fii
    implements java.io.DataInput
{


    public fid( java.io.InputStream in){
	super(in);
    }
    public fid( gnu.iou.bbuf buf){
	super(buf);
    }

    public final void readFully(byte b[]) 
	throws java.io.IOException 
    {
	this.readFully(b, 0, b.length);
    }
    public final void readFully(byte b[], int off, int len) 
	throws java.io.IOException 
    {
	if (len > this.read(b, 0, b.length))
	    throw new java.io.EOFException();
	else
	    return;
    }
    public final int skipBytes(int n) 
	throws java.io.IOException 
    {
	return (int)this.skip(n);
    }
    public final boolean readBoolean() 
	throws java.io.IOException 
    {
	int a = this.read();
	if (0 > a)
	    throw new java.io.EOFException();
	else
	    return (a != 0);
    }
    public final byte readByte() 
	throws java.io.IOException 
    {
	int a = this.read();
	if (0 > a)
	    throw new java.io.EOFException();
	return (byte)(a);
    }
    public final int readUnsignedByte() 
	throws java.io.IOException 
    {
	int a = this.read();
	if (0 > a)
	    throw new java.io.EOFException();
	else
	    return a;
    }
    public final short readShort() 
	throws java.io.IOException 
    {
	return (short)this.readUnsignedShort();
    }
    public final int readUnsignedShort() 
	throws java.io.IOException 
    {
        int a = this.read();
	if (0 > a)
	    throw new java.io.EOFException();
	else {
	    int b = this.read();
	    if (0 > b)
		throw new java.io.EOFException();
	    else {
		int re = (a << 8)+(b);
		return re;
	    }
	}
    }
    public final char readChar() 
	throws java.io.IOException 
    {
        return (char)this.readUnsignedShort();
    }
    public final int readInt() 
	throws java.io.IOException 
    {
        int a = this.read();
	if (0 > a)
	    throw new java.io.EOFException();
	else {
	    int b = this.read();
	    if (0 > b)
		throw new java.io.EOFException();
	    else {
		int c = this.read();
		if (0 > c)
		    throw new java.io.EOFException();
		else {
		    int d = this.read();
		    if (0 > d)
			throw new java.io.EOFException();
		    else {
			int re = (a << 24)+(b << 16)+(c << 8)+(d);
			return re;
		    }
		}
	    }
	}
    }
    public final long readLong() 
	throws java.io.IOException 
    {
        long a = this.read();
	if (0L > a)
	    throw new java.io.EOFException();
	else {
	    long b = this.read();
	    if (0L > b)
		throw new java.io.EOFException();
	    else {
		long c = this.read();
		if (0L > c)
		    throw new java.io.EOFException();
		else {
		    long d = this.read();
		    if (0L > d)
			throw new java.io.EOFException();
		    else {
			long e = this.read();
			if (0L > e)
			    throw new java.io.EOFException();
			else {
			    long f = this.read();
			    if (0L > f)
				throw new java.io.EOFException();
			    else {
				long g = this.read();
				if (0L > g)
				    throw new java.io.EOFException();
				else {
				    long h = this.read();
				    if (0L > h)
					throw new java.io.EOFException();
				    else {
					long re = (a << 56)+(b << 48)+(c << 40)+(d << 32)+(e << 24)+(f << 16)+(g << 8)+(h);
					return re;
				    }
				}
			    }
			}
		    }
		}
	    }
	}
    }
    public final float readFloat() 
	throws java.io.IOException 
    {
	int bits = this.readInt();
	return java.lang.Float.intBitsToFloat(bits);
    }
    public final double readDouble() 
	throws java.io.IOException 
    {
	long bits = this.readLong();
	return java.lang.Double.longBitsToDouble(bits);
    }
    public final java.lang.String readLine() 
	throws java.io.IOException 
    {
	if (this.haveBBI())
	    return this.getBBI().readLine();
	else {
	    gnu.iou.bbuf line = new gnu.iou.bbuf();
	    int ch;
	    readl:
	    while (true) {
		switch (ch = this.read()) {
		case -1:
		case '\n':
		    break readl;
		case '\r':
		    switch (ch = this.read()){
		    case -1:
			break readl;
		    case '\n':
			break readl;
		    default:
			this.unread(ch);
			break readl;
		    }
		    //break readl;//(unreachable)
		default:
		    line.write(ch);
		    break;
		}
	    }
	    byte[] lbits = line.toByteArray();
	    if (null == lbits)
		return null;
	    else {
		char[] cary = gnu.iou.utf8.decode(lbits);
		return new java.lang.String(cary);
	    }
	}
    }
    public final java.lang.String readUTF() 
	throws java.io.IOException 
    {
        return java.io.DataInputStream.readUTF(this);
    }

}
