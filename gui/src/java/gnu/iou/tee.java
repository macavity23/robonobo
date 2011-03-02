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
import java.io.OutputStream;

/**
 * Output stream <tt>`tee'</tt>.
 *
 * @author John Pritchard (john@syntelos.org)
 * 
 * @see pte
 */
public class tee extends OutputStream {



    protected volatile OutputStream fir ;

    protected volatile OutputStream sec ;

    protected volatile OutputStream thi ;

    public tee ( OutputStream first, OutputStream second ){
	super();

	if ( null == first) 
	    throw new IllegalArgumentException("Constructing `tee(2)' with one stream?");
	else
	    this.fir = first;
	

	if ( null == second) 
	    throw new IllegalArgumentException("Constructing `tee(2)' with one stream?");
	else
	    this.sec = second;
    }
    public tee ( tee first, OutputStream third ){
	this(first.fir,first.sec);

	if ( null == third) 
	    throw new IllegalArgumentException("Constructing `tee(3)' with two streams?");
	else
	    this.thi = third;
    }



    public OutputStream teeOrig(){ 

	if ( this.fir instanceof tee)

	    return ((tee)this.fir).teeOrig();
	else
	    return fir;
    }

    public boolean teeFull(){ return (null != this.thi);}

    public boolean teeEmpty(){ return (null == this.sec);}

    protected void teeAdd( OutputStream ps){

	if ( null != ps){

	    if ( null == this.sec)

		this.sec = ps;

	    else if ( null == this.thi)

		this.thi = ps;

	    else if ( this.thi instanceof tee)

		((tee)this.thi).teeAdd(ps);

	    else {
		tee t = new tee( this.thi, ps);

		this.thi = t;
	    }
	}
    }
    /**
     * @exception IllegalStateException Attempting to remove first
     * stream from tee (ie, when tee is empty -- only one stream in
     * it).  */
    protected void teeRemove( OutputStream ps){

	if ( null != ps){

	    if ( ps == this.thi)

		this.thi = null;

	    else if ( ps == this.sec){

		if ( null != this.thi){

		    OutputStream t = this.thi;

		    this.thi = null;

		    this.sec = t;
		}
		else 
		    this.sec = null;
	    }
	    else if ( ps == this.fir){

		if ( null != this.thi){

		    OutputStream t = this.thi;

		    this.thi = null;

		    OutputStream s = this.sec;

		    this.sec = t;

		    this.fir = s;
		}
		else if ( null != this.sec){

		    OutputStream s = this.sec;

		    this.sec = null;

		    this.fir = this.sec;
		}
		else
		    throw new IllegalStateException("Removing root from tee.");
	    }
	    else if ( this.thi instanceof tee){

		try {
		    ((tee)this.thi).teeRemove(ps);
		}
		catch ( IllegalStateException mmt){

		    this.thi = null; // is empty
		}
	    }
	}
    }

    public void write ( int b) throws IOException {
	fir.write(b);
	if ( null != sec)
	    sec.write(b);
	if ( null != thi)
	    thi.write(b);
    }
    public void write ( byte[] b)  throws IOException {
	this.write(b,0,b.length);
    }
    public void write ( byte[] b, int ofs, int len)  throws IOException {
	fir.write(b, ofs, len);
	if ( null != sec)
	    sec.write(b, ofs, len);
	if ( null != thi)
	    thi.write(b, ofs, len);
    }
    public void flush() throws IOException {
	fir.flush();
	if ( null != sec)
	    sec.flush();
	if ( null != thi)
	    thi.flush();
    }
    /**
     * @exception IllegalStateException Can't use `close()' on a tee.
     */
    public void close() throws IOException {
	throw new IllegalStateException("Close not available on output tee.");
    }

}
