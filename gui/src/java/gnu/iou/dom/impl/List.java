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
package gnu.iou.dom.impl;

/**
 * <p> Array list for iteration. </p>
 * 
 * @author jdp
 */
public class List 
    extends java.lang.Object 
    implements gnu.iou.dom.List
{

    /**
     * <p> List iterator. </p>
     * 
     * @author jdp
     */
    public static class Iterator 
	extends List 
	implements gnu.iou.dom.List.Iterator
    {

	protected int pointer = -1, terminal, for_start, for_many;

	public Iterator(java.lang.Object[] set){
	    this(set,-1,-1);
	}
	public Iterator(java.lang.Object[] set, int for_fromx, int for_many){
	    super(set);
	    this.terminal = super.getLength();

	    if (0 > for_fromx)
		this.for_start = 0;
	    else
		this.for_start = for_fromx;

	    if (0 > for_many)
		this.for_many = this.terminal;
	    else
		this.for_many = for_many;

	    int trm  = (this.for_start+this.for_many);
	    if (trm <= this.terminal)
		this.terminal = trm;

	    this.pointer = this.for_start;
	}

	public void reinit(){
	    super.reinit();
	    this.pointer = -1;
	}

	public final int pointer(){
	    return this.pointer;
	}
	public final int terminal(){
	    return this.terminal;
	}
	public final java.lang.Object next(){

	    return super.item(this.pointer++);
	}
	public final java.lang.Object current(){

	    return super.item(this.pointer);
	}
	public final java.lang.Object previous(){

	    return super.item(this.pointer--);
	}
	public boolean more(){
	    return (this.pointer < this.terminal);
	}
	public boolean head(){
	    return this.more();
	}
	public boolean tail(){
	    return this.last();
	}
	public boolean first(){
	    return (1 > this.pointer);
	}
	public boolean last(){
	    return (this.pointer + 1) == this.terminal;
	}
	/**
	 * @return Init value
	 */
	public int for_start(){
	    return this.for_start;
	}
	/**
	 * @return Init value
	 */
	public int for_many(){
	    return this.for_many;
	}

    }

    protected java.lang.Object[] set = null;

    public List(){
	super();
    }
    /**
     * @param node Add to the internal list
     */
    public List(java.lang.Object node){
	super();
	this.add(node);
    }
    /**
     * @param set Use this array as the internal list
     */
    public List(java.lang.Object[] set){
	super();
	this.set = set;
    }

    public void reinit(){
	//(nop)
    }
    public gnu.iou.dom.List.Iterator iterator(){
	return new List.Iterator(this.set);
    }
    public final void add( java.lang.Object node){
	if (null == node)
	    return;
	else {
	    java.lang.Object[] set = this.set;
	    if (null == set)
		this.set = new java.lang.Object[]{node};
	    else {
		int idx = set.length;
		java.lang.Object[] copier = 
		    new java.lang.Object[idx+1];
		java.lang.System.arraycopy(set,0,copier,0,idx);
		copier[idx] = node;
		this.set = copier;
	    }
	}
    }
    public final void remove( java.lang.Object node){
	if (null == node)
	    return;
	else {
	    java.lang.Object[] set = this.set;
	    if (null == set)
		return;
	    else {
		for (int cc = 0, len = set.length; cc < len; cc++){
		    if (node.equals(set[cc])){
			int nlen = (len-1);
			if (0 == nlen){
			    this.set = null;
			    return ;
			}
			else if (cc == nlen){
			    java.lang.Object[] nset = 
				new java.lang.Object[nlen];
			    java.lang.System.arraycopy(set,0,nset,0,nlen);
			    this.set = nset;
			    return;
			}
			else {
			    java.lang.Object[] nset = 
				new java.lang.Object[nlen];
			    java.lang.System.arraycopy(set,0,nset,0,cc);
			    java.lang.System.arraycopy(set,(cc+1),nset,cc,(nlen-cc));
			    this.set = nset;
			    return;
			}
		    }
		}
	    }
	}
    }

    public final java.lang.Object item(int idx){
	java.lang.Object[] set = this.set;
	if ( null != set && -1 < idx && idx < set.length)
	    return set[idx];
	else
	    return null;
    }
    public final int getLength(){
	java.lang.Object[] set = this.set;
	if ( null == set)
	    return 0;
	else
	    return set.length;
    }
    public void destroy(){
	this.set = null;
    }

}
