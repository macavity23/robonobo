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

import java.util.Enumeration;
import java.util.NoSuchElementException;

/**
 * A fast, recycling linked- list queue
 *
 * @author John Pritchard (john@syntelos.org)
 */
public class fq extends Object implements Enumeration, Cloneable {

    /**
     * List node.
     * 
     * @author John Pritchard */
    public static class fqn extends Object implements Cloneable {

	protected Object o = null;

	protected fqn n = null;

	public fqn(){
	    super();
	}

	public Object get(){
	    return o;
	}

	public void free(){
	    o = null;
	    n = null;
	}

	public void clear(){

	    if ( null != n) n.clear();

	    o = null;
	    n = null;
	}

	public boolean contains( Object obj){
	    if ( obj.equals(this.o))
		return true;
	    else if ( null != n)
		return n.contains(obj);
	    else
		return false;
	}

	public void reinit( Object obj){
	    o = obj;
	    n = null;
	}
	public String toString(){
	    return super.toString()+"["+o+"]";
	}
	public fqn clonefqn(){
	    try {
		fqn enq = (fqn)super.clone();

		if ( this.o instanceof copy)
		    enq.o = ((copy)this.o).copy();

		if ( null != n)
		    enq.n = this.n.clonefqn();

		return enq;
	    }
	    catch ( CloneNotSupportedException cnx){
		return null;
	    }
	}
	/**
	 * Clone, dropping without remainer of list
	 */
	public fqn copy(){
	    try {
		fqn enq = (fqn)super.clone();

		if ( this.o instanceof copy)
		    enq.o = ((copy)this.o).copy();

		enq.n = null;

		return enq;
	    }
	    catch ( CloneNotSupportedException cnx){
		return null;
	    }
	}
    }

    ///////fq////

    private volatile fqn list = null;

    /**
     * Recycle bin
     */
    private fqn free_q = null;

    /**
     * Create a new FIFO queue 
     */
    public fq(){
	super();
    }


    public void clear(){ 

	if ( null != list){

	    list.clear();
	    
	    list = null;
	}

	if ( null != free_q){

	    free_q.clear();
	    
	    free_q = null;
	}
    }

    public boolean contains ( Object obj){
	if ( null == list || null == obj)
	    return false;
	else
	    return list.contains(obj);
    }

    /**
     * @param o Object to push (Push null produces pop null).
     */
    public synchronized void push( Object o){

	if ( null == list){

	    list = free_pop(o);

	    return;
	}
	else {
	    for ( fqn q = list; q != null; q = q.n){

		if ( null == q.n){

		    q.n = free_pop(o);
			
		    return;
		}
	    }
	}
    }

    /**
     * Look at the next "pop" element without popping it (removing it)
     */
    public Object peek(){ 
	if ( null != list)

	    return list.o;
	else
	    return null;
    }

    /**
     * Return and remove the next element from the queue 
     */
    public synchronized Object pop(){

	if ( null == list)
	    return null;
	else {
	    fqn q = this.list;  

	    Object ret = q.o;

	    this.list = q.n;

	    free_push(q);

	    return ret;
	}
    }

    public boolean isEmpty(){ return (null == peek());}

    public boolean isNotEmpty(){ return (null != peek());}

    /**
     */
    public int size(){

	if ( null == list)

	    return 0;

	else {

	    int many = 0;

	    for ( fqn q = list; null != q; q = q.n)
		many++;

	    return many;
	}
    }

    /**
     * @param obj Non null target to remove, identity by object "equals" method.
     * @returns Enqueued object removed, or null for none.
     */
    public Object remove ( Object obj){

	if ( null == list || null == obj)

	    return null;

	else {

	    for ( fqn q = list, p = null; null != q; p = q, q = q.n){

		if ( obj.equals(q.o)){

		    obj = q.o;

		    if ( null == p)

			list = q.n;
		    else 
			p.n = q.n;
		    
		    free_push(q);

		    return obj;
		}
	    }
	    return null;
	}
    }

    /**
     * @param obj Non null finder, identity by object "equals" method.
     * @returns Enqueued object, or null for none.
     */
    public Object find ( Object obj){

	if ( null == list || null == obj)

	    return null;

	else {

	    for ( fqn q = list, p = null; null != q; p = q, q = q.n){

		if ( obj.equals(q.o))

		    return q.o;
	    }
	    return null;
	}
    }

    /**
     * Size greater than one.
     */
    public boolean size_gt_1(){

	return (null != list && null != list.n);
    }

    /**
     * Pop all elements into a string.  Concatenate all enqueued
     * elements into a string, each separated by a space (0x20).  
     */
    public String popall_String(){
	return popall_String(' ');
    }

    public String toString(){

	chbuf strbuf = new chbuf();

	strbuf.append("fq");

	int many = 0;

	Object obj;

	for ( fqn q = list; null != q; q = q.n){

	    obj = q.o;

	    if ( null != obj){

		if ( 0 == many)
		    strbuf.append('[');
		else
		    strbuf.append(',');

		many += 1;

		strbuf.append(obj);
	    }
	}
	if ( 0 < many)
	    strbuf.append(']');

	return strbuf.toString();
    }

    public boolean equals ( Object ano){

	if ( ano == this)
	    return true;

	else if ( ano instanceof fq)

	    return toString().equals(ano.toString());
	else
	    return false;
    }

    public fq cloneQueue(){
	try {
	    fq clone = (fq)super.clone();


	    if ( null != list)
		clone.list = list.clonefqn();

	    clone.free_q = null;

	    return clone;
	}
	catch ( CloneNotSupportedException cnx){
	    return null;
	}
    }

    /**
     * Clone without depth of queue -- with only the top element of
     * the queue.
     */
    public fq copy(){
	try {
	    fq clone = (fq)super.clone();

	    if ( null != list)
		clone.list = list.copy();

	    clone.free_q = null;

	    return clone;
	}
	catch ( CloneNotSupportedException cnx){
	    return null;
	}
    }

    /**
     * Pop all elements into a string.  Concatenate all enqueued
     * elements into a string, each separated by the argument
     * character.  Uses the java Object 'toString' method.  */
    public String popall_String( char sep){

	if (null == list) return null;

	chbuf strbuf = new chbuf();

	Object o ;

	while ( null != (o = pop())){

	    strbuf.append(o);

	    if ( null != list)
		strbuf.append(sep);
	}
	return strbuf.toString();
    }


    private void free_push ( fqn eo){

	eo.free();

	if ( null == free_q)

	    free_q = eo;

	else {

	    eo.n = free_q;

	    free_q = eo;
	}
    }

    private fqn free_pop ( Object o){

	fqn f = free_q;

	if ( null == f){

	    f = new fqn();

	    f.reinit(o);

	    return f;
	}
	else {
	    free_q = f.n;

	    f.reinit(o);

	    return f;
	}
    }

    /**
     * Returns true if the enumeration contains more elements; false
     * if its empty.
     *
     * @see java.util.Enumeration
     */
    public boolean hasMoreElements(){
	return isEmpty();
    }

    /**
     * Returns the next element of the enumeration. Calls to this
     * method will enumerate successive elements.
     *
     * @exception NoSuchElementException If no more elements exist.
     *
     * @see java.util.Enumeration
     */
    public Object nextElement(){
	if (isEmpty())
	    throw new NoSuchElementException();
	else 
	    return pop();
    }

    public Enumeration elements(){

	return this.cloneQueue();
    }

}
