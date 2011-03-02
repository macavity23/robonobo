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
 * @see gnu.iou.bbo
 * @see fii
 */
public class fod
    extends foo
    implements java.io.DataOutput
{
    private byte[] writebuf;

    public fod( java.io.OutputStream out){
	super(out);
    }
    public fod( gnu.iou.bbuf buf){
	super(buf);
    }

    public final void writeBoolean(boolean value) throws java.io.IOException {
	int ch = (value)?(1):(0);
	this.write(ch);
    }

    public final void writeByte(int value) throws java.io.IOException {
	this.write(value);
    }

    public final void writeShort(int value) throws java.io.IOException {
	if (this.haveBBO()){
	    gnu.iou.bbuf store = this.getByteBuffer();
	    store.write2(value);
	}
	else {
	    byte[] buf = this.writebuf;
	    if (null == buf){
		this.writebuf = new byte[8];
		buf = this.writebuf;
	    }
	    buf[0] = (byte)((value >>> 8) & 0xff);
	    buf[1] = (byte)(value & 0xff);
	    this.write(buf,0,2);
	}
    }

    public final void writeChar(int value) throws java.io.IOException {
	this.writeShort(value);
    }

    public final void writeInt(int value) throws java.io.IOException {
	if (this.haveBBO()){
	    gnu.iou.bbuf store = this.getByteBuffer();
	    store.write4(value);
	}
	else {
	    byte[] buf = this.writebuf;
	    if (null == buf){
		this.writebuf = new byte[8];
		buf = this.writebuf;
	    }
	    buf[0] = (byte)((value >>> 24) & 0xff);
	    buf[1] = (byte)((value >>> 16) & 0xff);
	    buf[2] = (byte)((value >>>  8) & 0xff);
	    buf[3] = (byte)(value & 0xff);
	    this.write(buf,0,4);
	}
    }

    public final void writeLong(long value) throws java.io.IOException {
	if (this.haveBBO()){
	    gnu.iou.bbuf store = this.getByteBuffer();
	    store.write8(value);
	}
	else {
	    byte[] buf = this.writebuf;
	    if (null == buf){
		this.writebuf = new byte[8];
		buf = this.writebuf;
	    }
	    buf[0] = (byte)((value >>> 56) & 0xff);
	    buf[1] = (byte)((value >>> 48) & 0xff);
	    buf[2] = (byte)((value >>> 40) & 0xff);
	    buf[3] = (byte)((value >>> 32) & 0xff);
	    buf[4] = (byte)((value >>> 24) & 0xff);
	    buf[5] = (byte)((value >>> 16) & 0xff);
	    buf[6] = (byte)((value >>>  8) & 0xff);
	    buf[7] = (byte)(value & 0xff);
	    this.write(buf,0,8);
	}
    }

    public final void writeFloat(float value) throws java.io.IOException {
	this.writeInt(java.lang.Float.floatToIntBits(value));
    }

    public final void writeDouble(double value) throws java.io.IOException {
	this.writeLong(java.lang.Double.doubleToLongBits(value));
    }

    public final void writeBytes(String str) throws java.io.IOException {
	char[] cary = str.toCharArray();
        int cary_len = (null != cary)?(cary.length):(0);
	byte ch;
        for (int cc = 0 ; cc < cary_len ; cc++) {
	    ch = (byte)cary[cc];
            this.write(ch);
        }
    }

    public final void writeChars(String str) throws java.io.IOException {
	char[] cary = str.toCharArray();
        int cary_len = (null != cary)?(cary.length):(0), ch;
        for (int cc = 0 ; cc < cary_len ; cc++) {
	    ch = cary[cc];
            this.write( (ch >>> 8) & 0xff);
            this.write( ch & 0xff);
        }
    }

    public final void writeUTF(String str) throws java.io.IOException {
	byte[] utf = gnu.iou.utf8.encode(str);
	int utf_len = (null != utf)?(utf.length):(0);
	this.writeShort(utf_len);
	this.write(utf);
    }

}
