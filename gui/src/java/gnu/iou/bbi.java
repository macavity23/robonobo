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
package gnu.iou ;

import java.io.IOException;
import java.io.InputStream;

/**
 * Wrapper for <tt>`bbuf'</tt> exporting standard input API.
 * 
 * @author John Pritchard (john@syntelos.org)
 * 
 * @see bbuf
 * @see bbo
 */
public class bbi extends InputStream {

    private bbuf buf ;

    public bbi( int capacity){
	super();
	buf = new bbuf(capacity);
    }
    public bbi(){
	super();
	buf = new bbuf();
    }
    public bbi ( bbuf buf){
	super();
	if ( null == buf)
	    throw new IllegalArgumentException("Null or empty input for `bbo'.");
	else
	    this.buf = buf;
    }
    public bbi ( byte[] src){
	super();

	if ( null == src)
	    throw new IllegalArgumentException("Null or empty input for `bbo'.");
	else
	    this.buf = new bbuf(src);
    }
    /**
     * Read `many' bytes from the input stream `src' into this buffer.
     */
    public bbi ( InputStream src, int many) throws IOException {
	super();

	byte[] bbuffer = new byte[many];

	for ( int cc = 0; cc < many; cc++){

	    bbuffer[cc] = (byte)(src.read()&0xff);
	}

	this.buf = new bbuf(bbuffer);
    }

    // bbi

    /**
     * @return Offset of the last byte read, or negative one before
     * the first byte has been read
     */
    public int offset(){
	return this.buf.offset_read();
    }

    public bbuf getByteBuffer(){ return buf;}

    public byte[] toByteArray(){ return buf.toByteArray();}

    public String toString(){ return buf.toString();}

    /**
     * @param ch last char read, only unread once per read cycle
     */
    public void unread(int ch){
	this.buf.unread();
    }
    // InputStream 

    public int read() throws IOException {
	return buf.read();
    }
    public int read(byte b[]) throws IOException {
	return buf.read(b, 0, b.length);
    }
    public int read( byte[] b, int ofs, int len) throws IOException {
	return buf.read(b, ofs, len);
    }
    public java.lang.String readLine() throws IOException {
	return buf.readLine();
    }
    public int available() throws IOException {
	return buf.available();
    }
    public long skip(long n) throws IOException { 
	if (0L < n && n < Integer.MAX_VALUE)
	    return buf.skip_read( (int)n);
	else if (0L != n)
	    throw new IllegalArgumentException(String.valueOf(n));
	else
	    return 0L;
    }
    public void close() throws IOException { 
	buf.close();
    }
    public void mark(int m){ 
	buf.mark_read();
    }
    public void reset() throws IOException { 
	buf.flush();
    }
    public boolean markSupported() { 
	return true;
    }
    public byte[] marked(){
	return buf.marked_read();
    }
}
