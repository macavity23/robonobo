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

import java.util.Hashtable;

/**
 * Particular type of locker ("mutex") serializes entry into area(s) 
 * "protected" by a <code>`lck.serialize()'</code> "gate" or
 * "gateway".
 *
 * <p> The "serialize" function allows one thread past at a time.
 * Each thread past "serialize" <b>MUST</b> call "unlock" once it's
 * done in the locked area, in order to exit the locked area and to
 * allow the next thread in.
 *
 * <p> If one lock object has its "serialize" gateway used in multiple
 * places, then all of those areas will be serialized -- one thread
 * enters their collective whole area at a time.  
 *
 * <p> A thread can reenter the serialized area.
 *
 * <h3>Usage</h3>
 *
 * <pre>
 *  new&nbsp;lck();<font color="#af0000">//(reentrant)//</font>
 *  new&nbsp;lck(true);<font color="#af0000">//(reentrant)//</font>
 *  new&nbsp;lck(false);<font color="#af0000">//(not reentrant)//</font>
 *
 * </pre> 
 *
 * <p>For example.
 * <p>
 * <pre>
 *  lck&nbsp;locker&nbsp;;
 *  try&nbsp;{
 *  &nbsp;&nbsp;&nbsp;&nbsp;locker.serialize(this);
 *
 *  &nbsp;&nbsp;&nbsp;&nbsp;<font color="#af0000">//(protected&nbsp;region)//</font>
 * 
 *  }
 *  finally&nbsp;{
 *  &nbsp;&nbsp;&nbsp;&nbsp;locker.unlock(this);
 *  }
 *
 * </pre> 
 *
 * @author John Pritchard (john@syntelos.org)
 */
public class lck implements Cloneable {

    public static boolean debug = false;

    /**
     * String lock descriptors set from user code.
     */
    private final static Hashtable lck_descriptors = new Hashtable(233);

    /**
     * Use the `lck' user object to setup a user lock descriptor string. 
     *
     * <p> This does not keep a reference to the user object,
     * preventing it from being garbage collected (and finalized).
     * 
     * <p> Call "LckDescRm" from the user- object's "finalize" method.
     * 
     * @see #LckDescRm
     */
    public final static void LckDesc ( Object user, String desc){

	if ( null != user && null != desc){

	    Integer ihc = new Integer(System.identityHashCode(user));

	    lck_descriptors.put( ihc, desc);
	}
	else
	    throw new IllegalArgumentException("Null argument to `LckDesc'.");
    }

    /**
     * Use the `lck' user- object  to lookup a user lock descriptor string. 
     */
    public final static String LckDesc ( Object user, lck lock){

	if ( null == user){

	    if ( null == lock)

		throw new IllegalArgumentException("Null argument to `LckDesc'.");
	    else
		user = lock;
	}

	Integer ihc = new Integer(System.identityHashCode(user));

	String desc = (String)lck_descriptors.get(ihc);

	if ( null == desc){

	    return chbuf.cat(user.getClass().getName(),"#",ihc.toString());
	}
	else
	    return desc;
    }

    /**
     * Use the `lck' user- object to delete a user lock descriptor
     * string from cache.  (Can call this from the user's "finalize".)  
     */
    public final static String LckDescRm ( Object user){

	if ( null == user)

	    throw new IllegalArgumentException("Null argument to `LckDesc'.");

	else {

	    Integer ihc = new Integer(System.identityHashCode(user));

	    return (String)lck_descriptors.remove(ihc);
	}
    }



    private volatile int entered = 0;

    private volatile int waiters = 0;

    private volatile Thread enteredT = null;

    private boolean reenterable = true; 

    /**
     * New locker constructor
     */
    public lck(){}

    /**
     * Define the reentry character of this mutex.
     * @param reenterable If true, allow thread reentry as default.
     * If false, throw a runtime exception on reentry.
     */
    public lck ( boolean reenterable){
	super();
	this.reenterable = reenterable;
    }

    /**
     * Clone this mutex object into an initialized state.
     */
    public lck copy(){
	try {
	    return (lck)this.clone();
	}
	catch (CloneNotSupportedException cx){
	    throw new IllegalStateException();
	}
    }

    /**
     * Clone this mutex object into an initialized state.
     */
    protected Object clone() throws CloneNotSupportedException {
	lck clo = (lck)super.clone();
	clo.entered = 0;
	clo.waiters = 0;
	clo.enteredT = null;
	return clo;
    }

