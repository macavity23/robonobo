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
 * Placeholder for future work.
 * 
 * @author jdp
 */
public class oaep
    implements coder.transform
{
    public final static java.lang.String NAME = "base64";

    /**
     * <p> Decode stream from OAEP AONT container. </p>
     * 
     */
    public static class dec
	extends fii
	implements consumer
    {

	public dec( java.io.InputStream in){
	    super(in);
	}
	public dec( gnu.iou.bbuf buf){
	    super(buf);
	}

    }

    /**
     * <p> Encode stream to OAEP AONT container. </p>
     * 
     */
    public static class enc
	extends foo
	implements producer
    {

	public enc( java.io.OutputStream out){
	    super(out);
	}
	public enc( gnu.iou.bbuf buf){
	    super(buf);
	}

    }

    public oaep(){
	super();
    }
    public java.lang.String getName(){
	return NAME;
    }
    public fii decoder( java.io.InputStream in){
	return new dec(in);
    }
    public fii decoder( gnu.iou.bbuf buf){
	return new dec(buf);
    }
    public foo encoder( java.io.OutputStream out){
	return new enc(out);
    }
    public foo encoder( gnu.iou.bbuf buf){
	return new enc(buf);
    }

}
