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
 * <p> Abstract base hash array maintains input order over indexed
 * keys for a map from source (key) to target (value) values
 * (maintained internally).  This is the infrastructure for a broad
 * variety of subclasses including not only specialized map key and
 * value types, but also multiple map indeces. </p>
 * 
 * <h3>Not Synchronized</h3>
 *
 * <p> This class is not multi- thread safe, it is intended for use by
 * a single thread- user.  External thread safety must be applied in
 * multi- threaded applications. </p>
 *
 * <h3>Multiple key instances</h3>
 *
 * <p> The key array may contain multiple identical keys.  Dictionary
 * usage implies one key, array usage may imply additional
 * instances. </p>
 * 
 * <h3>Multiple key and value types</h3>
 * 
 * <p> This abstract base class supports both object and primitive
 * types for both keys and values.  A primitive integer type key is
 * supported through a 64 bit long integer hash to represent a 31 bit
 * value (32 bit positive integer) plus a special internal value
 * (NIL).  </p>
 *
 * @author John Pritchard 
 */
public abstract class hasharray 
    extends java.util.Dictionary 
    implements java.lang.Cloneable 
{

    public final static int ZED = 0;

    public final static Object KO_NIL = null;

    public final static long KL_NIL = Long.MIN_VALUE;

    public final static long HASH_MASK = 0x7FFFFFFFL;

    /**
     * <p> Set a range of integer keys to special value NIL. </p>
     */
    protected final static void NILKEYS ( long[] keys, int from, int many){
	for ( int cc = from; cc < many; cc++)
	    keys[cc] = KL_NIL;
    }

    /**
     * <p> Extensible index for the hash vector.  Object map
     * subclasses can add indeces along- side the default object
     * index, 'table'. </p>
     * 
     * <p> Each index has its own hash function.  The default, defined
     * here, calls the instance object hash code method.</p>
     * 
     * @author John Pritchard
     */
    public static class Index implements Cloneable {

	/**
	 * <p> Extensible index table record</p>
	 * 
	 * @see hasharray$Index#newEntry()
	 */
	public static class Entry implements Cloneable {

	    public final static int XINIT = -1;

	    /**
	     * Thirty two bit value, or high bit on for nil.
	     * @see hasharray.KL_NIL
	     */
	    public final long hash;

	    /** <p> Initialized to {@link #XINIT}. </p> 
	     */
	    public int aryix = XINIT;

	    protected Entry next;

	    public Entry(long hash){
		super();
		this.hash = hash;
	    }
	    /**
	     * @return Clean 31 bit value (positive integer) for table
	     * arithmetic (modulo table length).
	     */
	    public long hash(){
		return (this.hash & HASH_MASK);
	    }

	    /**
	     * <p> This method is called from the index.
	     * </p> 
	     * @param caller Map
	     * @param index Container
	     * @param query Test key identity with internal value
	     * @return Key identity
	     */
	    public boolean kequals( hasharray caller, Index index, Object query){
		if (XINIT < this.aryix){
		    Object key = caller.keyO(this.aryix);
		    if (null != key)
			return key.equals(query);
		}
		return false;
	    }

	    /**
	     * <p> This method is called from the index.
	     * <pre>
	     *	  return caller.keys[this.aryix].equals(query);
	     * </pre>
	     * </p> 
	     * @param caller Map
	     * @param index Container
	     * @param query Test key identity with internal value
	     * @return Key identity
	     */
	    public boolean kequals( hasharray caller, Index index, long query){
		if (XINIT < this.aryix){
		    long key = caller.keyL(this.aryix);
		    if (KL_NIL != key)
			return (key == query);
		}
		return false;
	    }

	    protected Object clone(){
		return this.cloneEntry();
	    }
	    protected Entry cloneEntry(){
		try {
		    Entry entry = (Entry)super.clone();
		    entry.next = (null != this.next)?((Entry)this.next.clone()):(null);
		    return entry;
		}
		catch (CloneNotSupportedException cns){
		    throw new IllegalStateException();
		}
	    }	    
	}
	/**
	 * <p> Copy the linked list to an array for the consistent
	 * iteration over this list of table elements during changes
	 * to the list. </p>
	 */
	public final static Entry[] ListCopy( Entry li){
	    if (null == li)
		return null;
	    else if (null == li.next)
		return new Entry[]{li};
	    else {
		int bl = 10;/*(init optimistic output buffer)
			     */
		Entry[] re = new Entry[bl], copier;
		int rx = 0, rl = re.length;
		for (Entry pp = li; null != pp; rx += 1, pp = pp.next){
		    if (rx >= rl){
			copier = new Entry[rl+bl];/*(grow output buffer optimistically)
						   */
			System.arraycopy(re,0,copier,0,rl);
			re = copier;
			rl += bl;
		    }
		    re[rx] = pp;
		}
		if (rx < rl){
		    /*(truncate optimistic buffer)
		     */
		    copier = new Entry[rx];
		    System.arraycopy(re,0,copier,0,rx);
		    return copier;
		}
		else
		    return re;
	    }
	}
	/**
	 * <p> Append the element 'in' to the list 'li', maintaining
	 * both the uniqueness of the element 'in' within the set 'li'
	 * and the input order represented by each elements 'aryix'
	 * value. </p>
	 * 
	 * <p> The basic action here is to append 'in' to 'li', while
	 * the 'aryix' value of 'li' can require an insertion for
	 * maintaining the index table in the input order of the keys
	 * and values arrays of the hasharray subclass. </p>
	 * 
	 * @param li Table list
	 * @param in Table element 
	 */
	public final static Entry ListAppend( Entry li, Entry in){
	    if (null == li){
		in.next = null;
		return in;
	    }
	    else if (null == in)
		throw new IllegalArgumentException();
	    else if (li == in)
		return li;
	    else {
		int ix = in.aryix;
		if (Entry.XINIT == ix){
		    /*
		     * Simple append of (in) to list (li).
		     */
		    for (Entry lp = li; null != lp; lp = lp.next){
			/*
			 * Iterate over list (li) using list pointer (lp).
			 */
			if (in == lp)
			    return li;
			else if (null == lp.next){
			    lp.next = in;
			    in.next = null;
			    return li;
			}
		    }
		    return li;
		}
		else {
		    /*
		     * Insert (in) into list (li) where (ix=in.aryix) < (lx=lp.aryix)
		     */
		    int lx;
		    Entry ll = null, lp = li;
		    for (; null != lp; ll = lp, lp = lp.next){
			if (in == lp)
			    return li;
			else {
			    lx = lp.aryix;
			    if (ix < lx){
				if (null == ll){
				    in.next = lp;
				    return in;
				}
				else {
				    in.next = lp;
				    ll.next = in;
				    return li;
				}
			    }
			    else if (ix == lx){
				/*
				 * Replace entry
				 */
				if (null == ll){
				    in.next = lp.next;
				    lp.next = null;//delete(lp)
				    return in;
				}
				else {
				    ll.next = in;
				    in.next = lp.next;
				    return li;
				}
			    }
			}
		    }
		    //
		    ll.next = in;
		    in.next = null;
		    return li;
		}
	    }
	}
	public Entry[] ListAppend(Entry[] list, Entry item){
	    if (null == list)
		return new Entry[]{item};
	    else {
		int list_len = list.length;
		switch(list_len){
		case 1:
		    return new Entry[]{list[0],item};
		case 2:
		    return new Entry[]{list[0],list[1],item};
		case 3:
		    return new Entry[]{list[0],list[1],list[2],item};
		default:
		    Entry[] nlist = new Entry[list_len+1];
		    java.lang.System.arraycopy(list,0,nlist,0,list_len);
		    nlist[list_len] = item;
		    return nlist;
		}
	    }
	}
	/**
	 * <p> Collision map. </p> 
	 */
	protected Entry table[];
	/**
	 * <p> Current boundary for reindexing is table length times
	 * load. </p>
	 */
	protected int threshold;
	/**
	 * <p> A value between zero and one that defines the threshold
	 * for reindexing. </p>
	 */
	protected float load;
	/**
	 * <p> Internal capacity growth factor. </p> 
	 */
	protected int grow;

	protected Index(int initial, float load){
	    super();
	    if ((initial <= 0) || (load <= 0f) || (load >= 1f))
		throw new IllegalArgumentException();
	    else {
		if (0 == (initial & 1))
		    initial |= 1;
		
		this.load = load;
		this.table = new Entry[initial];
		this.threshold = (int)((float)initial * load);
		this.grow = initial;
	    }
	}
	protected Index(){
	    this(11,0.75f);
	}
	protected Entry newEntry(long hash){
	    return new Entry(hash);
        }
	/**
	 * <p> This method is called from the indexing methods to call
	 * the entry method.  </p>
	 * @param caller Map
	 * @param index Container
	 * @param query Test identity with internal value
	 * @return Keys are identical
	 */
	protected boolean kequals( hasharray caller, Entry ent, Object query){
	    return ent.kequals(caller,this,query);
	}
	/**
	 * <p> This method is called from the indexing methods to call
	 * the entry method.  </p>
	 * @param caller Map
	 * @param index Container
	 * @param query Test identity with internal value
	 * @return Keys are identical
	 */
	protected boolean kequals( hasharray caller, Entry ent, long query){
	    return ent.kequals(caller,this,query);
	}
	/**
	 * @return Non negative 32 bit (31 bits) value for key hash code
	 */
	protected long hash(Object key){
	    return (key.hashCode() & Integer.MAX_VALUE);
	}
	/**
	 * @return Non negative 32 bit (31 bits) value for key hash code
	 */
	protected long hash(long key){
	    return (key & HASH_MASK);
	}
	protected void clear(hasharray caller){
 	    Entry table[] = this.table;
 	    for (int index = 0, tlen = table.length; index < tlen; index++){
 		table[index] = null;
 	    }
	}
	protected Object clone(){
	    return this.cloneIndex();
	}
	protected Index cloneIndex(){
	    try {
		Index index = (Index)super.clone();
		index.table = (Entry[])this.table.clone();
		Entry ent;
		for (int ac = 0, an = this.table.length, bc, bn; ac < an; ac++){
		    ent = index.table[ac];
		    if (null != ent)
			index.table[ac] = ent.cloneEntry();
		}
		return index;
	    }
	    catch (CloneNotSupportedException cns){
		throw new IllegalStateException();
	    }
	}
	/** @return Found entry, or null
	 */
	protected Entry lookup(hasharray caller, Object key){
	    if (KO_NIL == key)
		return null;
	    else {
		long hash = this.hash(key);
		Entry table[] = this.table;
		int index = ((int)(hash & HASH_MASK) % table.length);
		for (Entry e = table[index] ; e != null ; e = e.next){
		    if (hash == e.hash && this.kequals(caller,e,key)){ //caller.keys[e.aryix].equals(key)){
			return e;
		    }
		}
		return null;
	    }
	}
	protected Entry lookup(hasharray caller, long key){
	    if (KL_NIL == key)
		return null;
	    else {
		long hash = this.hash(key);
		Entry table[] = this.table;
		int index = ((int)(hash & HASH_MASK) % table.length);
		for (Entry e = table[index] ; e != null ; e = e.next){
		    if (hash == e.hash && this.kequals(caller,e,key)){ //caller.keys[e.aryix].equals(key)){
			return e;
		    }
		}
		return null;
	    }
	}
	/** @return List or null
	 */
	protected Entry[] lookup_list(hasharray caller, Object key){
	    if (KO_NIL == key)
		return null;
	    else {
		long hash = this.hash(key);
		Entry table[] = this.table;
		Entry list[] = null;
		int index = ((int)(hash & HASH_MASK) % table.length);
		for (Entry e = table[index] ; e != null ; e = e.next){
		    if (hash == e.hash && this.kequals(caller,e,key)){
			list = ListAppend(list,e);
		    }
		}
		return list;
	    }
	}
	protected Entry[] lookup_list(hasharray caller, long key){
	    if (KL_NIL == key)
		return null;
	    else {
		long hash = this.hash(key);
		Entry table[] = this.table;
		Entry list[] = null;
		int index = ((int)(hash & HASH_MASK) % table.length);
		for (Entry e = table[index] ; e != null ; e = e.next){
		    if (hash == e.hash && this.kequals(caller,e,key)){
			list = ListAppend(list,e);
		    }
		}
		return list;
	    }
	}
	/** <p> Grow the table. </p>
	 */
	protected final void rehash(hasharray caller){
	    Entry ot[] = this.table, pp;
	    int olen = ot.length, px, pn;
	    int nlen = (olen + this.grow), cc, index;
	    if (0 == (nlen & 1))
		nlen |= 1;
	    Entry nt[] = new Entry[nlen],  pl[];
	    this.threshold = (int)(nlen * this.load);
	    this.table = nt;
	    for (cc = (olen-1); -1 < cc; cc--){
		pl = ListCopy(ot[cc]);
		if (null != pl)
		    for (px = 0, pn = pl.length; px < pn; px++){
			pp = pl[px];
			index = (((int)this.hash(pp.hash)) % nlen);
			nt[index] = ListAppend(nt[index],pp);
		    }
	    }
	}
	protected final boolean threshold(hasharray user){
	    return (user._size() >= this.threshold);
	}
	/** @return New entry
	 */
	protected Entry append( hasharray caller, Object key){
	    Entry table[] = this.table;
	    long hash = this.hash(key);
	    int index = ((int)(hash & HASH_MASK) % table.length);
	    Entry ne = this.newEntry(hash);
	    table[index] = ListAppend(table[index],ne);
	    return ne;
	}
	protected Entry append( hasharray caller, long key){
	    Entry table[] = this.table;
	    long hash = this.hash(key);
	    int index = ((int)(hash & HASH_MASK) % table.length);
	    Entry ne = this.newEntry(hash);
	    table[index] = ListAppend(table[index],ne);
	    return ne;
	}
	/** @return New entry
	 */
	protected Entry replace(hasharray caller, int idx, Object nkey, Object okey){
	    long nhash = this.hash(nkey);
	    long ohash = this.hash(okey);
	    Entry table[] = this.table;
	    int nindex = (((int)(nhash & HASH_MASK)) % table.length);
	    int oindex = (((int)(ohash & HASH_MASK)) % table.length);
	    return this.replace(idx,nhash,ohash,table,nindex,oindex);
	}
	/** @return New entry
	 */
	protected Entry replace(hasharray caller, int idx, long nkey, long okey){
	    long nhash = this.hash(nkey);
	    long ohash = this.hash(okey);
	    Entry table[] = this.table;
	    int nindex = (((int)(nhash & HASH_MASK)) % table.length);
	    int oindex = (((int)(ohash & HASH_MASK)) % table.length);
	    return this.replace(idx,nhash,ohash,table,nindex,oindex);
	}
	private Entry replace(int idx, long nhash, long ohash, Entry[] table, int nindex, int oindex){
	    Entry ne = this.newEntry(nhash), ie, le;
	    /*
	     * Drop OE
	     */
	    for (le = null, ie = table[oindex]; null != ie; le = ie, ie = ie.next){
		if (idx == ie.aryix){
		    if (null == le)
			table[oindex] = ie.next;
		    else 
			le.next = ie.next;
		}
	    }
	    /*
	     * Append NE
	     */
	    table[nindex] = ListAppend(table[nindex],ne);
	    //
	    return ne;
	}
	/** @return Dropped index entry 
	 */
	protected Entry remove( hasharray caller, Object key){
	    if (KO_NIL == key)
		return null;
	    else {
		Entry table[] = this.table;
		long hash = this.hash(key);
		int index = (((int)(hash & HASH_MASK)) % table.length);
		for (Entry ie = table[index], le = null ; ie != null ; le = ie, ie = ie.next){
		    if ((hash == ie.hash) &&
			(this.kequals(caller,ie,key)))
			return this.remove_drop(le,ie,table,index);
		}
		return null;
	    }
	}
	/** @return Dropped index entry 
	 */
	protected Entry remove( hasharray caller, long key){
	    if (KL_NIL == key)
		return null;
	    else {
		Entry table[] = this.table;
		long hash = this.hash(key);
		int index = (((int)(hash & HASH_MASK)) % table.length);
		for (Entry ie = table[index], le = null ; ie != null ; le = ie, ie = ie.next){
		    if ((hash == ie.hash) &&
			(this.kequals(caller,ie,key)))
			return this.remove_drop(le,ie,table,index);
		}
		return null;
	    }
	}
	private Entry remove_drop(Entry le, Entry ie, Entry[] table, int index){
	    Entry re = ie;
	    int aryix = ie.aryix;
	    /*
	     * Drop IE
	     */
	    if (null != le) 
		le.next = ie.next;
	    else 
		table[index] = ie.next;
	    /*
	     * Sanitize RE=IE
	     */
	    re.next = null;
	    /*
	     * Decrement pointers for remove
	     */
	    for (int tc = 0, tlen = table.length; tc < tlen; tc++){
		ie = table[tc];
		while ( null != ie){
		    if (aryix < ie.aryix)
			ie.aryix -= 1;
		    ie = ie.next;
		}
	    }
	    return re;
	}

	/** @return Dropped secondary (dupe) index entry 
	 */
	protected Entry remove( hasharray caller, int idx){
	    Entry table[] = this.table, ie, le, re = null;
	    int iearyix;
	    scanindex:
	    for (int tx = 0, tl = table.length; tx < tl; tx++){
		for (le = null, ie = table[tx]; null != ie; le = ie, ie = ie.next){
		    iearyix = ie.aryix;
		    if (idx < iearyix)
			/*
			 * Decrement pointers for remove
			 */
			ie.aryix -= 1;
		    else if (idx == iearyix){
			/*
			 * Drop IE
			 */
			re = ie;
			if (null == le)
			    table[tx] = ie.next;
			else
			    le.next = ie.next;
			re.next = null;
		    }
		}
	    }
	    return re;
	}
	/**
	 * @return Preexisting value, or null
	 */
	protected Entry store(hasharray caller, Object key){
	    if (null == key)
		return null;
	    else {
		long hash = this.hash(key);
		Entry table[] = this.table;
		int index = (((int)(hash & HASH_MASK)) % table.length);
		/*
		 * Lookup
		 */
		for (Entry ent = table[index] ; ent != null ; ent = ent.next){
		    if (ent.hash == hash){
			if (this.kequals(caller,ent,key))
			    return ent;
		    }
		}
		//
		if (this.threshold(caller)){
		    this.rehash(caller);
		    return this.store(caller, key);
		} 
		else {
		    Entry nent = this.newEntry(hash);
		    table[index] = ListAppend(table[index],nent);
		    return nent;
		}
	    }
	}
	protected Entry store(hasharray caller, long key){
	    if (KL_NIL == key)
		return null;
	    else {
		long hash = key;
		Entry table[] = this.table;
		int index = ((int)(hash & HASH_MASK) % table.length);
		/*
		 * Lookup
		 */
		for (Entry ent = table[index] ; ent != null ; ent = ent.next){
		    if (ent.hash == hash){
			if (this.kequals(caller,ent,key))
			    return ent;
		    }
		}
		//
		if (this.threshold(caller)){
		    this.rehash(caller);
		    return this.store(caller, key);
		} 
		else {
		    Entry nent = this.newEntry(hash);
		    table[index] = ListAppend(table[index],nent);
		    return nent;
		}
	    }
	}
	/** @return Existing or new entry
	 */
	protected Entry add( hasharray caller, Object key){
	    if (KO_NIL == key)
		return null;
	    else if (this.threshold(caller)){
		this.rehash(caller);
		return this.add(caller, key);
	    } 
	    else {
		long hash = this.hash(key);
		Entry table[] = this.table;
		int index = (((int)(hash & HASH_MASK)) % table.length);
		Entry nent = this.newEntry(hash);
		table[index] = ListAppend(table[index],nent);
		return nent; 
	    }
	}
	protected Entry add( hasharray caller, long key){
	    if (KL_NIL == key)
		return null;
	    else if (this.threshold(caller)){
		this.rehash(caller);
		return this.add(caller, key);
	    } 
	    else {
		long hash = this.hash(key);
		Entry table[] = this.table;
		int index = (((int)(hash & HASH_MASK)) % table.length);
		Entry nent = this.newEntry(hash);
		table[index] = ListAppend(table[index],nent);
		return nent; 
	    }
	}
	protected Entry insert(hasharray caller, int idx, Object key){
	    Entry table[] = this.table, re = null;
	    long hash = this.hash(key);
	    int index = (((int)(hash & HASH_MASK)) % table.length);
	    Entry ne = this.newEntry(hash), ie;
	    for (int ii = 0, il = table.length; ii < il; ii++){
		if (ii == index){
		    ie = table[ii];
		    if (null == ie)
			table[ii] = ne;
		    else {
			boolean nindexed = true;
			Entry le = null;
			for (; null != ie; le = ie, ie = ie.next){
			    if (hash == ie.hash){
				if (this.kequals(caller,ie,key)){ 
				    if (ie.aryix <= idx){
					ne.next = ie.next;
					ie.next = ne;
				    }
				    else if (null == le){
					/*
					 * Insert for (idx >  ie.aryix)
					 */
					table[ii] = ne;
					ne.next = ie;
				    }
				    else {
					le.next = ne;
					ne.next = ie;
				    }
				    nindexed = false;
				}
			    }
			    if (ie != ne && idx <= ie.aryix)
				/*
				 * Increment pointers for insert
				 */
				ie.aryix += 1;
			}
			if (nindexed){
			    ne.next = table[ii];
			    table[ii] = ne;
			}
		    }
		}//(if (ii == index)
		else {
		    for (ie = table[ii]; null != ie; ie = ie.next){
			if (idx <= ie.aryix)
			    /*
			     * Increment pointers for insert
			     */
			    ie.aryix += 1;
		    }
		}
	    }
	    return ne;
	}
	protected Entry insert(hasharray caller, int idx, long key){
	    Entry table[] = this.table, re = null;
	    long hash = this.hash(key);
	    int index = (((int)(hash & HASH_MASK)) % table.length);
	    Entry ne = this.newEntry(hash), ie;
	    for (int ii = 0, il = table.length; ii < il; ii++){
		if (ii == index){
		    ie = table[ii];
		    if (null == ie)
			table[ii] = ne;
		    else {
			boolean nindexed = true;
			Entry le = null;
			for (; null != ie; le = ie, ie = ie.next){
			    if (hash == ie.hash){
				if (this.kequals(caller,ie,key)){ 
				    if (ie.aryix <= idx){
					ne.next = ie.next;
					ie.next = ne;
				    }
				    else if (null == le){
					/*
					 * Insert for (idx >  ie.aryix)
					 */
					table[ii] = ne;
					ne.next = ie;
				    }
				    else {
					le.next = ne;
					ne.next = ie;
				    }
				    nindexed = false;
				}
			    }
			    if (ie != ne && idx <= ie.aryix)
				/*
				 * Increment pointers for insert
				 */
				ie.aryix += 1;
			}
			if (nindexed){
			    ne.next = table[ii];
			    table[ii] = ne;
			}
		    }
		}//(if (ii == index)
		else {
		    for (ie = table[ii]; null != ie; ie = ie.next){
			if (idx <= ie.aryix)
			    /*
			     * Increment pointers for insert
			     */
			    ie.aryix += 1;
		    }
		}
	    }
	    return ne;
	}
    }

    protected Index table;

    protected int count = 0;

    public hasharray(int initial, float load){
	super();
	this.table = this.newIndex(initial,load);
    }
    public hasharray(int initial){
	this (initial, 0.75f);
    }
    /**
     * Default initial capacity is 11 elements.  Default load factor
     * is three- quarters (of one).
     */
    public hasharray(){
	this( 11, 0.75f);
    }
    protected Index newIndex(int init, float load){
	return new Index(init,load);
    }
    public void destroy(){
	this.clear();
    }
    public abstract void clear();

    public int size(){
	return this._size();
    }
    protected final int _size(){
	return this.count;
    }
    public boolean isEmpty(){
	return this._isEmpty();
    }
    protected final boolean _isEmpty(){
	return (0 < this.count);
    }
    public boolean isNotEmpty(){
	return this._isNotEmpty();
    }
    protected final boolean _isNotEmpty(){
	return (0 >= this.count);
    }
    /** The unused method throws UnsupportedOperationException
     */
    public abstract Object keyO(int index);
    /** The unused method throws UnsupportedOperationException
     */
    public abstract long keyL(int index);

    protected final Index.Entry _lookup( Object key){
	return this.table.lookup(this,key);
    }
    protected final Index.Entry _lookup( long key){
	return this.table.lookup(this,key);
    }
    protected final Index.Entry[] _lookup_list( Object key){
	return this.table.lookup_list(this,key);
    }
    protected final Index.Entry[] _lookup_list( long key){
	return this.table.lookup_list(this,key);
    }
    public void copy ( hasharray ano){
	this._copy(ano);
    }
    protected final void _copy ( hasharray ano){
	this.clear();
	this.add(ano);
    }
    public void add ( hasharray ano){
	this._add(ano);
    }
    protected final void _add ( hasharray ano){
	if (null == ano)
	    return;
	else {
	    java.util.Enumeration keys = ano.keys();
	    Object k, v;
	    while (keys.hasMoreElements()){
		k = keys.nextElement();
		v = ano.get(k);
		this.put(k,v);
	    }
	}
    }
    public hasharray cloneHasharray(){
	try { 
	    hasharray t = (hasharray)super.clone();
	    t.table = table.cloneIndex();
	    return t;
	} catch (CloneNotSupportedException e){ 
	    throw new InternalError();
	}
    }
    /**
     * Print table with bracket wrappers, "name equals value, comma"
     * format, as in the following example.
     *
     * <pre>
     * "[" NAME1 "=" VALUE1 "," NAME2 "=" VALUE2 "]"
     * </pre> 
     */
    public String toString(){
	return chbuf.cat( "[", toString('=',','), "]");
    }
    /**
     * Print elements with characters "subinfix" and "infix" as in the
     * following example for two name, value pairs.
     *
     * <pre>
     *    NAME1 subinfix VALUE1 infix NAME2 subinfix VALUE2
     * </pre> 
     *
     * <p> Uses <tt>"chbuf.append(Object)"</tt> which defaults to
     * <tt>"Object.toString()".</tt>
     *
     * @param subinfix Character between elements of a name- value
     * pair, eg, <tt>'='.</tt>
     *
     * @param infix Character between name- value pairs, eg,
     * <tt>','</tt> or <tt>'\n'</tt>.
     */
    public String toString( char subinfix, char infix){
	chbuf sb = new chbuf();
	java.util.Enumeration keys = this.keys();
	Object k; 
	Object v;
	String ks, vs;
	for (int idx = 0; keys.hasMoreElements(); idx++){
	    k = keys.nextElement();
	    if (null != k){
		ks = k.toString();
		if ( null != ks){
		    if ( 0 < idx)
			sb.append(infix);
		    sb.append(ks);
		    v = this.get(k);
		    if ( null != v){
			vs = v.toString();
			if ( null != vs){
			    sb.append(subinfix);
			    sb.append(vs);
			}
		    }
		}
	    }
	}
	return sb.toString();
    }
    /**
     * Copy over deleted index in buffer.  Deleted means that the
     * value at index is ignored and simply overwritten by this
     * function.
     */
    public final static void shift ( Object[] oary, int idx){
	if ( 0 > idx)
	    return ;
	else {
	    int len = oary.length, len1 = oary.length-1;
	    if ( 0 == idx)
		System.arraycopy( oary, 1, oary, 0, len1);
	    else if ( idx == len1)
		oary[idx] = null;
	    else if ( idx < len1)
		System.arraycopy( oary, (idx+1), oary, idx, len1-idx);
	}
    }
    /**
     * Copy over deleted index in buffer.  Deleted means that the
     * value at index is ignored and simply overwritten by this
     * function.
     */
    public final static void shift ( int[] iary, int idx){
	if ( 0 > idx)
	    return ;
	else {
	    int len = iary.length, len1 = iary.length-1;
	    if ( 0 == idx)
		System.arraycopy( iary, 1, iary, 0, len1);
	    else if ( idx == len1)
		iary[idx] = ZED;
	    else if ( idx < len1)
		System.arraycopy( iary, (idx+1), iary, idx, len1-idx);
	}
    }
    /**
     * Copy over deleted index in buffer.  Deleted means that the
     * value at index is ignored and simply overwritten by this
     * function.
     */
    public final static void shift ( long[] iary, int idx){
	if ( 0 > idx)
	    return ;
	else {
	    int len = iary.length, len1 = iary.length-1;
	    if ( 0 == idx)
		System.arraycopy( iary, 1, iary, 0, len1);
	    else if ( idx == len1)
		iary[idx] = KL_NIL;
	    else if ( idx < len1)
		System.arraycopy( iary, (idx+1), iary, idx, len1-idx);
	}
    }

    /**
     * <p> Object CAT with optional array component type.</p>
     * 
     * @param a Optional object array 
     * @param b Optional object array 
     * @param c Optional component type
     */
    public final static Object[] cat( Object[] a, Object[] b, Class c){
	if (null == a)
	    return b;
	else if (null == b)
	    return a;
	else {
	    int a_len = a.length;
	    int b_len = b.length;
	    int r_len = a_len+b_len;
	    Object[] r;
	    if (null != c)
		r = (Object[])java.lang.reflect.Array.newInstance(c,r_len);
	    else
		r = new Object[r_len];
	    System.arraycopy(a,0,r,0,a_len);
	    System.arraycopy(b,0,r,a_len,b_len);
	    return r;
	}
    }
    /**
     * <p> Int CAT </p>
     * 
     * @param a Optional array 
     * @param b Optional array 
     */
    public final static int[] cat( int[] a, int[] b){
	if (null == a)
	    return b;
	else if (null == b)
	    return a;
	else {
	    int a_len = a.length;
	    int b_len = b.length;
	    int r_len = a_len+b_len;
	    int[] r = new int[r_len];

	    System.arraycopy(a,0,r,0,a_len);

	    System.arraycopy(b,0,r,a_len,b_len);

	    return r;
	}
    }
    /**
     * <p> Long CAT </p>
     * 
     * @param a Optional array 
     * @param b Optional array 
     */
    public final static long[] cat( long[] a, long[] b){
	if (null == a)
	    return b;
	else if (null == b)
	    return a;
	else {
	    int a_len = a.length;
	    int b_len = b.length;
	    int r_len = a_len+b_len;
	    long[] r = new long[r_len];

	    System.arraycopy(a,0,r,0,a_len);

	    System.arraycopy(b,0,r,a_len,b_len);

	    return r;
	}
    }

    public final static Object[] copy( Object[] a, Class c){
	if (null == a)
	    return a;
	else {
	    int a_len = a.length;
	    Object[] r;
	    if (null != c)
		r = (Object[])java.lang.reflect.Array.newInstance(c,a_len);
	    else
		r = new Object[a_len];
	    System.arraycopy(a,0,r,0,a_len);
	    return r;
	}
    }
    public final static int[] copy( int[] a){
	if (null == a)
	    return a;
	else {
	    int a_len = a.length;
	    int[] r = new int[a_len];

	    System.arraycopy(a,0,r,0,a_len);

	    return r;
	}
    }
    public final static long[] copy( long[] a){
	if (null == a)
	    return a;
	else {
	    int a_len = a.length;
	    long[] r = new long[a_len];

	    System.arraycopy(a,0,r,0,a_len);

	    return r;
	}
    }
    public final static Object[] grow( Object[] a, int n_len, Class c){
	if (1 > n_len)
	    return a;
	else if (null == a){
	    Object[] r;
	    if (null != c)
		r = (Object[])java.lang.reflect.Array.newInstance(c,n_len);
	    else
		r = new Object[n_len];
	    return r;
	}
	else {
	    int a_len = a.length;
	    if (n_len > a_len){
		Object[] r;
		if (null != c)
		    r = (Object[])java.lang.reflect.Array.newInstance(c,n_len);
		else
		    r = new Object[n_len];
		System.arraycopy(a,0,r,0,a_len);
		return r;
	    }
	    else
		return a;
	}
    }
    public final static int[] grow( int[] a, int n_len){
	if (1 > n_len)
	    return a;
	else if (null == a){
	    int[] r = new int[n_len];
	    return r;
	}
	else {
	    int a_len = a.length;
	    if (n_len > a_len){

		int[] r = new int[n_len];

		System.arraycopy(a,0,r,0,a.length);

		return r;
	    }
	    else
		return a;
	}
    }
    public final static long[] grow( long[] a, int n_len){
	if (1 > n_len)
	    return a;
	else if (null == a){
	    long[] r = new long[n_len];
	    return r;
	}
	else {
	    int a_len = a.length;
	    if (n_len > a_len){

		long[] r = new long[n_len];

		System.arraycopy(a,0,r,0,a.length);

		return r;
	    }
	    else
		return a;
	}
    }

    protected static void usage( java.io.PrintStream out){
	out.println(" usage: hasharray type N");
	out.println();
	out.println("    Test a hasharray for N elements.");
	out.println();
    }
    /**
     * <p> Timed put and get test, with confirmation by enumeration of
     * keys over get.  </p>
     */
    public static void main (String[] argv){
	if (null == argv || 2 > argv.length){
	    usage(System.err);
	    System.exit(1);
	}
	else {
	    try {
		String type = argv[0];
		int N = Integer.parseInt(argv[1]);
		if (0 < N){
		    hasharray tm = null;//new hasharray();
		    Object tt;
		    Object[] testvector = new Object[N];
		    long start;
		    double duration;
		    java.util.Random prng = new java.util.Random();
		    //
		    start = System.currentTimeMillis();
		    for (int cc = 0; cc < N; cc++)
			testvector[cc] = new Integer(prng.nextInt());
		    duration = (double)(System.currentTimeMillis()-start);
		    System.out.println("constructed test vector for "+N+" cycles in "+(duration/1000.0)+" seconds.");
		    //
		    int dupe = 0;
		    start = System.currentTimeMillis();
		    for (int cc = 0; cc < N; cc++){
			while (true){
			    tt = testvector[cc];
			    if (null != tm.get(tt)){
				dupe += 1;
				System.out.println("input-store-test dup "+tt);
			    }
			    //
			    tm.put(tt,tt);
			    //
			    if (tt != tm.get(tt))
				throw new IllegalStateException("input-store-test failed at "+cc);
			    else
				break;
			}
		    }
		    duration = (double)(System.currentTimeMillis()-start);
		    //
		    if ((N-dupe) != (tm.size()))
			throw new IllegalStateException("input-store-test failed for size "+tm.size()+" != N "+N);
		    else {
			System.out.println("input-store-test completed for "+N+" cycles in "+(duration/1000.0)+" seconds.");
			java.util.Enumeration keys = tm.keys();
			int count = 0;
			Object tk, tv;
			//
			start = System.currentTimeMillis();
			while (keys.hasMoreElements()){
			    tk = keys.nextElement();
			    tv = tm.get(tk);
			    count += 1;
			    if (tv == tk)
				continue;
			    else
				throw new IllegalStateException("keys-lookup-test miss "+tv+" != "+tk);
			}
			duration = (double)(System.currentTimeMillis()-start);
			//
			System.out.println("keys-lookup-test completed for "+count+" cycles in "+(duration/1000.0)+" seconds.");
			//
			start = System.currentTimeMillis();
			for (int cc = 0; cc < N; cc++){
			    tt = testvector[cc];
			    tv = tm.get(tt);
			    if (tt != tv)
				throw new IllegalStateException("vector-lookup-test miss "+tt+" != "+tv);
			}
			duration = (double)(System.currentTimeMillis()-start);
			//
			System.out.println("vector-lookup-test completed for "+count+" cycles in "+(duration/1000.0)+" seconds.");
			return;//(for test2)//System.exit(0);
		    }
		}
		else
		    throw new IllegalArgumentException();
	    }
	    catch (NumberFormatException input){
		usage(System.err);
		System.exit(1);
	    }
	    catch (IllegalArgumentException input){
		usage(System.err);
		System.exit(1);
	    }
	    catch (Exception exc){
		exc.printStackTrace();
		System.exit(1);
	    }
	}
    }

    /**
     * <p> Iterate over objects.</p>
     * 
     * @author jdp 
     */
    public final static class Enumerator1 implements java.util.Enumeration {
	Object[] target = null;
	int tc = 0, len = 0;
	public Enumerator1 ( Object[] target){
	    this( target, ((null == target)?(0):(target.length)));
	}
	public Enumerator1 ( Object[] target, int size){
	    super();
	    this.target = target;
	    this.len = size;
	}
	public boolean hasMoreElements(){
	    if (tc >= len)
		return false;
	    else {
		int tt = tc;
		Object ret = target[tt];
		while ( null == ret && tt < len && null == (ret = target[++tt]));
		if ( null == ret)
		    return false;
		else
		    return true;
	    }
	}
	public Object nextElement(){
	    if ( tc >= len)
		throw new java.util.NoSuchElementException();
	    else {
		Object ret = null;
		while ( tc < len && null == (ret = target[tc++]));
		if ( null == ret)
		    throw new java.util.NoSuchElementException();
		else
		    return ret;
	    }
	}
    }
    /**
     * <p> Iterate over integers.  Returns {@link java.lang.Integer}
     * objects.</p>
     * 
     * @author jdp
     */
    public final static class Enumerator2 implements java.util.Enumeration {
	int[] target = null;
	int tc = 0, len = 0;
	public Enumerator2 ( int[] target){
	    this( target, ((null == target)?(0):(target.length)));
	}
	public Enumerator2 ( int[] target, int size){
	    super();
	    this.target = target;
	    this.len = size;
	}
	public boolean hasMoreElements(){
	    if (tc >= len)
		return false;
	    else 
		return true;
	}
	public Object nextElement(){
	    if ( tc >= len)
		throw new java.util.NoSuchElementException();
	    else {
		int ret = target[tc++];
		return new java.lang.Integer(ret);
	    }
	}
    }
    /**
     * <p> Iterate over long integer keys as 31 bit integers using
     * Hash Mask.  Returns {@link java.lang.Integer} objects.</p>
     * 
     * @author jdp
     */
    public final static class Enumerator3 implements java.util.Enumeration {
	long[] target = null;
	int tc = 0, len = 0;
	public Enumerator3 ( long[] target){
	    this( target, ((null == target)?(0):(target.length)));
	}
	public Enumerator3 ( long[] target, int size){
	    super();
	    this.target = target;
	    this.len = size;
	}
	public boolean hasMoreElements(){
	    if (tc >= len)
		return false;
	    else 
		return true;
	}
	public Object nextElement(){
	    if ( tc >= len)
		throw new java.util.NoSuchElementException();
	    else {
		int ret = (int)(target[tc++] & HASH_MASK);
		return new java.lang.Integer(ret);
	    }
	}
    }

}