    /**
     * The "lck" gate function.  Each method calls this typically on
     * entering a "lck"- protected area.  Only one thread is allowed
     * past at a time.
     * 
     * <pre>
     *  lck&nbsp;locker;
     *  try&nbsp;{
     *  &nbsp;&nbsp;&nbsp;&nbsp;locker.serialize(this);
     *
     *  &nbsp;&nbsp;&nbsp;&nbsp;<font color="#af0000">//(protected&nbsp;region)//</font>
     * 
     *  }
     *  finally&nbsp;{
     *  &nbsp;&nbsp;&nbsp;&nbsp;locker.unlock(this);
     *  }
     * </pre>
     *
     * @param user The object maintaining and using the lock.  The
     * object may have a <tt>"LckDesc"</tt> record for debugging the
     * lock.
     * 
     * @see #LckDesc 
     */
    public final synchronized void serialize( Object user){

	Thread ct = Thread.currentThread();

	if ( this.enteredT == ct){

	    if (debug) System.out.println( chbuf.cat("LCK ",ct.getName()," SERIALIZE REENTER (",LckDesc(user,this),") ",bpo.atStack(new Exception(),2))); 

	    if (this.reenterable)
		return ;      // re- entry
	    else
		throw new RuntimeException("Reentering mutex.");
	}
	else {
	    this.waiters += 1;

	    while (0 < this.entered){
		if (debug) System.out.println(chbuf.cat("LCK ",ct.getName()," SERIALIZE WAIT (",LckDesc(user,this),") WAITING ",bpo.atStack(new Exception(),2))); 

		try { 
		    this.wait();

		} catch ( InterruptedException intx){
		    if (debug) System.out.println(chbuf.cat("LCK ",ct.getName()," SERIALIZE WAIT-INTERRUPTED (",LckDesc(user,this),") WAITING ",bpo.atStack(new Exception(),2))); 
		}
	    }

	    if (debug) System.out.println(chbuf.cat("LCK ",ct.getName()," SERIALIZE EXIT (",LckDesc(user,this),") ",bpo.atStack(new Exception(),2)));

//      	    if (null != enteredT)
//      		throw new RuntimeException(chbuf.cat("LCK (",LckDesc(user,this),"@",ct.getName(),") is broken?!"));

	    synchronized(this){

		this.waiters -= 1;

		this.entered += 1; 

		this.enteredT = ct;
	    }
	}
    }
    /**
     * The "lck" gate keeper function.  Each thread exiting the "lck"-
     * protected area calls on this unlock function to allow the next
     * thread to enter the protected ("serialized") area. 
     *
     * <pre>
     *  lck&nbsp;locker;
     *  try&nbsp;{
     *  &nbsp;&nbsp;&nbsp;&nbsp;locker.serialize(this);
     *
     *  &nbsp;&nbsp;&nbsp;&nbsp;<font color="#af0000">//(protected&nbsp;region)//</font>
     * 
     *  }
     *  finally&nbsp;{
     *  &nbsp;&nbsp;&nbsp;&nbsp;locker.unlock(this);
     *  }
     * </pre>
     *
     * @param user The object maintaining and using the lock.  The
     * object may have a <tt>"LckDesc"</tt> record for debugging the
     * lock.
     * 
     * @see #LckDesc 
     */
    public final synchronized void unlock( Object user){

	if (null != this.enteredT){

	    Thread ct = Thread.currentThread();

	    if (debug) System.out.println(chbuf.cat("LCK ",ct.getName()," UNLOCK (",LckDesc(user,this),") ",bpo.atStack(new Exception(),2)));

	    this.entered -= 1;

	    if (this.reenterable){
		if ( 0 == this.entered){
		    if (ct == this.enteredT)
			this.enteredT = null;
		    else
			throw new IllegalStateException("Thread unlocking reentrant ["+ct.getName()+" != "+this.enteredT.getName()+"] "+bpo.atStack(new Exception(),2));
		}
	    }
	    else
		this.enteredT = null;

	    if ( 0 < this.waiters)
		this.notify();
	}
    }
    /**
     * If a thread has entered `serialized', the lock is considered "locked".
     */
    public final synchronized boolean isLocked(){ return (0 != this.entered);}
    /**
     * Number of threads entered past this lock.  Should be zero or
     * one!  */
    public final synchronized int entered(){ return this.entered;}
    /**
     * Number of threads waiting on this lock (waiting behind the
     * gate). */
    public final synchronized int waiters(){ return this.waiters;}
    /**
     * Classname with "entered" parameter in usual "java" square
     * bracket format.  */
    public String toString(){
	return chbuf.cat(getClass().getName(),"[entered=",Integer.toString(entered),",waiters=",Integer.toString(waiters),"]");
    }

    private static class test implements Runnable {
	private lck locker = new lck();
	private int count;
	test(int N){
	    super();
	    this.count = N;
	}
	private long pseudo(){
	    int re = this.count;
	    if (10 > re){
		this.count = 9;
		return 19;
	    }
	    else {
		this.count = (re-1);
		return (re+10);
	    }
	}
	private void enter(){
	    this.locker.serialize(Thread.currentThread());
	}
	private void exit(){
	    this.locker.unlock(Thread.currentThread());
	}
	public void run(){

	    try {
		Thread.sleep(this.pseudo());
	    }
	    catch (InterruptedException ix){}

	    this.enter();
	    System.out.println(Thread.currentThread().getName());
	    this.exit();
	}
    }
    private static void usage(){
	System.out.println();
	System.out.println("usage: gnu.iou.lck N");
	System.out.println();
	System.exit(1);
    }
    public static void main(String[] argv){
	if (null == argv || 1 != argv.length)
	    usage();
	else {
	    try {
		int N = Integer.parseInt(argv[0]);

		Thread.currentThread().setPriority(Thread.MAX_PRIORITY);

		test to = new test(N);

		Thread T;
		for (int cc = 0; cc < N; cc++){
		    T = new Thread(to);
		    T.setPriority(Thread.MIN_PRIORITY);
		    T.start();
		}
	    }
	    catch (NumberFormatException nfx){
		usage();
	    }
	}
    }
}
