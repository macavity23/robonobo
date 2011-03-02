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
 * <p> Hasharray mapping from int primitive to int primitive. </p>
 *
 * @see intmap
 * @see hasharray
 * 
 * @author jdp
 */
public class intint 
    extends hasharray
{
    public long[] keys = null;

    public int[] vals = null;

    public intint(int initial, float load){
	super(initial,load);
	this.keys = new long[this.table.grow];
	this.vals = new int[this.table.grow];
    }
    public intint(int initial){
	super(initial);
	this.keys = new long[this.table.grow];
	this.vals = new int[this.table.grow];
    }
    public intint(){
	super();
	this.keys = new long[this.table.grow];
	this.vals = new int[this.table.grow];
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

    public int[] valary(){
	return this._valary();
    }
    protected final int[] _valary(){
	int many = this.count;
	if ( 0 < many){
	    int[] vals = this.vals;
	    int[] ret = new int[many];
	    System.arraycopy(vals,0,ret,0,many);
	    return ret;
	}
	else
	    return null;
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
    public int lastValue(){
	return this._lastValue();
    }
    protected final int _lastValue(){
	int idx = (this.count-1);
	if (-1 < idx)
	    return this.vals[idx];
	else
	    return ZED;
    }
    public void lastValue( int val){
	this._lastValue(val);
    }
    protected final void _lastValue( int val){
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
	return new Enumerator2(this.vals, this.count);
    }
    public boolean containsValue(int value){
	return this.contains(value);
    }
    public boolean _containsValue(int value){
	return this._contains(value);
    }
    public boolean contains(int value){
	return this._contains(value);
    }
    protected final boolean _contains(int value){
	int sval, vals[] = this.vals;
	for (int ti = (this.count-1) ; 0 <= ti; ti--){
	    sval = vals[ti];
	    if (value == sval)
		return true;
	}
	return false;
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
	if (KL_NIL == key)
	    return -1;
	else 
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
    public int indexOfValue( int val){
	return this.indexOfValue(val,0);
    }
    public int indexOfValue( int val, int fromIdx){
	return this._indexOfValue(val,fromIdx);
    }
    protected final int _indexOfValue( int val, int fromIdx){
	int many = this.count;
	int vals[] = this.vals;
	if (-1 < fromIdx && fromIdx < many){
	    for ( int idx = fromIdx; idx < many; idx++){
		if (val == vals[idx])
		    return idx;
	    }
	}
	return -1;
    }
    public int lastIndexOfValue( int val){
	return this.lastIndexOfValue(val,(this.count-1));
    }
    public int lastIndexOfValue( int val, int fromIdx){
	return this._lastIndexOfValue(val,fromIdx);
    }
    protected final int _lastIndexOfValue( int val, int fromIdx){
	int many = this.count;
	int vals[] = this.vals;
	if (-1 < fromIdx && fromIdx < many){
	    for ( int idx = fromIdx; -1 < idx; idx--){
		if (val == vals[idx])
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
	else {
	    int kei = ((java.lang.Number)key).intValue();
	    int idx = this._indexOf(kei);
	    if (-1 < idx && idx < this.count){
		int re = this.vals[idx];
		return new java.lang.Integer(re);
	    }
	    else
		return null;
	}
    }
    public int get(int key){
	return this._get(key);
    }
    protected final int _get(int key){
	int idx = this._indexOf(key);
	if (-1 < idx && idx < this.count)
	    return this.vals[idx];
	else
	    return ZED;
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
	    return null;
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
    public int value(int idx){
	return this._value(idx);
    }
    protected final int _value(int idx){
	if (-1 < idx && idx < this.count)
	    return this.vals[idx];
	else
	    return ZED;
    }

    public int value(int idx, int value){
	return this._value(idx,value);
    }
    protected final int _value(int idx, int value){
	if (-1 < idx && idx < this.count)
	    return (this.vals[idx] = value);
	else
	    return ZED;
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
	else if (!(value instanceof java.lang.Number))
	    throw new IllegalArgumentException("value "+value.getClass());
	else {
	    int kei = ((java.lang.Number)key).intValue();
	    int val = ((java.lang.Number)value).intValue();
	    Index.Entry ent = this.table.store(this,kei);
	    int aryix = ent.aryix;
	    if (Index.Entry.XINIT == aryix){
		ent.aryix = this._vadd_(kei,val);
		return null;/*(new)*/
	    }
	    else {
		int old = this.vals[aryix];
		this.vals[aryix] = val;
		return new java.lang.Integer(old);
	    }
	}
    }
    public int put(int key, int value){
	return this._put(key,value);
    }
    protected final int _put(int key, int value){
	Index.Entry ent = this.table.store(this,key);
	int aryix = ent.aryix;
	if (Index.Entry.XINIT == aryix){
	    ent.aryix = this._vadd_(key,value);
	    return ZED;/*(new)*/
	}
	else {
	    int old = this.vals[aryix];
	    this.vals[aryix] = value;
	    return old;
	}
    }
    /**
     * Add a potentially duplicate key.  It enters the index (get- put
     * "key" interface) when it is unique, or as a duplicate when the
     * key (former) is removed.  
     */
    public int add( int key, int val){
	return this._add(key,val);
    }
    protected final int _add( int key, int val){
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
    public int insert( int idx, int key, int val){
	return this._insert(idx,key,val);
    }
    protected final int _insert( int idx, int key, int val){
	if (0 > idx)
	    return this.add(key,val);
	else {
	    long[] keys = this.keys;
	    int[] vals = this.vals;
	    int len = keys.length;
	    long[] kcopier = new long[len+1];// simple aglo: always grow by one
	    int[] vcopier = new int[len+1];// 
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
    public int replace( int idx, int nkey, int nval){
	return this._replace(idx,nkey,nval);
    }
    protected final int _replace( int idx, int nkey, int nval){
	if (0 > idx || idx >= this.count)
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
    public int append( int nkey, int nval){
	return this._append(nkey,nval);
    }
    protected final int _append( int nkey, int nval){
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
    /**
     * Add to data vectors, no index activity.  Manage optimistic
     * vectors.
     */
    protected final int _vadd_ ( int key, int val){
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
	    int[] v_copier = new int[nlen];
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
    public int drop( int idx){
	return this._drop(idx);
    }
    protected final int _drop( int idx){
	if (0 > idx || idx >= this.count)
	    return ZED;
	else {
	    long key = this.keys[idx];
	    if (KL_NIL == key)
		return ZED;
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
		    int old = this.vals[idx];
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
	    if (-1 < this.indexOf(kei)){
		int re = this.remove(kei);
		return new java.lang.Integer(re);
	    }
	    else
		return null;
	}
    }
    /**
     * Remove key and value, index any latter identical key, truncate
     * data arrays (keys and vals), scan index table and decrement
     * pointer indeces for truncated data arrays.  
     */
    public int remove( int key){
	return this._remove(key);
    }
    protected final int _remove( int key){
	Index.Entry dropped = this.table.remove(this,key);
	if (null != dropped){
	    int aryix = dropped.aryix;
	    if (-1 < aryix){
		long keys[] = this.keys;
		int vals[] = this.vals;
		int ret = vals[aryix];
		keys[aryix] = KL_NIL;
		vals[aryix] = ZED;
		shift(keys,aryix);
		shift(vals,aryix);
		this.count--;
		
		return ret;
	    }
	}
	return ZED;
    }
    public void clear(){
	this._clear();
    }
    protected final void _clear(){
	this.table.clear(this);
 	long[] keys = this.keys;
 	int[] vals = this.vals;
 	for (int index = 0, count = this.count; index < count; index++){
 	    keys[index] = KL_NIL;
 	    vals[index] = ZED;
 	}
	this.count = 0;
    }
    public final intint cloneIntint(){
	intint t = (intint)super.cloneHasharray();
	t.keys = (long[])this.keys.clone();
	t.vals = (int[])this.vals.clone();
	return t;
    }
}
