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
 * A linked- list queue (or stack).
 *
 * @author John Pritchard (john@syntelos.org)
 */
public class queue extends Object implements Enumeration, Cloneable {

    /**
     * List node can be subclassed, eg, for a soft queue.  Such a
     * `queue' subclass needs to set the queue type in its
     * constructors using the equivalent of
     * <pre>
     * q_t = new Enqueued().getClass();
     * </pre>
     * 
     * <p> The `Enqueued' methods `get', `reinit', `cloneEnqueued',
     * `copy' and `contains' need to be defined in an `Enqueued'
     * subclass that employs an object intermediary like a
     * SoftReference.  Refer to the java sourcecode for this class for
     * more information.
     * 
     * @author John Pritchard */
    public static class Enqueued extends Object implements Cloneable {

	protected Object o = null;

	protected Enqueued n = null;

	public Enqueued(){
	    super();
	}

	public Object get(){
	    return o;
	}

	public final Object get(int idx){
	    if (0 > idx)
		return null;
	    else if (0 == idx)
		return this.get();
	    else if (null != this.n)
		return this.n.get(idx-1);
	    else
		return null;
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
	    if ( o == obj)
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
	public Enqueued cloneEnqueued(){
	    try {
		Enqueued enq = (Enqueued)super.clone();

		if ( this.o instanceof copy)
		    enq.o = ((copy)this.o).copy();

		if ( null != n)
		    enq.n = this.n.cloneEnqueued();

		return enq;
	    }
	    catch ( CloneNotSupportedException cnx){
		return null;
	    }
	}
	/**
	 * Clone, dropping without remainer of list
	 */
	public Enqueued copy(){
	    try {
		Enqueued enq = (Enqueued)super.clone();

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

    public final static Class DefaultEnqueuedClass = Enqueued.class;

    ///////queue////

    private volatile Enqueued list = null;

    /**
     * Queue type set by <tt>`this()'</tt> constructor and replaceable
     * by subclasses' constructors.  */
    protected Class q_t ;

    private lck pushpoplock = new lck();

    /**
     * By default it's a queue (FIFO), but with this on, it's a stack
     * (LIFO).  This is used exclusively to determined this behavior
     * in the "push" method.  */
    private boolean stack = false;

    /**
     * Recycle bin
     */
    private Enqueued free_q = null;

    /**
     * Create a new FIFO queue (not LIFO stack).
     */
    public queue(){
	super();

	q_t = DefaultEnqueuedClass;
    }

    /**
     * @param use_stack If true, this is a stack (LIFO), otherwise
     * it's a queue (FIFO).  */
    public queue( boolean use_stack){
	this();
	stack = use_stack;
    }

    public lck locker(){ return pushpoplock;}

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
	if ( null == list)
	    return false;
	else
	    return list.contains(obj);
    }

    /*
     * Synchronizing push and pop prevents popping an object twice
     * concurrently, and tends to improve the execution patterns of
     * user code.  */

    /*
     * User interface for stack or queue "push", depending on constructor.
     */
//      public void push( Object o){
//  	push(this,o,stack);
//      }

    /**
     * User interface for stack or queue "push", depending on constructor.
     * 
     * @param usr The queue user is the object with a field for the
     * queue.  For <tt>`lck.LckDesc'</tt>.  For example, locks used by
     * the <tt>`Context'</tt> or its fields use the <tt>`Context'</tt>
     * (instance) object for their <tt>`usr'</tt>.
     *
     * @param o Object for queue/stack
     * 
     * @see lck#LckDesc */
    public void push( Object usr, Object o){
	push(usr,o,stack);
    }
    /**
     * User interface for stack or queue, depending on constructor,
     * with override option for FIFO or LIFO behavior in stack or
     * queue.
     *
     * <p> This interface should be used only when needing to change
     * the behavior of the stack or queue, for exampe using an
     * enumeration or queue with a "pushback" of the last popped
     * element.  Otherwise use the regular <code>`push(Object)'</code>
     * method.
     *
     * @param usr The queue user is the object with a field for the
     * queue.  For <tt>`lck.LckDesc'</tt>.  For example, locks used by
     * the <tt>`Context'</tt> or its fields use the <tt>`Context'</tt>
     * (instance) object for their <tt>`usr'</tt>.
     *
     * @param o Object to push
     *
     * @param use_stack_fifo If true uses stack behavior (FIFO),
     * otherwise false uses queue behavior (LIFO).  
     *
     * @see lck#LckDesc */
    public void push( Object usr, Object o, boolean use_stack_fifo){

	if ( null == o) return;

	try {
	    pushpoplock.serialize(usr);

	    if ( null == list){

		list = free_pop(o);

		return;
	    }
	    else {

		if ((stack && use_stack_fifo)||((!stack) && use_stack_fifo)){

		    // FIFO

		    Enqueued q = list;

		    list = free_pop(o);

		    list.n = q;

		    return;
		}
		else {

		    // LIFO

		    for ( Enqueued q = list; q != null; q = q.n){

			if ( null == q.n){

			    q.n = free_pop(o);
			
			    return;
			}
		    }
		}
	    }
	}
	finally {
	    pushpoplock.unlock(usr);
	}
    }

    /**
     * Look at the next "pop" element without popping it (removing it)
     * from the queue/stack.
     *
     * @param usr The queue user is the object with a field for the
     * queue.  For <tt>`lck.LckDesc'</tt>.  For example, locks used by
     * the <tt>`Context'</tt> or its fields use the <tt>`Context'</tt>
     * (instance) object for their <tt>`usr'</tt>.
     * 
     * @see lck#LckDesc
     */
    public Object peek( Object usr){ 

	if ( null != list){

	    while ( null == list.get()) // support for softq
		pop(usr);

	    return list.get();
	}
	else
	    return null;
    }

    /**
     * Look at the next "pop" element without popping it (removing it)
     * from the queue/stack.
     *
     * @param usr The queue user is the object with a field for the
     * queue.  For <tt>`lck.LckDesc'</tt>.  For example, locks used by
     * the <tt>`Context'</tt> or its fields use the <tt>`Context'</tt>
     * (instance) object for their <tt>`usr'</tt>.
     * 
     * @param idx Index from top to bottom counting from zero
     * 
     * @see lck#LckDesc
     */
    public Object peek( Object usr, int idx){ 

	if ( null != list){

	    while ( null == list.get()) // support for softq
		pop(usr);

	    return list.get(idx);
	}
	else
	    return null;
    }

    /**
     * Return and remove the next element from the queue or stack.
     *
     * @param usr The queue user is the object with a field for the
     * queue.  For <tt>`lck.LckDesc'</tt>.  For example, locks used by
     * the <tt>`Context'</tt> or its fields use the <tt>`Context'</tt>
     * (instance) object for their <tt>`usr'</tt>.
     * 
     * @see lck#LckDesc
     */
    public Object pop( Object usr){

	if ( null == list)
	    return null;

	try {
	    pushpoplock.serialize(usr);

	    Enqueued q;  Object o;

	    while (true){

		q = list;

		if ( null == q)
	    
		    return null;

		else {
		    list = q.n;

		    o = q.get();    // support for softq

		    free_push(q);

		    if ( null != o)
			
			return o;
		}
	    }
	}
	finally {
	    pushpoplock.unlock(usr);
	}
    }

    public boolean isEmpty( Object usr){ return (null == peek(usr));}

    public boolean isNotEmpty( Object usr){ return (null != peek(usr));}

    /**
     * @param usr The queue user is the object with a field for the
     * queue.  For <tt>`lck.LckDesc'</tt>.  For example, locks used by
     * the <tt>`Context'</tt> or its fields use the <tt>`Context'</tt>
     * (instance) object for their <tt>`usr'</tt>.
     * 
     * @see lck#LckDesc */
    public int size( Object usr){

	if ( null == peek(usr))

	    return 0;

	else {

	    int many = 0;

	    for ( Enqueued q = list; null != q; q = q.n){

		if ( null != q.get())
		    many++;
	    }

	    return many;
	}
    }

    /**
     * @param usr The queue user is the object with a field for the
     * queue.  For <tt>`lck.LckDesc'</tt>.  For example, locks used by
     * the <tt>`Context'</tt> or its fields use the <tt>`Context'</tt>
     * (instance) object for their <tt>`usr'</tt>.
     * 
     * @param obj Target to remove
     * 
     * @return Target when found and removed, otherwise null.
     * 
     * @see lck#LckDesc */
    public Object remove ( Object usr, Object obj){

	if ( null == peek(usr))

	    return null;

	else {

	    for ( Enqueued q = list, p = null; null != q; p = q, q = q.n){

		if ( obj == q.get()){

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
     * Size greater than one.
     *
     * <p> This can be used for maintaining a default element at the
     * bottom of the stack without counting the whole stack.
     *
     * <p> If a peek reveals the default element, and the stack size is
     * not greater than one, then the stack position is at this bottom
     * default element which would not be popped.  
     *
     * <p> A <tt>`queue'</tt> subclass should redefine this
     * <tt>`Enqueued'</tt> optimized method for its requirements.  */
    public boolean size_gt_1(){

	return (null != list && null != list.n);

    }

    /**
     * Pop all elements into a string.  Concatenate all enqueued
     * elements into a string, each separated by a space (0x20).  */
    public String popall_String(Object usr){
	return popall_String(usr,' ');
    }

    public String toString(){

	chbuf strbuf = new chbuf();

	if (stack)
	    strbuf.append("stack");
	else
	    strbuf.append("queue");

	int many = 0;

	Object obj;

	for ( Enqueued q = list; null != q; q = q.n){

	    obj = q.get();

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

	else if ( ano instanceof queue)

	    return toString().equals(ano.toString());
	else
	    return false;
    }

    public queue cloneQueue(){
	try {
	    queue clone = (queue)super.clone();

	    clone.pushpoplock = new lck();

	    lck.LckDesc( clone, chbuf.cat("QUEUE #",Integer.toString(System.identityHashCode(clone))));

	    if ( null != list)
		clone.list = list.cloneEnqueued();

	    clone.free_q = null;

	    return clone;
	}
	catch ( CloneNotSupportedException cnx){
	    return null;
	}
    }

    /**
     * Clone without depth of queue -- with only the top element of
     * the queue/ stack.
     */
    public queue copy(){
	try {
	    queue clone = (queue)super.clone();

	    clone.pushpoplock = new lck();

	    lck.LckDesc( clone, chbuf.cat("QUEUE #",Integer.toString(System.identityHashCode(clone))));

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
    public String popall_String( Object usr, char sep){

	if (null == list) return null;

	chbuf strbuf = new chbuf();

	Object o ;

	while ( null != (o = pop(usr))){

	    strbuf.append(o);

	    if ( null != list)
		strbuf.append(sep);
	}
	return strbuf.toString();
    }


    private void free_push ( Enqueued eo){

	eo.free();

	if ( null == free_q)

	    free_q = eo;

	else {

	    eo.n = free_q;

	    free_q = eo;
	}
    }

    private Enqueued free_pop ( Object o){

	Enqueued f = free_q;

	if ( null == f){

	    try {
		f = (Enqueued)q_t.newInstance();
	    }
	    catch ( InstantiationException insx){insx.printStackTrace();}
	    catch ( IllegalAccessException ilacx){ilacx.printStackTrace();}

	    f.reinit(o); // null ptr exc, if q_t can't instantiate

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
	return isEmpty(this);
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
	if (isEmpty(this))
	    throw new NoSuchElementException();
	else 
	    return pop(this);
    }

    public Enumeration elements(){

	return this.cloneQueue();
    }

}
