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
package gnu.iou;

/**
 * <p> A subclass of {@link objmap} for frame relations.  A {@link
 * frame} relation links a child hash array to a parent hash array for
 * collisions and distinctions.  The code here depends on {@link
 * frame} class values that link themselves independently, and that
 * these links are in the subject maps.  The {@link obfmap} is highly
 * dependent on the {@link hasharray}, while highly independent of the
 * linking that its applications may implement.  </p>
 * 
 * <p> The basic hash array is the class {@link objmap} implementing
 * {@link frame}.  The {@link objmap} can be the parent (target) of a
 * frame relation, but not  </p>
 * 
 * <p> This frame map maintains a {@link obfmap$framelist} mapping the
 * frame relation sources with targets.  </p>
 * 
 * @author jdp
 */
public class obfmap 
    extends objmap
{
    public final static java.lang.Class CLA_NIL = null;

    /**
     * <p> Index set describes the client exposed nodeset in a frame
     * relation.  </p>
     * 
     * <h3>Structure</h3>
     * 
     * <p> The integer value lists <i>xfc</i> and <i>xfp</i> are
     * maintained in parallel.  Only the keys of these
     * {@link gnu.iou.intint} maps are used.  Their values 
     * are ignored.  </p>
     * 
     * <p> The <b>xfc</b> array is constructed in node array order by
     * {@link #frameInit}.  This is a list of negative one for no client
     * frame member in this slot, or an index value greater than
     * negative one for an index into the client frame.  </p>
     * 
     * <p> The <b>xfp</b> array is parallel, indexing members of the
     * parent frame.  </p>
     * 
     * @author jdp
     */
    public final static class framelist
	extends java.lang.Object
	implements java.lang.Cloneable
    {
	private boolean stale =  true;
	/**
	 * List parallel with xfp in global list order.  If greater
	 * than negative one, the parent frame index value.
	 */
	private intint xfp = new intint();
	/**
	 * List parallel with xfc in global list order.  If greater
	 * than negative one, the child frame index value.
	 */
	private intint xfc = new intint();

	framelist(){
	    super();
	}
	protected Object clone(){
	    return this.cloneFramelist();
	}
	public framelist cloneFramelist(){
	    try {
		framelist clone = (framelist)super.clone();
		clone.xfp = this.xfp.cloneIntint();
		clone.xfc = this.xfc.cloneIntint();
		return clone;
	    }
	    catch (java.lang.CloneNotSupportedException cns){
		throw new java.lang.IllegalStateException();
	    }
	}
	int size(){
	    return this.xfc.size();
	}
	void stale(){
	    this.stale = true;
	}
	boolean ok(){
	    return (!this.stale);
	}
	boolean nok(){
	    return (this.stale);
	}
	/** <p> As <code>reset(false)</code>. </p> 
	 */
	synchronized void reset(){
	    this.stale = true;
	    this.xfc.clear();
	    this.xfp.clear();
	    this.stale = false;
	}
	synchronized void reset(boolean stale){
	    this.stale = true;
	    this.xfc.clear();
	    this.xfp.clear();
	    this.stale = stale;
	}
	/**
	 * @param gidx Called in global index sequence from zero to
	 * global (linked + exposed) set length
	 * @param fp_idx Greater than negative one parent frame index for gidx
	 * @param fc_idx Greater than negative one client frame index for gidx
	 */
	void update( int gidx, int fp_idx, int fc_idx){
	    int sz = this.size();
	    if (gidx != sz){
		if (-1 < gidx && gidx < sz){
		    this.xfc.replace(gidx,fc_idx,fc_idx);
		    this.xfp.replace(gidx,fp_idx,fp_idx);
		    return;
		}
		else
		    throw new java.lang.IllegalStateException("bug:gidx "+gidx+" != "+sz);
	    }
	    else {
		if ((-1 < fp_idx)&&(this.hasPxNot(fp_idx))){
		    this.xfc.add(fc_idx,fc_idx);
		    this.xfp.add(fp_idx,fp_idx);
		}
		else {
		    if (0 > fc_idx)
			return;
		    else {
			this.xfc.add(fc_idx,fc_idx);
			this.xfp.add(-1,-1);
		    }
		}
	    }
	}
	/**
	 */
	void update_fin(){
	    this.stale = false;
	}
	/** @return Whether this parent frame slot index is exposed or
	 * overridden by a client frame slot.
	 */
	boolean hasPx( int px){
	    return (-1 < this.xfp.indexOf(px));
	}
	boolean hasPxNot( int px){
	    return (0 > this.xfp.indexOf(px));
	}
	/** @return Whether this child frame index overrides a slot in
	 * the parent frame
	 */
	boolean hasCx( int cx){
	    return (-1 < this.xfc.indexOf(cx));
	}
	boolean hasCxNot( int cx){
	    return (0 > this.xfc.indexOf(cx));
	}
	/** @return Given a parent frame index, return the overriding
	 * child frame slot index or negative one
	 */
	int cx4px( int px){
	    int gidx = this.xfp.indexOf(px);
	    if (-1 < gidx)
		return this.xfc.key(gidx);
	    else
		return -1;
	}
	/** @return Given a child frame index, return the linked
	 * parent frame slot index or negative one
	 */
	int px4cx( int cx){
	    int gidx = this.xfc.indexOf(cx);
	    if (-1 < gidx)
		return this.xfp.key(gidx);
	    else
		return -1;
	}
	/**
	 * @return Global index has parent member not overridden
	 */
	boolean infp(int gidx){
	    /*(not over- ridden)
	     */
	    return (-1 < this.px4gx(gidx))&&(0 > this.cx4gx(gidx));
	}
	/**
	 * @return Global index has client member
	 */
	boolean infc(int gidx){
	    /*(present)
	     */
	    return (-1 < this.cx4gx(gidx));
	}
	/** @return Parent frame index for global index
	 */
	int px4gx(int gidx){
	    if (-1 < gidx && gidx < this.size())
		return this.xfp.key(gidx);
	    else
		return -1;
	}
	/** @return Child frame index for global index
	 */
	int cx4gx(int gidx){
	    if (-1 < gidx && gidx < this.size())
		return this.xfc.key(gidx);
	    else
		return -1;
	}

	private int x4gxf(boolean fc, int gidx, boolean asc){
	    if (-1 < gidx && gidx < this.size()){
		int tmpi;
		if (fc)
		    tmpi = this.xfc.key(gidx);
		else
		    tmpi = this.xfp.key(gidx);
		//
		if (-1 < tmpi)
		    return tmpi;
		else {
		    /*
		     * This can be non-linear and so can cause
		     * infinite loops -- a monotonic algo will take
		     * more time than available today -- don't think
		     * this code is actually used in practice.
		     */
		    int sz = this.size();
		    if (asc){
			if (0 == gidx)
			    return 0;
			else {
			    for (int test = gidx; test < sz; test++){
				if (fc)
				    tmpi = this.xfc.key(test);
				else
				    tmpi = this.xfp.key(test);
				//
				if (-1 < tmpi)
				    return tmpi;
			    }
			    return sz;
			}
		    }
		    else {
			if (gidx >= (sz-1))
			    return gidx;
			else {
			    for (int test = gidx; -1 < test; test--){
				if (fc)
				    tmpi = this.xfc.key(test);
				else
				    tmpi = this.xfp.key(test);
				//
				if (-1 < tmpi)
				    return tmpi;
			    }
			    return 0;
			}
		    }
		}
	    }
	    else
		return -1;
	}
	/** @return Parent frame region index for global index
	 */
	int px4gxf(int gidx, boolean asc){
	    return this.x4gxf(false,gidx,asc);
	}
	/** @return Child frame region index for global index
	 */
	int cx4gxf(int gidx, boolean asc){
	    return this.x4gxf(true,gidx,asc);
	}
	/** @return Global index for child frame index
	 */
	int gx4cx(int fcx){
	    if (0 > fcx)
		return -1;
	    else 
		return this.xfc.indexOf(fcx);
	}
	/** @return Global index for parent frame index
	 */
	int gx4px(int fpx){
	    if (0 > fpx)
		return -1;
	    else 
		return this.xfp.indexOf(fpx);
	}
	void drop(int gidx){
	    this.xfc.drop(gidx);
	    this.xfp.drop(gidx);
	}
    }



    private framelist frame_distinct;

    public obfmap(int init, float load){
	super(init,load);
    }
    public obfmap(int init){
	super(init);
    }
    public obfmap(){
	super();
    }
    /**
     * @param copy Copy into this object (calls this "add")
     */
    public obfmap(objmap copy){
	super();
	this.add(copy);
    }

    public void clear(){
	if (null != this.frame_distinct)
	    this.frame_distinct.reset();
	super.clear();
    }
    private void stale(){
	if (null != this.frame_parent){
	    if (null == this.frame_distinct)
		this.frame_distinct = new framelist();
	    else
		this.frame_distinct.stale = true;
	}
    }
    public final boolean frameStale(){
	if (null != this.frame_parent){
	    if ((null == this.frame_distinct)||(this.frame_distinct.nok()))
		return true;//(known stale)
	    else
		return this.frame_parent.frameStale();//(may be stale)
	}
	else
	    return false;//(cant be stale, with distinct corner case ok for init)
    }
    public final boolean frameStaleNot(){
	return (!this.frameStale());
    }
    public final boolean frameParentSet(objmap pf){
	if (null == pf){
	    this.frame_parent = null;
	    if (null != this.frame_distinct)
		this.frame_distinct.reset();
	    return true;
	}
	else if (null == this.frame_parent){
	    if (this == pf)
		throw new java.lang.IllegalStateException("cyclic frame reference identical");
	    else {
		this.frame_parent = pf;
		if (null != this.frame_distinct)
		    this.frame_distinct.reset(true);
		return true;
	    }
	}
	else
	    return false;
    }
    public final void frameParentReset(objmap pf){
	if (null == pf){
	    this.frame_parent = null;
	    if (null != this.frame_distinct)
		this.frame_distinct.reset();
	}
	else {
	    if (this == pf)
		throw new java.lang.IllegalStateException("cyclic frame reference identical");
	    else {
		if (null != this.frame_distinct)
		    this.frame_distinct.reset(true);
		this.frame_parent = pf;
	    }
	}
    }
    public final void frameInit(){
	objmap fp = this.frameGet();
	if (null == fp)
	    return;
	else if (null == this.frame_distinct)
	    this.frame_distinct = new framelist();
	else
	    this.frame_distinct.reset(true);//(true) := see (this.frame_distinct.update_fin())
	//
	try {
	    this.frame_parent = null;//(obscure [this.frame_distinct] during update)
	    //
	    int fp_len = fp.size();
	    int fc_len = this._size();
	    //
	    Object fcv, fck;
	    int fp_idx = 0, fc_idx = 0, fp_test_idx, g_test_idx, fc_test_idx;
	    frame child, parent, fp_test;
	    int gidx = 0;
	    /*
	     * Index Frame (parent) Map (children)
	     * directly.
	     */
	    for (; ; gidx++){
		if (fp_idx < fp_len)
		    this.frame_distinct.update(gidx,fp_idx++,-1);//(no fc, unique fp)
		else
		    break;
	    }
	    /*
	     * Index Frame (child) Map (children)
	     * by editing Frame (parent) slots.
	     */
	    for (; ; gidx++){
		if (fc_idx < fc_len){
		    fcv = this.value(fc_idx);
		    fck = this.key(fc_idx);
		    if (fcv instanceof frame){
			child = (frame)fcv;
			fp_test = child.frameParentGet();
			if (null != fp_test){/*(collision [child frame overriding parent frame])
					      */
			    fp_test_idx = fp.indexOfValue(fp_test);/*(test for collision @ [fp_test_idx,fc_idx])
								    */
			    if (0 > fp_test_idx)
				this.frame_distinct.update(gidx,-1,fc_idx++);//(collision off frame)
			    else {
				g_test_idx = this.frame_distinct.gx4px(fp_test_idx);
				if (-1 < g_test_idx){
				    fc_test_idx = this.frame_distinct.cx4gx(g_test_idx);

				    if (0 > fc_test_idx){/*(update previous for found collision: 
							  * pull fc into fp override position)
							  */
					this.frame_distinct.update(g_test_idx,fp_test_idx,fc_idx++);
					gidx -= 1;
				    }
				    else {
					/*(duplicate collision)
					 */
					this.frame_distinct.update(gidx,fp_test_idx,fc_idx++);
				    }
				}
				else
				    this.frame_distinct.update(gidx,fp_test_idx,fc_idx++);//(simple collision)
			    }
			}
			else 
			    this.frame_distinct.update(gidx,-1,fc_idx++);//(no fp, unique fc)
		    }
		    else
			this.frame_distinct.update(gidx,-1,fc_idx++);//(no fp, unique fc)
		}//(fc_idx < fc_len)
		else
		    break;
	    }//(for gidx)
	    this.frame_distinct.update_fin();
	}
	finally {
	    this.frame_parent = fp;
	}
    }
    protected final Object[] frary(Class comp, boolean keys, boolean filter){
	if (null == this.frameGet())
	    throw new java.lang.IllegalStateException("bug:missing-frame");
	else if (this.frameStale())
	    throw new java.lang.IllegalStateException("bug:frame-stale");
	else {
	    int glen = this.frame_distinct.size();
	    Object test, gary[] = grow(null,glen,comp);
	    for (int gidx = 0; gidx < glen; gidx++){
		if (keys)
		    test = this.key(gidx);
		else
		    test = this.value(gidx);
		//
		if (null == test)
		    throw new java.lang.IllegalStateException("bug:null-pointer @ "+gidx+" in "+glen);
		else if (filter){
		    if (comp.isAssignableFrom(test.getClass()))
			gary[gidx] = test;
		    else
			/////////////////////////////////////////////////////////////////////////////////////////////////////
			throw new java.lang.IllegalStateException("bug:unimplemented-filter-failure @ "+gidx+" in "+glen); //
			/////////////////////////////////////////////////////////////////////////////////////////////////////
			/////////////////////////////////////////////////////////////////////////////////////////////////////
		}
		else 
		    gary[gidx] = test;
	    }
	    return gary;
	}
    }

    public int size(){
 	if (null == this.frame_parent)
 	    return super.size();
	else if (null != this.frame_distinct)
	    return (this.frame_distinct.size());
	else
	    return super.size();/*(special case for 'size' needed by frame-init)
				 */
    }
    public int sizeNof(){
	return super._size();
    }
    public boolean isEmpty(){
	return (1 > this.size());
    }
    public boolean isEmptyNof(){
	return (1 > this.sizeNof());
    }
    public boolean isNotEmpty(){
	return (0 < this.size());
    }
    public boolean isNotEmptyNof(){
	return (0 < this.sizeNof());
    }

    public int indexOf(Object key) {
	if (null == this.frame_parent)
	    return super.indexOf(key);
	else if (null != this.frame_distinct){
	    int xfc_idx = super.indexOf(key);
	    int xfp_idx = this.frame_parent.indexOf(key);
	    if (0 > xfc_idx){
		if (0 > xfp_idx)
		    return -1;
		else 
		    return this.frame_distinct.gx4px(xfp_idx);
	    }
	    else if (0 > xfp_idx)
		return this.frame_distinct.gx4cx(xfc_idx);
	    else {
		int xfc_gidx = this.frame_distinct.gx4cx(xfc_idx);
		int xfp_gidx = this.frame_distinct.gx4px(xfp_idx);
		if (0 > xfc_gidx || 0 > xfp_gidx){
		    if (0 > xfc_gidx && 0 > xfp_gidx)
			throw new java.lang.IllegalStateException("bug:xfc-and-xfp");
		    else if (0 > xfc_gidx)
			throw new java.lang.IllegalStateException("bug:xfc");
		    else 
			throw new java.lang.IllegalStateException("bug:xfp");
		}
		else if (xfc_gidx <= xfp_gidx)
		    return xfc_gidx;
		else
		    return xfp_gidx;
	    }
	}
	else
	{/*
	  * (bootstrap for link target discovery)
	  */
	    int re = this.frame_parent.indexOf(key);
	    if (-1 < re)
		return re;
	    else {
		re = super.indexOf(key);
		if (-1 < re)
		    return (this.frame_parent.size()+re);
		else 
		    return -1;
	    }
	}
    }
    public int indexOfNof(Object key) {
	return super._indexOf(key);
    }
    public int indexOf(Object key, int from) {
	if (null == this.frame_parent)
	    return super.indexOf(key,from);
	else if (null != this.frame_distinct){
	    if (0 > from)
		from = 0;
	    else if (from >= this.frame_distinct.size())
		return -1;
	    //
	    int xfc_from = this.frame_distinct.cx4gxf(from,true);
	    int xfp_from = this.frame_distinct.px4gxf(from,true);
	    int xfc_idx = super.indexOf(key,xfc_from);
	    int xfp_idx = this.frame_parent.indexOf(key,xfp_from);
	    if (0 > xfc_idx){
		if (0 > xfp_idx)
		    return -1;
		else 
		    return this.frame_distinct.gx4px(xfp_idx);
	    }
	    else if (0 > xfp_idx)
		return this.frame_distinct.gx4cx(xfc_idx);
	    else {
		int xfc_gidx = this.frame_distinct.gx4cx(xfc_idx);
		int xfp_gidx = this.frame_distinct.gx4px(xfp_idx);
		if (0 > xfc_gidx || 0 > xfp_gidx){
		    if (0 > xfc_gidx && 0 > xfp_gidx)
			throw new java.lang.IllegalStateException("bug:xfc-and-xfp");
		    else if (0 > xfc_gidx)
			throw new java.lang.IllegalStateException("bug:xfc");
		    else 
			throw new java.lang.IllegalStateException("bug:xfp");
		}
		else if (xfc_gidx <= xfp_gidx)
		    return xfc_gidx;
		else
		    return xfp_gidx;
	    }
	}
	else
	{/*
	  * (bootstrap for link target discovery)
	  */
	    int re = this.frame_parent.indexOf(key,from);
	    if (-1 < re)
		return re;
	    else {
		re = super.indexOf(key,from);
		if (-1 < re)
		    return re+this.frame_parent.size();
		else 
		    return -1;
	    }
	}
    }
    public int indexOfNof(Object key, int from) {
	return super._indexOf(key,from);
    }    
    public int lastIndexOf(Object key) {
 	if (null == this.frame_parent)
 	    return super.lastIndexOf(key);
 	else if (null != this.frame_distinct){
	    int xfp_idx = this.frame_parent.lastIndexOf(key);
	    int xfc_idx = super.lastIndexOf(key);
	    int xgp = this.frame_distinct.gx4px(xfp_idx);
	    int xgc = this.frame_distinct.gx4cx(xfc_idx);
	    return Math.max(xgc,xgp);
	}
	//
	throw new java.lang.IllegalStateException("frame-stale");
    }
    public int lastIndexOf(Object key, int from) {
 	if (null == this.frame_parent)
 	    return super.lastIndexOf(key,from);
 	else if (null != this.frame_distinct){
	    int xfp_from = this.frame_distinct.px4gxf(from,false);
	    int xfc_from = this.frame_distinct.cx4gxf(from,false);
	    int xfp_idx = this.frame_parent.lastIndexOf(key,xfp_from);
	    int xfc_idx = super.lastIndexOf(key,xfc_from);
	    int xgp = this.frame_distinct.gx4px(xfp_idx);
	    int xgc = this.frame_distinct.gx4cx(xfc_idx);
	    return Math.max(xgc,xgp);
 	}
	//
	throw new java.lang.IllegalStateException("frame-stale");
    }
    public int lastIndexOfNof(Object key, int from) {
	return super._lastIndexOf(key,from);
    }
    public int indexOfValue(Object val) {
 	if (null == this.frame_parent)
 	    return super.indexOfValue(val);
 	else if (null != this.frame_distinct){
	    int xfp_idx = this.frame_parent.indexOfValue(val);
	    int xfc_idx = super.indexOfValue(val);
	    int xgp = this.frame_distinct.gx4px(xfp_idx);
	    int xgc = this.frame_distinct.gx4cx(xfc_idx);
	    /*(-1 problem)
	     */
	    if (0 > xgc)
		return xgp;
	    else if (0 > xgp)
		return xgc;
	    else
		return Math.min(xgc,xgp);
	}
	else
	{/*
	  * (bootstrap for link target discovery)
	  */
	    int re = this.frame_parent.indexOfValue(val);
	    if (-1 < re)
		return re;
	    else {
		re = super.indexOfValue(val);
		if (-1 < re)
		    return re+this.frame_parent.size();
		else 
		    return -1;
	    }
	}
    }
    public int indexOfValueClass(Class vc) {
 	if (null == this.frame_parent)
 	    return super.indexOfValueClass(vc);
 	else if (null != this.frame_distinct){
	    int xfp_idx = this.frame_parent.indexOfValueClass(vc);
	    int xfc_idx = super.indexOfValueClass(vc);
	    int xgp = this.frame_distinct.gx4px(xfp_idx);
	    int xgc = this.frame_distinct.gx4cx(xfc_idx);
	    /*(-1 problem)
	     */
	    if (0 > xgc)
		return xgp;
	    else if (0 > xgp)
		return xgc;
	    else
		return Math.min(xgc,xgp);
 	}
	//
	throw new java.lang.IllegalStateException("frame-stale");
    }
    public int indexOfValueClassNof(Class vc) {
	return super._indexOfValueClass(vc,0);
    }
    public int indexOfValue(Object val, int from) {
 	if (null == this.frame_parent)
 	    return super.indexOfValue(val,from);
 	else if (null != this.frame_distinct){
	    int xfp_from = this.frame_distinct.px4gxf(from,true);
	    int xfc_from = this.frame_distinct.cx4gxf(from,true);
	    int xfp_idx = this.frame_parent.indexOfValue(val,xfp_from);
	    int xfc_idx = super.indexOfValue(val,xfc_from);
	    int xgp = this.frame_distinct.gx4px(xfp_idx);
	    int xgc = this.frame_distinct.gx4cx(xfc_idx);
	    /*(-1 problem)
	     */
	    if (0 > xgc)
		return xgp;
	    else if (0 > xgp)
		return xgc;
	    else
		return Math.min(xgc,xgp);
	}
	else
	{/*
	  * (bootstrap for link target discovery)
	  */
	    int re = this.frame_parent.indexOfValue(val,from);
	    if (-1 < re)
		return re;
	    else {
		int sz = this.frame_parent.size();
		int test = (from-sz);
		if (-1 < test)
		    re = super.indexOfValue(val,test);
		else
		    re = super.indexOfValue(val,from);
		if (-1 < re)
		    return re+this.frame_parent.size();
		else
		    return -1;
	    }
	}
    }
    public int indexOfValueNof(Object key, int from) {
	return super._indexOfValue(key,from);
    }
    public int lastIndexOfValue(Object val) {
 	if (null == this.frame_parent)
 	    return super.lastIndexOfValue(val);
 	else if (null != this.frame_distinct){
	    int xfp_idx = this.frame_parent.indexOfValue(val);
	    int xfc_idx = super.indexOfValue(val);
	    int xgp = this.frame_distinct.gx4px(xfp_idx);
	    int xgc = this.frame_distinct.gx4cx(xfc_idx);
	    return Math.max(xgc,xgp);
 	}
	//
	throw new java.lang.IllegalStateException("frame-stale");
    }
    public int lastIndexOfValue(Object val, int from) {
 	if (null == this.frame_parent)
 	    return super.lastIndexOfValue(val,from);
	else if (null != this.frame_distinct){
	    int xfp_from = this.frame_distinct.px4gx(from);
	    int xfc_from = this.frame_distinct.cx4gx(from);
	    int xfp_idx = this.frame_parent.lastIndexOfValue(val,xfp_from);
	    int xfc_idx = super.lastIndexOfValue(val,xfc_from);
	    int xgp = this.frame_distinct.gx4px(xfp_idx);
	    int xgc = this.frame_distinct.gx4cx(xfc_idx);
	    return Math.max(xgc,xgp);
 	}
	//
	throw new java.lang.IllegalStateException("frame-stale");
    }
    public int lastIndexOfValueClass(Class clas) {
 	if (null == this.frame_parent)
 	    return super.lastIndexOfValueClass(clas);
	else if (null != this.frame_distinct){
	    int xfp_idx = this.frame_parent.indexOfValue(clas);
	    int xfc_idx = super.indexOfValue(clas);
	    int xgp = this.frame_distinct.gx4px(xfp_idx);
	    int xgc = this.frame_distinct.gx4cx(xfc_idx);
	    return Math.max(xgc,xgp);
 	}
	//
	throw new java.lang.IllegalStateException("frame-stale");
    }
    public int lastIndexOfValueClassNof(Class key) {
	return super._lastIndexOfValueClass(key,(this.sizeNof()-1));
    }
    public Object value(int idx){
	if (null == this.frame_parent)
	    return super.value(idx);
	else if (null != this.frame_distinct){
	    int xfc_idx = this.frame_distinct.cx4gx(idx);
	    int xfp_idx = this.frame_distinct.px4gx(idx);
	    if (0 > xfc_idx){
		if (0 > xfp_idx)
		    return null;
		else 
		    return this.frame_parent.value(xfp_idx);
	    }
	    else if (0 > xfp_idx)
		return super.value(xfc_idx);
	    else {
		int xfc_gidx = this.frame_distinct.gx4cx(xfc_idx);
		int xfp_gidx = this.frame_distinct.gx4px(xfp_idx);
		if (0 > xfc_gidx || 0 > xfp_gidx){
		    if (0 > xfc_gidx && 0 > xfp_gidx)
			throw new java.lang.IllegalStateException("bug:xfc-and-xfp");
		    else if (0 > xfc_gidx)
			throw new java.lang.IllegalStateException("bug:xfc");
		    else 
			throw new java.lang.IllegalStateException("bug:xfp");
		}
		else if (xfc_gidx <= xfp_gidx)
		    return super.value(xfc_idx);
		else
		    return this.frame_parent.value(xfp_idx);
	    }
	}
	else
	{/*
	  * (bootstrap for link target discovery)
	  */
	    int psz = this.frame_parent.size();
	    int lx = (idx-psz);
	    if (-1 < lx)
		return super.value(lx);
	    else 
		return this.frame_parent.value(idx);
	}
    }
    public Object valueNof(int idx){
	return super._value(idx);
    }
    public Object get(Object key){
	if (null == this.frame_parent)
	    return super.get(key);
	else if (null != this.frame_distinct){
	    int gidx = this.indexOf(key);
	    if (0 > gidx)
		return null;
	    else
		return this.value(gidx);
	}
	else
	{/*
	  * (bootstrap for link target discovery)
	  */
	    Object re = this.frame_parent.get(key);
	    if (null != re)
		return re;
	    else 
		return super.get(key);
	}
    }
    public Object getNof(Object key){
	return super._get(key);
    }
    public Object key(int idx){
	if (null == this.frame_parent)
	    return super.key(idx);
	else if (null != this.frame_distinct){
	    int xfc_idx = this.frame_distinct.cx4gx(idx);
	    int xfp_idx = this.frame_distinct.px4gx(idx);
	    if (0 > xfc_idx){
		if (0 > xfp_idx)
		    return null;
		else 
		    return this.frame_parent.key(xfp_idx);
	    }
	    else if (0 > xfp_idx)
		return super.key(xfc_idx);
	    else {
		int xfc_gidx = this.frame_distinct.gx4cx(xfc_idx);
		int xfp_gidx = this.frame_distinct.gx4px(xfp_idx);
		if (0 > xfc_gidx || 0 > xfp_gidx){
		    if (0 > xfc_gidx && 0 > xfp_gidx)
			throw new java.lang.IllegalStateException("bug:xfc-and-xfp");
		    else if (0 > xfc_gidx)
			throw new java.lang.IllegalStateException("bug:xfc");
		    else 
			throw new java.lang.IllegalStateException("bug:xfp");
		}
		else if (xfc_gidx <= xfp_gidx)
		    return super.key(xfc_idx);
		else
		    return this.frame_parent.key(xfp_idx);
	    }
	}
	else
	{/*
	  * (bootstrap for link target discovery)
	  */
	    int psz = this.frame_parent.size();
	    int lx = (idx-psz);
	    if (-1 < lx)
		return super.key(lx);
	    else 
		return this.frame_parent.key(idx);
	}
    }
    public Object keyNof(int idx){
	return super._key(idx);
    }
    public Object put( Object key, Object value){
	this.stale();
	//
	return super.put(key,value);
    }
    public Object value(int idx, Object value){
	if (null == this.frame_parent)
	    return super.value(idx,value);
	else if (null != this.frame_distinct){
	    this.stale();
	    int xfc_idx = this.frame_distinct.cx4gx(idx);
	    int xfp_idx = this.frame_distinct.px4gx(idx);
	    if (0 > xfc_idx){
		if (0 > xfp_idx)
		    return null;
		else 
		    return this.frame_parent.value(xfp_idx,value);
	    }
	    else if (0 > xfp_idx)
		return super.value(xfc_idx);
	    else {
		int xfc_gidx = this.frame_distinct.gx4cx(xfc_idx);
		int xfp_gidx = this.frame_distinct.gx4px(xfp_idx);
		if (0 > xfc_gidx || 0 > xfp_gidx){
		    if (0 > xfc_gidx && 0 > xfp_gidx)
			throw new java.lang.IllegalStateException("bug:xfc-and-xfp");
		    else if (0 > xfc_gidx)
			throw new java.lang.IllegalStateException("bug:xfc");
		    else 
			throw new java.lang.IllegalStateException("bug:xfp");
		}
		else if (xfc_gidx <= xfp_gidx)
		    return super.value(xfc_idx,value);
		else
		    return this.frame_parent.value(xfp_idx,value);
	    }
	}
	else
	    throw new java.lang.IllegalStateException("frame-stale @ "+this.toString()+" @ "+idx);
    }
    public Object valueNof(int idx, Object value){
	this.stale();
	return super._value(idx,value);
    }
    public Object remove(int idx){
	if (null == this.frame_parent)
	    return super.remove(idx);
	else if (null != this.frame_distinct){
	    int xfc_idx = this.frame_distinct.cx4gx(idx);
	    int xfp_idx = this.frame_distinct.px4gx(idx);
	    if (0 > xfc_idx){
		if (0 > xfp_idx)
		    return null;
		else {
		    this.stale();
		    return this.frame_parent.remove(xfp_idx);
		}
	    }
	    else if (0 > xfp_idx){
		this.stale();
		return super.remove(xfc_idx);
	    }
	    else {
		int xfc_gidx = this.frame_distinct.gx4cx(xfc_idx);
		int xfp_gidx = this.frame_distinct.gx4px(xfp_idx);
		if (0 > xfc_gidx || 0 > xfp_gidx){
		    if (0 > xfc_gidx && 0 > xfp_gidx)
			throw new java.lang.IllegalStateException("bug:xfc-and-xfp");
		    else if (0 > xfc_gidx)
			throw new java.lang.IllegalStateException("bug:xfc");
		    else 
			throw new java.lang.IllegalStateException("bug:xfp");
		}
		else if (xfc_gidx <= xfp_gidx){
		    this.stale();
		    return super.remove(xfc_idx);
		}
		else {
		    this.stale();
		    return this.frame_parent.remove(xfp_idx);
		}
	    }
	}
	else
	    throw new java.lang.IllegalStateException("frame-stale @ "+this.toString()+" @ "+idx);
    }
    public java.util.Enumeration keys(){
	if (null == this.frame_parent)
	    return super.keys();
	else
	    return new Enumerator1(this.keyary());
    }
    public java.util.Enumeration keysNof(){
	return super._keys();
    }
    public java.util.Enumeration elements(){
	if (null == this.frame_parent)
	    return super.elements();
	else
	    return new Enumerator1(this.valary());
    }
    public java.util.Enumeration elementsNof(){
	return super._elements();
    }
    public Object[] keyary(){
	if (null != this.frame_parent)
	    return this.frary(CLA_NIL,true,false);
	else
	    return super.keyary();
    }
    public Object[] keyaryNof(){
	return super._keyary();
    }
    public Object[] keyary(Class comptype){
	if (null != this.frame_parent)
	    return this.frary(comptype,true,false);
	else
	    return super.keyary(comptype);
    }
    public Object[] keyaryNof(Class comptype){
	return super._keyary(comptype);
    }
    public Object[] valary(){
	if (null != this.frame_parent)
	    return this.frary(CLA_NIL,false,false);
	else
	    return super.valary();
    }
    public Object[] valaryNof(){
	return super._valary();
    }
    public Object[] valary(Class comptype){
	if (null != this.frame_parent)
	    return this.frary(comptype,false,false);
	else
	    return super.valary(comptype);
    }
    public Object[] valaryNof(Class comptype){
	return super._valary(comptype);
    }
    public Object[] valary_filter(Class comptype){
	if (null != this.frame_parent)
	    return this.frary(comptype,false,true);
	else
	    return super.valary_filter(comptype);
    }
    public Object[] valary_filterNof(Class comptype){
	return super._valary_filter(comptype);
    }
    public boolean containsKey (Object value){
	if (null != this.frame_parent && this.frame_parent.containsKey(value))
	    return true;
	else
	    return super.containsKey(value);
    }
    public boolean containsKeyNof (Object value){
	return super._containsKey(value);
    }
    public boolean containsValue (Object value){
	if (null != this.frame_parent && this.frame_parent.containsValue(value))
	    return true;
	else
	    return super.containsValue(value);
    }
    public boolean containsValueNof (Object value){
	return super._containsValue(value);
    }
    public boolean contains (Object value){
	if (null != this.frame_parent && this.frame_parent.contains(value))
	    return true;
	else
	    return super.contains(value);
    }
    public boolean containsNof (Object value){
	return super._contains(value);
    }
    public int insert( int idx, Object key, Object val){
	if (null == this.frame_parent)
	    return super.insert(idx,key,val);
	else if (null != this.frame_distinct){
	    this.stale();
	    int xfc_idx = this.frame_distinct.cx4gx(idx);
	    if (0 > xfc_idx)
		return super.insert(0,key,val);
	    else
		return super.insert(xfc_idx,key,val);
	}
	//
	throw new java.lang.IllegalStateException("frame-stale @ "+this.toString()+" @ "+idx);
    }
    public int insertNof( int idx, Object key, Object val){
	return super._insert(idx,key,val);
    }
    public obfmap cloneObfmap(boolean inherit){
	obfmap clone = (obfmap)super.cloneObjmap();
	if (null != clone.frame_distinct)
	    clone.frame_distinct = this.frame_distinct.cloneFramelist();
	if (inherit)
	    clone.frameParentReset(this);
	return clone;
    }

}
