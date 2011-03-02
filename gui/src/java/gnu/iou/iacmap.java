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

/**
 * Integer keyed map with access timestamps on the "get" and "put"
 * methods.
 * 
 * @author jdp
 */
public class iacmap extends intmap {

    public static class IacIndex
	extends Index
    {
	public IacIndex(int init, float load){
	    super(init,load);
	}
	protected Index.Entry newEntry(long hash){
	    return new IacEntry(hash);
	}
	/**
	 * Intmap entry with access timestamp.
	 * 
	 * @author jdp
	 */
	public static class IacEntry 
	    extends Index.Entry 
	{
	    long acc;

	    public IacEntry(long hash){
		super(hash);
	    }
	}
    }

    public iacmap(int initialCapacity, float loadFactor) {
	super(initialCapacity,loadFactor);
    }

    public iacmap(int initialCapacity) {
	super(initialCapacity);
    }

    public iacmap() {
	super();
    }

    protected Index newIndex(int init, float load){
	return new IacIndex(init,load);
    }

    /**
     * Lookup the "last access" timestamp on this `acctab' entry.
     *
     * @exception IllegalArgumentException If key is not found.
     */
    public long access(int key) {
	IacIndex.IacEntry entry = (IacIndex.IacEntry)super._lookup(key);
	if (null != entry)
	    return entry.acc;
	else
	    return 0L;
    }
    public int indexOf(int key){
	IacIndex.IacEntry entry = (IacIndex.IacEntry)super._lookup(key);
	if (null == entry)
	    return -1;
	else {
	    entry.acc = System.currentTimeMillis();
	    return entry.aryix;
	}
    }
    public Object put( int key, Object val){
	IacIndex.IacEntry ent = (IacIndex.IacEntry)this.table.store(this,key);
	ent.acc = System.currentTimeMillis();
	int aryix = ent.aryix;
	if (Index.Entry.XINIT == aryix){
	    ent.aryix = this._vadd_(key,val);
	    return null;/*(new)*/
	}
	else {
	    Object old = this.vals[aryix];
	    this.vals[aryix] = val;
	    return old;
	}
    }

}
