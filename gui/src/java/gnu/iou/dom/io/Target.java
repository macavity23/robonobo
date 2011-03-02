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
 * <p> An I/O target is an object systemically responsible to the DOM
 * for its storage.  It is the DOM's perspective of its I/O device.
 * </p>
 * 
 * @see gnu.iou.dom.Document
 * 
 * @author jdp
 */
public interface Target {

    /**
     * <p> A byte target is a system device to which the DOM document
     * may be written.  For example a file or http device. </p>
     * 
     * <p> In a large and common class of DOM related systems, this
     * device also maintains a reference (java object) to the
     * DOM Document as a DOM cache. </p>
     * 
     * @see gnu.iou.dom.Document
     */
    public interface Byte extends Target {

	/**
	 * <p> The device target. </p>
	 * 
	 * @return A URI System Id string
	 * @see #getSystemId2()
	 */
	public String getSystemIdTarget();

	/**
	 * @return A parsed URI System Id 
	 * @see #getSystemId()
	 */
	public gnu.iou.uri getSystemIdTarget2();

    }

    /**
     * <p> A character target is a character data node which content
     * may be written. </p>
     *
     * @see gnu.iou.dom.CharacterData
     */
    public interface Char extends Target {

	public class Writer 
	    extends gnu.iou.bbo
	{
	    protected Target.Char target;

	    /**
	     * @param target Must not be null.  If null throws a
	     * "hard" null pointer exception (native sig segv).
	     */
	    public Writer(Target.Char target){
		super(target.getIOBuffer());
		this.target = target;
	    }

	    public Target.Char getTarget(){
		return this.target;
	    }
	    public void flush()
		throws java.io.IOException
	    {
		this.target.flushIO(this);
		super.flush();
	    }
	    public void close()
		throws java.io.IOException
	    {
		this.target.closeIO(this);
		super.close();
		this.target = null;
	    }
	}

	/**
	 * @return Cast itself to element
	 */
	public gnu.iou.dom.CharacterData asIONode();

	public Writer getIOWriter();

	public gnu.iou.bbuf getIOBuffer();

	public void flushIO(Writer writer);

	public void closeIO(Writer writer);

    }

}
