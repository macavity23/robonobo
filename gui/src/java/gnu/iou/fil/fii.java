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
 * Filter input stream with {@link gnu.iou.bbi BBI} extensions
 * available for BBI input streams only.  
 * 
 * @author John Pritchard (jdp@syntelos.com)
 * 
 * @see gnu.iou.bbi
 * @see foo
 */
public class fii 
    extends java.io.FilterInputStream 
{

    private final boolean have_bbi;

    private gnu.iou.bbi bbi;

    private boolean unread_a = false, unread_b = true;

    private int unread_c;

    public fii( java.io.InputStream in){
	super(in);
	if (in instanceof gnu.iou.bbi){
	    this.have_bbi = true;
	    this.bbi = (gnu.iou.bbi)in;
	}
	else if (in instanceof fii){
	    fii in_fii = (fii)in;
	    this.have_bbi = in_fii.haveBBI();
	    if (this.have_bbi)
		this.bbi = in_fii.getBBI();
	}
	else {
	    this.have_bbi = false;
	}
    }
    public fii( gnu.iou.bbuf buf){
	this(new gnu.iou.bbi(buf));
    }

    /**
     * @return Whether the BBI dependent methods will work without
     * throwing exceptions, whether this object has a non null BBI.
     */
    public final boolean haveBBI(){
	return this.have_bbi;
    }
    /**
     * @return A non null BBI, or exception
     * @exception java.lang.IllegalStateException For no BBI
     * @see #haveBBI()
     */
    public final gnu.iou.bbi getBBI(){
	if (this.have_bbi)
	    return this.bbi;
	else
	    throw new java.lang.IllegalStateException();
    }
    /**
     * @return The filter input stream underlying source stream
     */
    public final java.io.InputStream getIn(){
	return super.in;
    }
    /**
     * @return BBI offset of the last byte read, or negative one before
     * the first byte has been read
     * @exception java.lang.IllegalStateException For no BBI
     * @see #haveBBI()
     */
    public int offset(){
	if (this.have_bbi)
	    return this.bbi.offset();
	else
	    throw new java.lang.IllegalStateException();
    }
    /**
     * @return BBI Buffer
     * @exception java.lang.IllegalStateException For no BBI
     * @see #haveBBI()
     */
    public gnu.iou.bbuf getByteBuffer(){ 
	if (this.have_bbi)
	    return this.bbi.getByteBuffer();
	else
	    throw new java.lang.IllegalStateException();
    }
    /**
     * @return BBI buffer, null with no BBI
     * @see #haveBBI()
     */
    public byte[] toByteArray(){ 
	if (this.have_bbi)
	    return this.bbi.toByteArray();
	else
	    return null;
    }
    /**
     * @return BBI buffer decoded from UTF8, null with no BBI
     * @see #haveBBI()
     */
    public String toString(){ 
	if (this.have_bbi)
	    return this.bbi.toString();
	else
	    return null;
    }
    /**
     * BBI buffer reset, no op without BBI
     * @see #haveBBI()
     */
    public void reset() 
	throws java.io.IOException 
    {
	if (this.have_bbi)
	    this.bbi.reset();
    }
    /**
     * @return BBI marked buffer, null with no BBI
     * @see #haveBBI()
     */
    public byte[] marked(){
	if (this.have_bbi)
	    return this.bbi.marked();
	else
	    return null;
    }
    public void mark(int cap){
	if (this.have_bbi)
	    this.bbi.mark(cap);
	else
	    super.mark(cap);
    }
    public boolean markSupported(){
	if (this.have_bbi)
	    return this.bbi.markSupported();
	else
	    return super.markSupported();
    }
    public int read()
	throws java.io.IOException
    {
	if (this.have_bbi)
	    return this.bbi.read();
	else if (this.unread_a){
	    this.unread_a = false;
	    this.unread_b = true;
	    return this.unread_c;
	}
	else
	    return super.read();
    }
    /**
     * @param ch last char read, only unread once per read cycle
     */
    public void unread(int ch){
	if (this.have_bbi)
	    this.bbi.unread(ch);
	else if (this.unread_b){
	    this.unread_a = false;
	    this.unread_b = true;
	    this.unread_c = ch;
	}
	else
	    throw new java.lang.IllegalStateException("Can only unread one byte.");
    }
}
