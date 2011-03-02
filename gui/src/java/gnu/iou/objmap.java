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
 * <p> Hash array structure maintains input order for indeces,
 * enumerations and arrays. </p>
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
 * @author John Pritchard 
 */
public class objmap 
    extends hasharray
    implements frame
{
    /**
     * <p> The keys and vals arrays are initialized by this
     * constructor, and are maintained internally as (parallel)
     * optimistically allocated buffers.</p>
     *
     * <p> Beware that the "keys" array is particularly sensitive as
     * index data.  Don't modify its values directly!</p>
     */
    protected Object[] keys ;
    /**
     * <p> The keys and vals arrays are initialized by this
     * constructor, and are maintained internally as (parallel)
     * optimistically allocated buffers.</p>
     *
     * <p> The length of keys and vals are always identical, and they
     * are maintained in parallel: the index into one corresponds with
     * the index into the other for a matching key- value pair. </p>
     */
    protected Object[] vals ;

    protected objmap frame_parent;

    public objmap(int initial, float load){
	super(initial,load);
	this.keys = new Object[this.table.grow];
	this.vals = new Object[this.table.grow];
    }
    public objmap(int initial){
	super(initial);
	this.keys = new Object[this.table.grow];
	this.vals = new Object[this.table.grow];
    }
    public objmap(){
	super();
	this.keys = new Object[this.table.grow];
	this.vals = new Object[this.table.grow];
    }

    public void destroy(){
	this.frame_parent = null;
	super.destroy();
    }
    public final objmap frameGet(){
	/*
	 * (obfmap frameInit) depends on this method being final: that
	 * the value returned here is identical to this.frame_parent
	 */
	return this.frame_parent;
    }
    public boolean frameStale(){
	if (null != this.frame_parent)
	    throw new java.lang.IllegalStateException("class use bug");
	else
	    return false;/*(no frame,- not stale)
			  */
    }
    public final boolean frameExists(){
	return (null != this.frame_parent);
    }
    public final boolean frameExistsNot(){
	return (null == this.frame_parent);
    }
    public frame frameParentGet(){
	return this.frame_parent;
    }
    public boolean frameParentSet(objmap pf){
	if (null == pf){
	    this.frame_parent = null;
	    return true;
	}
	else if (null == this.frame_parent){
	    if (this == pf)
		throw new java.lang.IllegalStateException("cyclic frame reference identical");
	    else {
		this.frame_parent = pf;
		return true;
	    }
	}
	else
	    return false;
    }
    public void frameParentReset(objmap pf){
	if (null == pf)
	    this.frame_parent = null;
	else {
	    if (this == pf)
		throw new java.lang.IllegalStateException("cyclic frame reference identical");
	    else 
		this.frame_parent = pf;
	}
    }

    public Object[] keyary(){
	return this._keyary();
    }
    protected final Object[] _keyary(){
	int many = this.count;
	if ( 0 < many){
	    Object[] keys = this.keys;
	    Object[] ret = new Object[many];
	    System.arraycopy(keys,0,ret,0,many);
	    return ret;
	}
	else
	    return null;
    }
    public Object[] keyary(Class arycla){
	return this._keyary(arycla);
    }
    protected final Object[] _keyary(Class arycla){
	if (null == arycla)
	    return this._keyary();
	else {
	    int many = this.count;
	    if ( 0 < many){
		Object[] keys = this.keys;
		Object[] ret = (Object[])java.lang.reflect.Array.newInstance(arycla,many);
		System.arraycopy(keys,0,ret,0,many);
		return ret;
	    }
	    else
		return null;
	}
    }
    public Object[] keyary_filter(Class arycla){
	return this._keyary_filter(arycla);
    }
    protected final Object[] _keyary_filter(Class arycla){
	if (null == arycla)
	    return this._keyary();
	else {
	    int many = this.count;
	    if ( 0 < many){
		Object keys[] = this.keys, key;
		Object[] ret = (Object[])java.lang.reflect.Array.newInstance(arycla,many);
		int rc = 0;
		for (int cc = 0; cc < many; cc++){
		    key = keys[cc];
		    if (null != key && arycla.isAssignableFrom(key.getClass()))
			ret[rc++] = key;
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
	    return this._valary();
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
	    return this._valary();
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
    public Object lastKey(){
	return this._lastKey();
    }
    protected final Object _lastKey(){
	int idx = (this.count-1);
	if (-1 < idx)
	    return this.keys[idx];
	else
	    return null;
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
    /**
     * Return an ordered enumeration of keys.
     */
    public java.util.Enumeration keys(){
	return this._keys();
    }
    protected final java.util.Enumeration _keys(){
	return new Enumerator1(this.keys, this.count);
    }
    public java.util.Enumeration elements(){
	return this._elements();
    }
    protected final java.util.Enumeration _elements(){
	return new Enumerator1(this.vals, this.count);
    }
    public boolean containsValue(Object value){
	return this._contains(value);
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
    public boolean containsKey(Object key){
	return this._containsKey(key);
    }
    protected final boolean _containsKey(Object key){
	return (null != this._lookup(key));
    }
    public int indexOf(Object key){
	return this._indexOf(key);
    }
    public int[] indexOfList(Object key){
	return this._indexOfList(key);
    }
    protected final int _indexOf(Object key){
	Index.Entry e = this._lookup(key);
	if (null == e)
	    return -1;
	else
	    return e.aryix;
    }
    protected final int[] _indexOfList(Object key){
	Index.Entry[] list = this._lookup_list(key);
	if (null == list)
	    return null;
	else {
	    int len = list.length;
	    int[] re = new int[len];
	    for (int idx = 0; idx < len; idx++)
		re[idx] = list[idx].aryix;
	    return re;
	}
    }
    public int indexOf( Object key, int fromIdx){
	return this._indexOf(key,fromIdx);
    }
    protected final int _indexOf( Object key, int fromIdx){
	if (null == key)
	    return -1;
	else {
	    int[] list = this._indexOfList(key);
	    if (null == list)
		return -1;
	    else {
		int len = list.length;
		for (int idx = 0, re; idx < len; idx++){
		    re = list[idx];
		    if (fromIdx <= re)
			return re;
		}
		return -1;
	    }
	}
    }
    public int lastIndexOf( Object key){
	return this._lastIndexOf(key);
    }
    protected final int _lastIndexOf( Object key){
	if (null == key)
	    return -1;
	else 
	    return this._lastIndexOf(key,(this.count-1));
    }
    public int lastIndexOf( Object key, int fromIdx){
	return this._lastIndexOf(key,fromIdx);
    }
    protected final int _lastIndexOf( Object key, int fromIdx){
	if (null == key)
	    return -1;
	else {
	    int[] list = this._indexOfList(key);
	    if (null == list)
		return -1;
	    else {
		int len = list.length;
		for (int idx = (len-1), re; -1 < idx; idx--){
		    re = list[idx];
		    if (fromIdx >= re)
			return re;
		}
		return -1;
	    }
	}
    }
    public int indexOfValue( Object val){
	return this._indexOfValue(val,0);
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
	return this._lastIndexOfValue(val,(this.count-1));
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
	return this._indexOfValueClass(sup,-1);
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
	return this._lastIndexOfValueClass(sup,Integer.MAX_VALUE);
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
	int idx = this._indexOf(key);
	if (-1 < idx && idx < this.count)
	    return this.vals[idx];
	else
	    return null;
    }
    public Object[] list(Object key){
	return this._list(key,null);
    }
    public Object[] list(Object key, Class comp){
	return this._list(key,comp);
    }
    protected Object[] _list(Object key, Class comp){
	int[] idxl = this._indexOfList(key);
	if (null == idxl)
	    return null;
	else {
	    int len = idxl.length;
	    Object[] list;
	    if (null == comp)
		list = new Object[len];
	    else
		list = (Object[])java.lang.reflect.Array.newInstance(comp,len);
	    for (int lidx = 0, vidx; lidx < len; lidx++){
		vidx = idxl[lidx];
		list[lidx] = this.vals[vidx];
	    }
	    return list;
	}
    }
    public Object keyO(int idx){
	return this._key(idx);
    }
    public Object key(int idx){
	return this._key(idx);
    }
    protected final Object _key(int idx){
	if (-1 < idx && idx < this.count)
	    return this.keys[idx];
	else
	    return null;
    }
    public long keyL(int idx){
	throw new java.lang.UnsupportedOperationException();
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
    /**
     * @param idx Key- value input- order index
     * @param value Replace existing value with this value
     * @return Replaced argument value, or null for bad index.  (Of
     * course, a null argument produces a null return value).
     */
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
    public int add( Object key, Object val){
	return this._add(key,val);
    }
    protected final int _add( Object key, Object val){
	if (null == key)
	    return -1;
	else {
	    Index.Entry ent = this.table.add(this,key);
	    int aryix = ent.aryix;
	    if (Index.Entry.XINIT == aryix){
		ent.aryix = this._vadd_(key,val);
		return ent.aryix;
	    }
	    else {
		Object old = this.vals[aryix];
		this.vals[aryix] = val;
		return aryix;
	    }
	}
    }
    /**
     * <p> Insert the argument pair.  If the key has been indexed
     * before, the new key is inserted into the index before it.</p>
     */
    public int insert( int idx, Object key, Object val){
	return this._insert(idx,key,val);
    }
    protected final int _insert( int idx, Object key, Object val){
	if (null == key || null == val)
	    return -1;
	else if (0 > idx)
	    return this.add(key,val);
	else {
	    
	    Object[] keys = this.keys;
	    Object[] vals = this.vals;
	    int len = keys.length;
	    Object[] kcopier = new Object[len+1];// simple aglo: always grow by one
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
    public int replace( int idx, Object nkey, Object nval){
	return this._replace(idx,nkey,nval);
    }
    protected final int _replace( int idx, Object nkey, Object nval){
	if (null == nkey || null == nval)
	    return -1;
	else if (0 > idx || idx >= this.count)
	    return this.add(nkey,nval);
	else {
	    Object okey = this.keys[idx];
	    if (okey.equals(nkey)){
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
    public int append( Object nkey, Object nval){
	return this._append(nkey,nval);
    }
    protected final int _append( Object nkey, Object nval){
	if (null == nkey || null == nval)
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
    protected final int _vadd_ ( Object key, Object val){
	int idx = this.count, len = this.keys.length;
	if ( idx >= (len-1)){
	    // grow rate
	    int grow = (len/3);
	    if (this.table.grow > grow) 
		grow = this.table.grow;
	    //
	    int nlen = len+(grow);
	    // grow keys
	    Object[] k_copier = new Object[nlen];
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
     * Remove element with known index.
     */
    public Object remove( int idx){
	return this._remove(idx);
    }
    protected final Object _remove( int idx){
	if (0 > idx)
	    return null;
	else {
	    Object key = this.keys[idx];
	    int kidx = this._indexOf(key);
	    if ( kidx == idx){
		/*
		 * (this.keys[idx]) is the first indexed key
		 */
		return this._remove(key);
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
    /**
     * Remove key and value, index any latter identical key, truncate
     * data arrays (keys and vals), scan index table and decrement
     * pointer indeces for truncated data arrays.  
     */
    public Object remove( Object key){
	return this._remove(key);
    }
    protected final Object _remove( Object key){
	if (null == key)
	    return null;
	else {
	    Index.Entry dropped = this.table.remove(this,key);
	    if (null != dropped){
		int aryix = dropped.aryix;
		if (-1 < aryix){
		    Object keys[] = this.keys;
		    Object vals[] = this.vals;
		    Object ret = vals[aryix];
		    keys[aryix] = null;
		    vals[aryix] = null;
		    shift(keys,aryix);
		    shift(vals,aryix);
		    this.count--;
	    
		    return ret;
		}
	    }
	    return null;
	}
    }
    public void clear(){
	this._clear();
    }
    protected final void _clear(){
	this.table.clear(this);
 	Object[] keys = this.keys, vals = this.vals;
	
 	for (int index = 0, count = this.count; index < count; index++){
 	    keys[index] = null;
 	    vals[index] = null;
 	}
	this.count = 0;
    }
    public final objmap cloneObjmap(){
	objmap t = (objmap)super.cloneHasharray();
	t.keys = (Object[])this.keys.clone();
	t.vals = (Object[])this.vals.clone();
	return t;
    }
}
