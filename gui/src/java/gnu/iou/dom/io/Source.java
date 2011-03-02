 /* 
  *  `gnu.iou.dom'
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
package gnu.iou.dom.io;

/**
 * <p> An I/O source is an object systemically responsible to the DOM
 * for its input.  It is the DOM's perspective of its I/O device.
 * </p>
 * 
 * @see gnu.iou.dom.Document
 * 
 * @author jdp
 */
public interface Source {

    /**
     * <p> A byte source is a system device from which the DOM
     * document was read.  For example a file or http device. </p>
     * 
     * <p> In a large and common class of DOM related systems, this
     * device also maintains a reference (java object) to the DOM
     * Document as a DOM cache. </p>
     * 
     * @see gnu.iou.dom.Document
     */
    public interface Byte extends Source {

	/**
	 * <p> The device source is a distinct method name from the
	 * source of a node, as a device itself could be a node.  In
	 * this case (at least useful in the analysis of this API) its
	 * own document source is distinct from the device source it
	 * is working on. </p>
	 * 
	 * @return A URI System Id string
	 * @see #getSystemId2()
	 */
	public String getSystemIdSource();

	/**
	 * @return A parsed URI System Id 
	 * @see #getSystemId()
	 */
	public gnu.iou.uri getSystemIdSource2();

    }

    /**
     * <p> A character source is a character data node which content
     * is being read in stream order. </p>
     *
     * @see gnu.iou.dom.CharacterData
     */
    public interface Char extends Source {

	public class Reader 
	    extends gnu.iou.bbi
	{
	    protected Source.Char source;

	    /**
	     * @param source Must not be null.  If null throws a
	     * "hard" null pointer exception (native sig segv).
	     */
	    public Reader(Source.Char source){
		super(source.getIOBuffer());
		this.source = source;
	    }

	    public Source.Char getSource(){
		return this.source;
	    }
	    public void close()
		throws java.io.IOException
	    {
		this.source.closeIO(this);
		super.close();
		this.source = null;
	    }
	}

	/**
	 * @return Cast itself to element
	 */
	public gnu.iou.dom.CharacterData asIONode();

	public Reader getIOReader();

	public gnu.iou.bbuf getIOBuffer();

	public void closeIO(Reader reader);

    }

    /**
     * <p> Called by document destroy.  The implementor should never
     * throw an exception. </p>
     */
    public void destroy();
}
