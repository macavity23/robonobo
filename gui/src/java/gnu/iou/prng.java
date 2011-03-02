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
 * 
 *
 * @author John Pritchard
 */
public abstract class prng {

    static {
	/*
	 * The DOM uses this class, so it's a good place to initialize
	 * the filter registry.
	 */
	gnu.iou.fil.Registry.Defaults();
    }

    /**
     * A scheduler is a minimum priority thread that periodically
     * discards a number of random bits from a PRNG, increasing its
     * randomness by periodically iterating its deterministic
     * sequence.
     * 
     * <p> The scheduler employs a duty cycle upper bound as a number
     * of milliseconds between cycles.  The actual cycle delay is
     * derived from the random value returned by the PRNG.
     * 
     * <p> While the scheduler's "random" cycling is not in itself a
     * source of true randomness, its contribution to randomness in
     * thread scheduling and randomness in calls to the shared PRNG is
     * certainly a component of the total contribution of the
     * scheduler to randomness.
     * 
     * <p> A typical PRNG doesn't collect randomness.  Any randomness
     * in this case is the chance occurance from an external
     * perspective.
     * 
     * <p> While this approach contributes only a very weak
     * randomness, it can be useful as predicting the resulting PRNG
     * output sequence is effectively obviated across shared PRNG
     * users: an external user looking at an application's use of a
     * shared PRNG will have reduced utility from its local copy of
     * the PRNG.  This remote PRNG will only be able to predict a
     * limited number of bits in the observed PRNG sequence before the
     * sequence skips, and the frequency of these skips will be
     * tedious to deal with --- at best.  It becomes increasingly
     * likely, subject to the system's use of the shared PRNG, that a
     * remote observer would have better success observing the system
     * by other means.
     * 
     * @author John Pritchard
     */
    public static class scheduler extends Thread {

	private volatile static int tn = 0;

	private final java.util.Random prng;

	private final long cycle_bound;

	private final int bytes_bound;

	/**
	 * @param prng A PRNG to schedule.
	 * 
	 * @param cycle Upper bound for the delay in milliseconds
	 * between cycles.
	 * 
	 * @param bits Upper bound for the number of bits (a multiple
	 * of eight) to take from the PRNG on each cycle.
	 */
	public scheduler( java.util.Random prng, long cycle, int bits){
	    super("gnu.iou.prng$scheduler-"+(tn++));

	    this.prng = prng;

	    super.setPriority(MIN_PRIORITY);

	    super.setDaemon(true);

	    this.cycle_bound = cycle;

	    bits/= 8;

	    if ( 0 == bits) 
		this.bytes_bound = 1;
	    else
		this.bytes_bound = bits;
	}
	/**
	 * A scheduler with a cycle delay of one half second, taking
	 * eight bits on each cycle.
	 *
	 * @param prng A PRNG to schedule.
	 */
	public scheduler( java.util.Random prng){
	    this(prng,499,8);
	}

	public void run(){
	    try {
		/*
		 * This code is a bit weird.  
		 * 
		 * Both the cycle time and collection bits (bytes
		 * length) are variables derived from random bits.
		 */
		long cycle = this.cycle_bound;

		int len = this.bytes_bound;

		byte[] bits = null;


		while(true){

		    Thread.sleep(cycle);

		    if ( null == bits || bits.length != len)
			bits = new byte[len];

		    this.prng.nextBytes(bits);

		    /*
		     * Next cycle delay.
		     */

		    cycle = (cycle ^ LongXor(bits)) % this.cycle_bound;

		    if (0 == cycle)
			cycle = this.cycle_bound;

		    /*
		     * Next number of random bytes.
		     */

		    len = (len ^ IntegerXor(bits)) % this.bytes_bound;

		    if (0 == len)
			len = this.bytes_bound;

		    continue;
		}
	    }
	    catch ( InterruptedException intx){

		return;
	    }
	}
    }

    /**
     * Scheduler for the Fast PRNG.  (The Slow PRNG doesn't have a
     * scheduler because a scheduler on the slow PRNG would not be
     * practical.)
     */
    private static scheduler fastsche = null;
    /**
     * Shared PRNG initialization monitor.
     */
    private final static Object randmon = new Object();

    /**
     * The fast, shared PRNG for nondeterministic bits is initialized
     * on demand.
     */
    private volatile static java.util.Random fast = null;

    /**
     * A shared weak PRNG which is periodically reseeded
     */
    public final static java.util.Random Instance(){
	synchronized(randmon){
	    if ( null == fast){
		fast = new java.util.Random();
		fastsche = new scheduler(fast,500000,64);
		fastsche.start();
	    }
	}
	return fast;
    }

    /**
     * Eight bytes of random bits
     */
    public final static long RandLong(){

	return Instance().nextLong();
    }

