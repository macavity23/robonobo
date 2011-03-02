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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Debugging <tt>`bbo'</tt> copies writes to file.
 * 
 * @author John Pritchard (john@syntelos.org)
 * 
 * @see bbo
 */
public class dbo extends bbo {

    protected PrintStream debugCopy = null;

    protected boolean debugCopyTrace = false;

    /**
     * Debug mode, tee this stream into a file.
     * 
     * @param debugCopyFilename Copy everything on this stream into this file
     * 
     * @param trace If true, print stack traces before each method invocation.
     * 
     * @exception IOException Construction file output stream.
     */
    public dbo ( String debugCopyFilename, boolean trace) throws IOException {
	super();
	if ( null == debugCopyFilename)
	    throw new IllegalArgumentException("Debug mode constructor missing filename argument.");
	else
	    this.debugCopy = new PrintStream(new FileOutputStream(debugCopyFilename));

	this.debugCopyTrace = trace;
    }

    protected void trace(){
	debugCopy.println();
	debugCopy.println();
	debugCopy.println(bpo.stackTrace());
    }

    // OutputStream

    public void write ( int b) throws IOException {
	super.write(b);
	if ( null != this.debugCopy){
	    if (this.debugCopyTrace)
		trace();
	    debugCopy.write(b);
	}
    }
    public void write ( byte[] b) throws IOException {
	super.write(b, 0, b.length);
	if ( null != this.debugCopy){
	    if (this.debugCopyTrace)
		trace();
	    debugCopy.write(b,0,b.length);
	}
    }
    public void write ( byte[] b, int ofs, int len) throws IOException {
	super.write(b, ofs, len);
	if ( null != this.debugCopy){
	    if (this.debugCopyTrace)
		trace();
	    debugCopy.write(b,ofs,len);
	}
    }
    public void flush() throws IOException {
	if ( null != this.debugCopy){
	    if (this.debugCopyTrace)
		trace();
	}
    }
    public void close() throws IOException {
	if ( null != this.debugCopy){

	    if (this.debugCopyTrace)
		trace();

	    this.debugCopy .close();

	    this.debugCopy = null;
	}
    }

    // bbuf

    public bbuf getByteBuffer(){ 
	if ( null != this.debugCopy && this.debugCopyTrace)
	    trace();

	return super.getByteBuffer();
    }

    public int length(){ return super.length();}

    public void reset(){ 
	super.reset();

	if ( null != this.debugCopy && this.debugCopyTrace)
	    trace();
    }

    public byte[] toByteArray(){ 
	if ( null != this.debugCopy && this.debugCopyTrace)
	    trace();

	return super.toByteArray();
    }
}
