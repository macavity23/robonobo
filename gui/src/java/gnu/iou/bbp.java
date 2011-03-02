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
 * Pipe for <tt>`bbo'</tt> and <tt>`bbi'</tt> implements input
 * blocking over <tt>`bbuf'</tt>.  
 * 
 * <p><b>Usage</b>
 * 
 * <pre>
 * bbp pipe = new bbp();
 * OutputStream pipeOut = new bbo(pipe);
 * InputStream pipeIn = new bbi(pipe);
 * </pre>
 * 
 * <p><b>Thread safety</b>
 * 
 * <p> This class is multi- thread safe only in the standard I/O API
 * defined here and used by <bb>`bbo'</bb> and <tt>`bbi'</tt>.
 * <i>Note that `bbuf' is not multi- thread safe.</i>
 * 
 * <p> The mutex locker is accessable via the <tt>`getLocker()'</tt>
 * method.
 * 
 * @author John Pritchard (john@syntelos.org)
 * 
 * @see bbo
 * @see bbi
 * @see dbo
 */
public class bbp extends bbuf {

    protected final lck locker = new lck();

    protected boolean apierrors = true; 

    public bbp(){
	super();
    }
    /**
     * @param api_exc If true, throw exceptions on I/O methods that
     * shouldn't be used on the pipe.  
     */
    public bbp( boolean api_exc){
	super();
	this.apierrors = api_exc;
    }

    public bbp ( int init){
	super(init);
    }

    public bbp ( byte[] buffer){
	super(buffer);
    }

    // bbp

    public lck getLocker(){ return locker;}

    // InputStream

    public synchronized int read() {
	while (true){
	    if ( 0 < super.available()){
		try {
		    locker.serialize(null);

		    if ( 0 < super.available())
			return super.read();
		}
		finally {
		    locker.unlock(null);
		}
	    }

	    try {
		synchronized(this){

		    this.wait();

		    continue;
		}
	    } catch ( InterruptedException intx){
		return -1;
	    }
	}
    }
    public synchronized int read(byte b[]) {
	return this.read( b, 0, b.length);
    }
    public synchronized int read(byte b[], int off, int len) {
	while (true){
	    if ( 0 < super.available()){
		try {
		    locker.serialize(null);

		    if ( 0 < super.available())
			return super.read(b, off, len);
		}
		finally {
		    locker.unlock(null);
		}
	    }

	    try {
		synchronized(this){

		    this.wait();

		    continue;
		}
	    } catch ( InterruptedException intx){
		return -1;
	    }
	}
    }
    public long skip(long n) {
	if (apierrors) 
	    throw new IllegalStateException("Skip not available on pipe.");
	else
	    return 0L;
    }

    public void mark(int readlimit) {}

    public void reset() {
	if (apierrors) throw new IllegalStateException("Reset not available on pipe.");
    }
    public boolean markSupported() {
	return false;
    }

    // OutputStream

    public synchronized void write(int b) {

	try {
	    locker.serialize(null);

	    super.write(b);

	    synchronized(this){
		this.notifyAll();
	    }
	}
	finally {
	    locker.unlock(null);
	}
    }
    public synchronized int write(byte b[]) {

	try {
	    locker.serialize(null);

	    int rc = super.write( b, 0, b.length);

	    synchronized(this){
		this.notifyAll();
	    }
	    return rc;
	}
	finally {
	    locker.unlock(null);
	}
    }
    public synchronized int write(byte b[], int off, int len) {

	try {
	    locker.serialize(null);

	    int rc = super.write( b, off, len);

	    synchronized(this){
		this.notifyAll();
	    }
	    return rc;
	}
	finally {
	    locker.unlock(null);
	}
    }
    public void flush() {
	if (apierrors) throw new IllegalStateException("Flush not available on pipe.");
    }
    public void close() {
	if (apierrors) throw new IllegalStateException("Close not available on pipe, will be GC'ed.");
    }
}