    /**
     * Eight bytes of random bits
     */
    public final static byte[] RandLongBits(){

	return Long(Instance().nextLong());
    }

    /**
     * Eight bytes of random bits in a hexidecimal string value.
     */
    public final static String RandLongStringHex(){

	return chbuf.hex(Long(Instance().nextLong()));
    }

    /**
     * Eight bytes of random bits in a hexidecimal string value.
     */
    public final static byte[] RandLongAsciiHex(){

	return chbuf.hex_ascii(Long(Instance().nextLong()));
    }

    /**
     * Eight bytes of random bits in a base64 string value.
     */
    public final static String RandLongStringB64(){

	return new java.lang.String(b64.encode(Long(Instance().nextLong())));
    }

    /**
     * Eight bytes of random bits in a base64 string value.
     */
    public final static byte[] RandLongAsciiB64(){

	return b64.encode(Long(Instance().nextLong()));
    }

    /**
     * Fast and efficient checksum: XOR- fold input bits into output
     * in big- endian order.
     * 
     * <p> Eight bytes representing a big endian long integer will
     * produce the value of the integer.
     */
    public final static long LongXor ( byte[] b){
	if ( null == b) throw new IllegalArgumentException("Null argument for hash function.");

	long accum = 0, tmp;

	int shift ;

	for ( int c = 0, uc = b.length- 1; c < b.length; c++, uc--){

	    shift = ((uc % 8)<<3);

	    tmp = (b[c]&0xff);

	    tmp <<= shift;

	    accum ^= tmp;
	}
	return accum;
    }

    /**
     * Fast and efficient checksum: XOR- fold input bits into output
     * in big- endian order.
     * 
     * <p> Four bytes representing a big endian integer will produce
     * the value of the integer.
     */
    public final static int IntegerXor ( byte[] b){
	if ( null == b) throw new IllegalArgumentException("Null argument for hash function.");

	int accum = 0, tmp;

	int shift ;

	for ( int c = 0, uc = b.length- 1; c < b.length; c++, uc--){

	    shift = ((uc % 4)<<2);

	    tmp = (b[c]&0xff);

	    tmp <<= shift;

	    accum ^= tmp;
	}
	return accum;
    }

    /**
     * Fast and efficient checksum: XOR- fold input bits into output
     * in big- endian order.
     * 
     * <p> Two bytes representing a big endian short integer will
     * produce the value of the integer.
     */
    public final static short ShortXor ( byte[] b){
	if ( null == b) throw new IllegalArgumentException("Null argument for hash function.");

	short accum = 0, tmp;

	int shift ;

	for ( int c = 0, uc = b.length- 1; c < b.length; c++, uc--){

	    shift = ((uc % 2)<<1);

	    tmp = (short)(b[c]&0xff);

	    tmp <<= shift;

	    accum ^= tmp;
	}
	return accum;
    }

    /**
     * For eight bytes in big- endian order, return their integer
     * value.
     */
    public final static long Long( byte[] buf, int ofs){
	long ret = 0, reg;

	int len = buf.length;

	if ( 8 < len) len = 8;

	for ( int cc = ofs, sh = 56; cc < len; cc++, sh -= 8){

	    reg = (buf[cc]&0xff);

	    ret += reg<<sh;
	}
	return ret;
    }

    /**
     * Eight bytes with the big- endian binary representation of the
     * argument value.
     */
    public final static byte[] Long( long num){

	byte[] ret = new byte[8];

	ret[0] = (byte)((num>>>56)&0xff);
	ret[1] = (byte)((num>>>48)&0xff);
	ret[2] = (byte)((num>>>40)&0xff);
	ret[3] = (byte)((num>>>32)&0xff);
	ret[4] = (byte)((num>>>24)&0xff);
	ret[5] = (byte)((num>>>16)&0xff);
	ret[6] = (byte)((num>>>8)&0xff);
	ret[7] = (byte)(num&0xff);

	return ret;
    }
    /**
     * For eight bytes in big- endian order, return their integer
     * value.
     */
    public final static int Integer( byte[] buf, int ofs){
	int ret = 0, reg;

	int len = buf.length;

	if ( 4 < len) len = 4;

	for ( int cc = ofs, sh = 24; cc < len; cc++, sh -= 4){

	    reg = (buf[cc]&0xff);

	    ret += reg<<sh;
	}
	return ret;
    }

    /**
     * Eight bytes with the big- endian binary representation of the
     * argument value.
     */
    public final static byte[] Integer( int num){

	byte[] ret = new byte[4];

	ret[0] = (byte)((num>>>24)&0xff);
	ret[1] = (byte)((num>>>16)&0xff);
	ret[2] = (byte)((num>>>8)&0xff);
	ret[3] = (byte)(num&0xff);

	return ret;
    }

}
