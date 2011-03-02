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
 * An initialization protocol.  The plain init method is called which
 * returns zero for no other initialization method need be called, or
 * a known value for the user to cast its subject to another type and
 * to call that initialization method.  Of course the user and subject
 * will have a narrowed scope, where the init return value may be only
 * a subset of the possible values represented here.
 * 
 * @author John Pritchard 
 */
public interface init {

    /**
     * A user may elect to throw this exception for an unrecognized
     * (unsupported) value returned by the subject.  Note that the
     * java compiler will not enforce the declaration of this
     * exception because it its a subclass of {@link
     * java.lang.RuntimeException}.
     */
    public static class UnrecognizedReturnValue
	extends java.lang.IllegalStateException
    {
	public final java.lang.Object user;

	public final gnu.iou.init subject;

	public final int subject_rvalue;
	
	public UnrecognizedReturnValue(java.lang.Object user, init subject, int rvalue, String msg){
	    super(msg);
	    this.user = user;
	    this.subject = subject;
	    this.subject_rvalue = rvalue;
	}
    }

    public final static int PLAIN   = 0;
    public final static int ARGV    = 1;
    public final static int BOOLEAN = 2;
    public final static int INTEGER = 3;
    public final static int LONG    = 4;
    public final static int FLOAT   = 5;
    public final static int DOUBLE  = 6;
    public final static int MAP     = 7;
    public final static int BITS    = 8;

    /**
     * Init type argv
     */
    public interface Argv {

	public final static int INIT = ARGV;

	/**
	 * @param argv May be null but should not be (rather array
	 * length zero)
	 */
	public void init(java.lang.String[] argv);
    }
    /**
     * Init type bool
     */
    public interface Boolean {

	public final static int INIT = BOOLEAN;

	public void init(boolean arg);
    }
    /**
     * Init type integer
     */
    public interface Integer {

	public final static int INIT = INTEGER;

	public void init(int arg);
    }
    /**
     * Init type long
     */
    public interface Long {

	public final static int INIT = LONG;

	public void init(long arg);
    }
    /**
     * Init type float
     */
    public interface Float {

	public final static int INIT = FLOAT;

	public void init(float arg);
    }
    /**
     * Init type double
     */
    public interface Double {

	public final static int INIT = DOUBLE;

	public void init(double arg);
    }
    /**
     * Init type map
     */
    public interface Map {

	public final static int INIT = MAP;

	public void init(java.util.Map args);
    }
    /** 
     * Init type bits
     */
    public interface Bits {

	public final static int INIT = BITS;

	public void init(byte[] bits);
    }

    /**
     * @return A value greater than zero for another kind of
     * initialization
     */
    public int init();

}
