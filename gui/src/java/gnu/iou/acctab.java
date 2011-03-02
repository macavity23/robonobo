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
 * Access table maintains timestamps.
 *
 * <p><b>Not Synchronized</b>
 *
 * <p> Note that the `acctab' is not multi-thread safe.  In
 * multithreaded usage, external synchronization will be required.
 *
 * <pre>
 * public void run(){
 * 
 *   acctab st = system.st;
 *
 *   synchronized(st){
 *     Object val = st.get("key");
 *   }
 *
 * }
 * </pre>
 *
 * @see java.lang.ref.SoftReference
 */
public class acctab 
    extends objmap 
{
    public static class AccIndex 
	extends Index
    {
	public AccIndex(int init, float load){
	    super(init,load);
	}
	public Index.Entry newEntry(long hash){
	    return new AccEntry(hash);
	}
	public static class AccEntry
	    extends Index.Entry
	{
	    long acc;

	    boolean rel;

	    public AccEntry(long hash){
		super(hash);
	    }
	}
    }

    public acctab(int init, float load) {
	super(init,load);
    }

    public acctab(int init) {
	super(init);
    }

    public acctab() {
	super();
    }

    protected Index newIndex(int init, float load){
	return new AccIndex(init,load);
    }
    /**
     * Lookup the "last access" timestamp on this `acctab' entry.
     *
     * @exception IllegalArgumentException If key is not found.
     */
    public long access(Object key) {
	AccIndex.AccEntry entry = (AccIndex.AccEntry)super._lookup(key);
	if (null != entry)
	    return entry.acc;
	else
	    throw new java.lang.IllegalArgumentException("Key not found.");
    }
    /**
     * Lookup the "release" flag on this `acctab' entry.
     *
     * @exception IllegalArgumentException If key is not found.
     */
    public boolean released(Object key) {
	AccIndex.AccEntry entry = (AccIndex.AccEntry)super._lookup(key);
	if (null != entry)
	    return entry.rel;
	else
	    throw new java.lang.IllegalArgumentException("Key not found.");
    }
    /**
     * Set the "release" flag on this `acctab' entry.
     *
     * @exception IllegalArgumentException If key is not found.
     */
    public boolean release(Object key, boolean relval) {
	AccIndex.AccEntry entry = (AccIndex.AccEntry)super._lookup(key);
	if (null != entry)
	    return (entry.rel = relval);
	else
	    throw new java.lang.IllegalArgumentException("Key not found.");
    }
    public Object get(Object key) {
	AccIndex.AccEntry entry = (AccIndex.AccEntry)super._lookup(key);
	if (null != entry){
	    entry.acc = System.currentTimeMillis();
	    return this.value(entry.aryix);
	}
	else
	    return null;
    }
    public Object put(Object key, Object value) {
	AccIndex.AccEntry ent = (AccIndex.AccEntry)this.table.store(this,key);
	ent.acc = System.currentTimeMillis();
	int aryix = ent.aryix;
	if (Index.Entry.XINIT == aryix){
	    ent.aryix = this._vadd_(key,value);
	    return null;/*(new)*/
	}
	else {
	    Object old = this.vals[aryix];
	    this.vals[aryix] = value;
	    return old;
	}
    }

}
