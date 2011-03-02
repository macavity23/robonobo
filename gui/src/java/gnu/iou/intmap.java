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
 * Integer map for an efficient and compact map from integers to
 * objects.  Maintains input order for keys and values.  The integer
 * value <tt>`Long.MIN_VALUE' (0xffffffffffffffff)</tt> is a special
 * <tt>`NIL'</tt> key value that cannot be used as a key, it
 * represents the empty (or "undefined") key.  In normal usage it is
 * not possible to employ the NIL key as keys are 32 bit integers.
 * 
 * <pre>
 * STANDARD DICTIONARY API
 * 
 * Object put (Object key, Object value)
 * 
 * Object get (Object key)
 * 
 * INTMAP DICTIONARY API
 * 
 * Object put (int key, Object value)
 * 
 * Object get (int key)
 *
 * </pre>
 * 
 * <p> The standard dictionary API simply wraps the int- key
 * dictionary API.  Users should not use the standard dictionary API
 * unless they have object keys in memory already.  The principal
 * purpose of intmap is for applications that need to avoid creating
 * objects for keys that are integer numbers using a java Hashtable.
 * 
 * <p> A good object key should be an <tt>`Integer'</tt> class object,
 * or any <tt>`Number'</tt> subclass, <tt>`Character'</tt> class
 * object, or have a hashCode value that is consistent with respect to
 * the semantic value (content) of the object.  For example, both the
 * AWT <tt>`Color'</tt> class and <tt>`InetAddress'</tt> class make
 * good keys for this table because their hashcodes are a consistent
 * and semantically meaningful value (a color value and an IP address,
 * respectively).
 * 
 * <p> For good keys, the intmap is an excellent lookup table,
 * performing marginally faster than a java Hashtable, while having a
 * heavier memory footprint (with its <tt>`keys'</tt> and
 * <tt>`vals'</tt> arrays in addition to the index table).  The intmap
 * weighs more than the java Hashtable, but is faster and provides
 * input- ordered output (storage).
 * 
 * <p> Most classes of object keys will be used solely for an
 * ambiguous <tt>`hashCode'</tt>, which in most cases will lead to
 * unpredictable and undesireable behavior, and cannot be recommended.
 * 
 * <p> The <tt>`String'</tt> class is a very bad intmap key because
 * its hashcode can represent only part of the identity of the
 * semantic value, or content, of most strings (ones more than four
 * bytes long).  However, even short strings of four or fewer
 * characters are not good intmap keys because the string's hash
 * function is not "crisp" or "tight".
 * 
 * <p><b>API Caution</b>
 *
 * <p> Note that <i>indeces</i> and <i>keys</i> are two distinct and
 * separate spaces.  An integer key must be mapped into an integer
 * index in order to access the <tt>`keys'</tt> and <tt>`vals'</tt>
 * arrays (the function <tt>`indexOf(int)'</tt> performs the mapping
 * from key to index).  An index directly retrieves a key or value
 * from the <tt>`keys'</tt> and <tt>`vals'</tt> arrays.
 * 
 * <p><b>Not Synchronized</b>
 *
 * <p> Note that the `intmap' is not multi-thread safe.  This class is
 * optimized for high frequency, single- threaded use as required for
 * thread specific data structures.
 *
 * @author John Pritchard */
public class intmap 
    extends hasharray
{
    public long[] keys = null;

    public Object[] vals = null;

    public intmap(int initial, float load){
	super(initial,load);
	this.keys = new long[this.table.grow];
	this.vals = new Object[this.table.grow];
    }
    public intmap(int initial){
	super(initial);
	this.keys = new long[this.table.grow];
	this.vals = new Object[this.table.grow];
    }
    public intmap(){
	super();
	this.keys = new long[this.table.grow];
	this.vals = new Object[this.table.grow];
    }

    public int[] keyary(){
	return this._keyary();
    }
    protected final int[] _keyary(){
	int many = this.count;
	if ( 0 < many){
	    long[] keys = this.keys;
	    int[] ret = new int[many];
	    for (int idx = 0; idx < many; idx++)
		ret[idx] = (int)(keys[idx] & 0xffffffffL);
	    return ret;
	}
	else
	    return null;
    }

    public Object[] valary(){
	return this._valary();
    }
    protected final Object[] _valary(){
	int many = this.count;
	if ( 0 < many){
	    Object[] vals = this.vals;
	    Object[] ret = new Object[many];
	    System.arraycopy(vals,0,ret,0,many);
	    return ret;
	}
	else
	    return null;
    }
    public Object[] valary(Class arycla){
	return this._valary(arycla);
    }
    protected final Object[] _valary(Class arycla){
	if (null == arycla)
	    return this.valary();
	else {
	    int many = this.count;
	    if ( 0 < many){
		Object[] vals = this.vals;
		Object[] ret = (Object[])java.lang.reflect.Array.newInstance(arycla,many);
		System.arraycopy(vals,0,ret,0,many);
		return ret;
	    }
	    else
		return null;
	}
    }
    public Object[] valary_filter(Class arycla){
	return this._valary_filter(arycla);
    }
    protected final Object[] _valary_filter(Class arycla){
	if (null == arycla)
	    return this.valary();
	else {
	    int many = this.count;
	    if ( 0 < many){
		Object vals[] = this.vals, val;
		Object[] ret = (Object[])java.lang.reflect.Array.newInstance(arycla,many);
		int rc = 0;
		for (int cc = 0; cc < many; cc++){
		    val = vals[cc];
		    if (null != val && arycla.isAssignableFrom(val.getClass()))
			ret[rc++] = val;
		}
		if (1 > rc)
		    return null;
		else {
		    if (many != rc){
			Object[] trunc = (Object[])java.lang.reflect.Array.newInstance(arycla,rc);
			System.arraycopy(ret,0,trunc,0,rc);
			return trunc;
		    }
		    else
			return ret;
		}
	    }
	    else
		return null;
	}
    }
    public long lastKey(){
	return this._lastKey();
    }
    protected final long _lastKey(){
	int idx = (this.count-1);
	if (-1 < idx)
	    return this.keys[idx];
	else
	    return KL_NIL;
    }
    public Object lastValue(){
	return this._lastValue();
    }
    protected final Object _lastValue(){
	int idx = (this.count-1);
	if (-1 < idx)
	    return this.vals[idx];
	else
	    return null;
    }
    public void lastValue( Object val){
	this._lastValue(val);
    }
    protected final void _lastValue( Object val){
	int idx = (this.count-1);
	if (-1 < idx)
	    this.vals[idx] = val;
    }
    public java.util.Enumeration keys(){
	return this._keys();
    }
    protected final java.util.Enumeration _keys(){
	return new Enumerator3(this.keys, this.count);
    }
    public java.util.Enumeration elements(){
	return this._elements();
    }
    protected final java.util.Enumeration _elements(){
	return new Enumerator1(this.vals, this.count);
    }
    public boolean containsValue(Object value){
	return this.contains(value);
    }
    public boolean _containsValue(Object value){
	return this._contains(value);
    }
    public boolean contains(Object value){
	return this._contains(value);
    }
    protected final boolean _contains(Object value){
	if (value == null) 
	    return false;
	else {
	    Object sval, vals[] = this.vals;
	    for (int ti = (this.count-1) ; 0 <= ti; ti--){
		sval = vals[ti];
		if (null != sval && (value == sval || sval.equals(value))){
		    return true;
		}
	    }
	    return false;
	}
    }
    public boolean containsKey(int key){
	return this._containsKey(key);
    }
    protected final boolean _containsKey(int key){
	return (null != this._lookup(key));
    }
    public int indexOf(int key){
	return this._indexOf(key);
    }
    protected final int _indexOf(int key){
	Index.Entry e = this._lookup(key);
	if (null == e)
	    return -1;
	else
	    return e.aryix;
    }
    public int indexOf( int key, int fromIdx){
	return this._indexOf(key,fromIdx);
    }
    protected final int _indexOf( int key, int fromIdx){
	long lkey = key;
	int many = this.count;
	long keys[] = this.keys;
	if (-1 < fromIdx && fromIdx < many){
	    for ( int idx = fromIdx; idx < many; idx++){
		if (lkey == keys[idx])
		    return idx;
	    }
	}
	return -1;
    }
    public int lastIndexOf( int key){
	return this._lastIndexOf(key);
    }
    protected final int _lastIndexOf( int key){

	return this.lastIndexOf(key,(this.count-1));
    }
    public int lastIndexOf( int key, int fromIdx){
	return this._lastIndexOf(key,fromIdx);
    }
    protected final int _lastIndexOf( int key, int fromIdx){
	long lkey = key;
	int many = this.count;
	long keys[] = this.keys;
	if (-1 < fromIdx && fromIdx < many){
	    for ( int idx = fromIdx; -1 < idx; idx--){
		if (lkey == keys[idx])
		    return idx;
	    }
	}
	return -1;
    }
    public int indexOfValue( Object val){
	return this.indexOfValue(val,0);
    }
    public int indexOfValue( Object val, int fromIdx){
	return this._indexOfValue(val,fromIdx);
    }
    protected final int _indexOfValue( Object val, int fromIdx){
	if (null == val)
	    return -1;
	else {
	    int many = this.count;
	    Object vals[] = this.vals;
	    if (-1 < fromIdx && fromIdx < many){
		for ( int idx = fromIdx; idx < many; idx++){
		    if (val == vals[idx])
			return idx;
		}
		return -1;
	    }
	    else
		return -1;
	}
    }
    public int lastIndexOfValue( Object val){
	return this.lastIndexOfValue(val,(this.count-1));
    }
    public int lastIndexOfValue( Object val, int fromIdx){
	return this._lastIndexOfValue(val,fromIdx);
    }
    protected final int _lastIndexOfValue( Object val, int fromIdx){
	if (null == val)
	    return -1;
	else {
	    int many = this.count;
	    Object vals[] = this.vals;
	    if (-1 < fromIdx && fromIdx < many){
		for ( int idx = fromIdx; -1 < idx; idx--){
		    if (val == vals[idx])
			return idx;
		}
		return -1;
	    }
	    else
		return -1;
	}
    }
    public int indexOfValueClass( Class sup){
	return this.indexOfValueClass(sup,-1);
    }
    public int indexOfValueClass( Class sup, int from){
	return this._indexOfValueClass(sup,from);
    }
    protected final int _indexOfValueClass( Class sup, int from){
	int count = this.count;
	Object vals[] = this.vals, val;
	if (null != sup && from < count){
	    if (0 > from) from = 0;
	    for (int idx = from; idx < count; idx++){
		val = vals[idx];
		if (null != val && sup.isAssignableFrom(val.getClass()))
		    return idx;
	    }
	}
	return -1;
    }
    public int lastIndexOfValueClass( Class sup){
	return this.lastIndexOfValueClass(sup,Integer.MAX_VALUE);
    }
    public int lastIndexOfValueClass( Class sup, int from){
	return this._lastIndexOfValueClass(sup,from);
    }
    protected final int _lastIndexOfValueClass( Class sup, int from){
	int count = this.count;
	Object vals[] = this.vals, val;
	if (null != sup && -1 < from){
	    if (from >= count) from = (count-1);
	    for (int idx = from; -1 < idx; idx--){
		val = vals[idx];
		if (null != val && sup.isAssignableFrom(val.getClass()))
		    return idx;
	    }
	}
	return -1;
    }
    public Object get(Object key){
	return this._get(key);
    }
    protected final Object _get(Object key){
	if (null == key)
	    return null;
	else if (!(key instanceof java.lang.Number))
	    throw new IllegalArgumentException("key "+key.getClass());
	else
	    return this.get( ((java.lang.Number)key).intValue());
    }
    public Object get(int key){
	int idx = this.indexOf(key);
	if (-1 < idx && idx < this.count)
	    return this.vals[idx];
	else
	    return null;
    }
    protected final Object _get(int key){
	int idx = this._indexOf(key);
	if (-1 < idx && idx < this.count)
	    return this.vals[idx];
	else
	    return null;
    }
    public Object keyO(int idx){
	return this._keyO(idx);
    }
    protected final Object _keyO(int idx){
	if (-1 < idx && idx < this.count){
	    long lkey = this.keys[idx];
	    if (KL_NIL == lkey)
		return null;
	    else
		return new java.lang.Integer( (int)lkey);
	}
	else
	    return KO_NIL;
    }
    public int key(int idx){
	return this._key(idx);
    }
    protected final int _key(int idx){
	if (-1 < idx && idx < this.count){
	    long lkey = this.keys[idx];
	    if (KL_NIL == lkey)
		return ZED;
	    else
		return (int)lkey;
	}
	else
	    return ZED;
    }
    public long keyL(int idx){
	return this._keyL(idx);
    }
    protected final long _keyL(int idx){
	if (-1 < idx && idx < this.count)
	    return this.keys[idx];
	else
	    return KL_NIL;
    }
    public Object value(int idx){
	return this._value(idx);
    }
    protected final Object _value(int idx){
	if (-1 < idx && idx < this.count)
	    return this.vals[idx];
	else
	    return null;
    }

    public Object value(int idx, Object value){
	return this._value(idx,value);
    }
    protected final Object _value(int idx, Object value){
	if (-1 < idx && idx < this.count)
	    return (this.vals[idx] = value);
	else
	    return null;
    }
    /**
     * Replace the keyed value
     */
    public Object put(Object key, Object value){
	return this._put(key,value);
    }
    protected final Object _put(Object key, Object value){
	if (null == key)
	    return null;
	else if (!(key instanceof java.lang.Number))
	    throw new IllegalArgumentException("key "+key.getClass());
	else
	    return this.put( ((java.lang.Number)key).intValue(), value);
    }
    public Object put(int key, Object value){
	return this._put(key,value);
    }
    protected final Object _put(int key, Object value){
	Index.Entry ent = this.table.store(this,key);
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
    /**
     * Add a potentially duplicate key.  It enters the index (get- put
     * "key" interface) when it is unique, or as a duplicate when the
     * key (former) is removed.  
     */
    public int add( int key, Object val){
	return this._add(key,val);
    }
    protected final int _add( int key, Object val){
	Index.Entry ent = this.table.add(this,key);
	int aryix = ent.aryix;
	if (Index.Entry.XINIT == aryix){
	    ent.aryix = this._vadd_(key,val);
	    return ent.aryix;
	}
	else {
	    this.vals[aryix] = val;
	    return aryix;
	}
    }
    /**
     * <p> Insert the argument pair.  If the key has been indexed
     * before, the new key is inserted into the index before it.</p>
     */
    public int insert( int idx, int key, Object val){
	return this._insert(idx,key,val);
    }
    protected final int _insert( int idx, int key, Object val){
	if (null == val)
	    return -1;
	else if (0 > idx)
	    return this.add(key,val);
	else {
	    long[] keys = this.keys;
	    Object[] vals = this.vals;
	    int len = keys.length;
	    long[] kcopier = new long[len+1];// simple aglo: always grow by one
	    Object[] vcopier = new Object[len+1];// 
	    if ( 0 == idx){
		System.arraycopy(keys,0,kcopier,1,len);
		System.arraycopy(vals,0,vcopier,1,len);
		kcopier[idx] = key;
		vcopier[idx] = val;
	    }
	    else if (idx == (len-1)){
		System.arraycopy(keys,0,kcopier,0,len);
		System.arraycopy(vals,0,vcopier,0,len);
		kcopier[idx] = key;
		vcopier[idx] = val;
	    }
	    else {
		System.arraycopy(keys,0,kcopier,0,idx);
		System.arraycopy(vals,0,vcopier,0,idx);
		System.arraycopy(keys,idx,kcopier,idx+1,(len-idx));//copied (many=idx) above
		System.arraycopy(vals,idx,vcopier,idx+1,(len-idx));
		kcopier[idx] = key;
		vcopier[idx] = val;
	    }
	    this.keys = kcopier;
	    this.vals = vcopier;
	    this.count += 1;
	    Index.Entry ent = this.table.insert(this,idx,key);
	    
	    if (Index.Entry.XINIT == ent.aryix){
		ent.aryix = idx;
		return idx;
	    }
	    else
		throw new IllegalStateException("BBBUGGGG");
	}
    }
    /**
     * <p> Replace the key- value pair at key- value index 'idx' with
     * the argument pair.</p>
     */
    public int replace( int idx, int nkey, Object nval){
	return this._replace(idx,nkey,nval);
    }
    protected final int _replace( int idx, int nkey, Object nval){
	if (null == nval)
	    return -1;
	else if (0 > idx || idx >= this.count)
	    return this.add(nkey,nval);
	else {
	    long okey = this.keys[idx];
	    if (okey == nkey){
		this.vals[idx] = nval;
		return idx;
	    }
	    else {
		Index.Entry ent = this.table.replace(this,idx,nkey,okey);
		ent.aryix = idx;
		this.keys[idx] = nkey;
		this.vals[idx] = nval;
		return idx;
	    }
	}
    }
    /**
     * <p> Append new key- value pair.  If the key has been indexed
     * before, the new key is appended into the index after it. </p>
     */
    public int append( int nkey, Object nval){
	return this._append(nkey,nval);
    }
    protected final int _append( int nkey, Object nval){
	if (null == nval)
	    return -1;
	else {
	    int idx = this._indexOf(nkey);
	    if (0 > idx)
		return this.add(nkey,nval);
	    else {
		idx = this._vadd_(nkey,nval);
		//
		Index.Entry ent = this.table.append(this,nkey);
		ent.aryix = idx;
		return idx;
	    }
	}
    }
    /**
     * Add to data vectors, no index activity.  Manage optimistic
     * vectors.
     */
    protected final int _vadd_ ( int key, Object val){
	int idx = this.count, len = this.keys.length;
	if ( idx >= (len-1)){
	    // grow rate
	    int grow = (len/3);
	    if (this.table.grow > grow) 
		grow = this.table.grow;
	    //
	    int nlen = len+(grow);
	    // grow keys
	    long[] k_copier = new long[nlen];
	    System.arraycopy(keys,0,k_copier,0,len);
	    keys = k_copier;
	    
	    // grow vals
	    Object[] v_copier = new Object[nlen];
	    System.arraycopy(vals,0,v_copier,0,len);
	    vals = v_copier;
	}
	this.keys[idx] = key;
	this.vals[idx] = val;
	this.count += 1;
	return idx;
    }
    /**
     * Drop slot by index
     */
    public Object drop( int idx){
	return this._drop(idx);
    }
    protected final Object _drop( int idx){
	if (0 > idx)
	    return null;
	else {
	    long key = this.keys[idx];
	    if (KL_NIL == key)
		return null;
	    else {
		int ikey = (int)key;
		int kidx = this._indexOf(ikey);
		if ( kidx == idx){
		    /*
		     * (this.keys[idx]) is the first indexed key
		     */
		    return this._remove(ikey);
		}
		else {
		    if (-1 < kidx)
			/*
			 * (this.keys[idx]) is not the first indexed key
			 */
			this.table.remove(this,idx);
		    Object old = this.vals[idx];
		    shift(this.keys,idx);
		    shift(this.vals,idx);
		    this.count--;
		    return old;
		}
	    }
	}
    }
    public Object remove( Object key){
	return this._remove(key);
    }
    protected final Object _remove( Object key){
	if (null == key)
	    return null;
	else if (!(key instanceof java.lang.Number))
	    throw new java.lang.IllegalArgumentException("key "+key.getClass());
	else {
	    int kei = ((java.lang.Number)key).intValue();
	    return this.remove(kei);
	}
    }
    /**
     * Remove key and value, index any latter identical key, truncate
     * data arrays (keys and vals), scan index table and decrement
     * pointer indeces for truncated data arrays.  
     */
    public Object remove( int key){
	return this._remove(key);
    }
    protected final Object _remove( int key){
	Index.Entry dropped = this.table.remove(this,key);
	if (null != dropped){
	    int aryix = dropped.aryix;
	    if (-1 < aryix){
		long keys[] = this.keys;
		Object vals[] = this.vals;
		Object ret = vals[aryix];
		keys[aryix] = KL_NIL;
		vals[aryix] = null;
		shift(keys,aryix);
		shift(vals,aryix);
		this.count--;
		
		return ret;
	    }
	}
	return null;
    }
    public void clear(){
	this._clear();
    }
    protected final void _clear(){
	this.table.clear(this);
 	long[] keys = this.keys;
 	Object[] vals = this.vals;
	
 	for (int index = 0, count = this.count; index < count; index++){
 	    keys[index] = KL_NIL;
 	    vals[index] = null;
 	}
	this.count = 0;
    }
    public final intmap cloneIntmap(){
	intmap t = (intmap)super.cloneHasharray();
	t.keys = (long[])this.keys.clone();
	t.vals = (Object[])this.vals.clone();
	return t;
    }
}
