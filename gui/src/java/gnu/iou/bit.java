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
 * Bit array encoding: seven bits per byte.
 *
 * <p> An enabled high bit marks the use of the following bit values.
 * The highest bit turned on marks the upper bound of the bit array.
 *
 * <p> Each byte is indexed from high to low bits, left to right,
 * index zero to N.  In the first example, below, bit [0] is on, while
 * in the second and third examples it is off.  In the fourth example,
 * bit [0] is on and bit [1] is off.
 *
 * <pre>
 *   11010101    Seven bits: on, off, on, off, on, off, on. 
 *   01000000    Six bits, off.
 *   00001010    Three bits, off, on, and off.
 *   00000110    Two bits, off and on.
 *   00000011    One bit, on.
 *   00000000    No bits (invalid).
 * </pre>
 *
 * <p> An array of bits beyond seven bits uses multiple of these
 * bytes, each employing the identical "high bit" encoding.
 *
 * <p> For example, a bit array field of ten bits is two bytes:
 * <i>11010101,</i> and <i>00001010.</i> The zeroth bit <i>(byte[0] &
 * 0x40)</i> is on and the nineth bit <i>(byte[1] & 0x1)</i> is off.
 *
 * <p><b>Contiguous</b>
 *
 * <p> A byte with fewer than seven bit bits is not followed by
 * another byte in a bit array field, in order that implementations
 * can quickly calculate the number of bits in an array by counting
 * the number bytes, and then checking the number of bits in the last
 * byte.
 *
 * @author John Pritchard (john@syntelos.org)
 */
public class bit {

    /**
     * Boolean byte bit mask for the eigth bit (10000000). */
    public final static int BOOL_BIT_7 = 0x80;
    /**
     * Boolean byte bit mask for the seventh bit (01000000). */
    public final static int BOOL_BIT_6 = 0x40;
    /**
     * Boolean byte bit mask fir the sixth bit (00100000).*/
    public final static int BOOL_BIT_5 = 0x20;
    /**
     * Boolean byte bit mask for the fifth bit (00010000). */
    public final static int BOOL_BIT_4 = 0x10;
    /**
     * Boolean byte bit mask for the fourth bit (00001000). */
    public final static int BOOL_BIT_3 = 0x08;
    /**
     * Boolean byte bit mask for the third bit (00000100). */
    public final static int BOOL_BIT_2 = 0x04;
    /**
     * Boolean byte bit mask for the second bit (00000010). */
    public final static int BOOL_BIT_1 = 0x02;
    /**
     * Boolean byte bit mask for the first bit (00000001). */
    public final static int BOOL_BIT_0 = 0x01;

    /**
     * Encode boolean array to bit array.
     * 
     * @param boolary Array of booleans to encode.
     */
    public final static byte[] encode( boolean[] boolary){

	if ( null == boolary)
	    return null;
	
	byte[] bary;

	int cc = boolary.length;

	if ( 7 >= cc){
	    bary = new byte[1];
	    bary[0] = (byte)0;
	    bary[0] |= (byte)(1<<cc);
	    
	    for ( int c = 0; c < cc; c++){
		if ( boolary[c])
		    bary[0] |= (byte)(1<<c);
	    }
	    return bary;
	}
	else if ( 0 < (cc % 7))

	    bary = new byte[(cc/7)+1];
	else 
	    bary = new byte[(cc/7)];

	for ( int c = 0, ccc, cccc = 0; c < bary.length; c++){

	    bary[c] = (byte)0;

	    if ( ((c+1)*7) <= cc){
		bary[c] |= BOOL_BIT_7;

		for ( ccc = 0; ccc < 7; ccc++){

		    if ( boolary[cccc++])

			bary[c] |= (byte)(1<<ccc);
		}
	    }
	    else {
		int mod = cc % 7;
		bary[c] |= (byte)(1<<mod);

		for ( ccc = 0; ccc < mod; ccc++){

		    if ( boolary[cccc++])

			bary[c] |= (byte)(1<<ccc);
		}
		// break (meaning covered by `; c < bary.length;', above)
	    }
	}
	return bary;
    }

    private final static boolean[] grow_many ( boolean[] src, int many){
	if ( 0 >= many) return src;

	if ( null == src)
	    return new boolean[many];
	else {
	    boolean[] copier = new boolean[src.length+many];
	    System.arraycopy(src,0,copier,0,src.length);
	    return copier;
	}
    }

    /**
     * Decode bits to array of booleans.
     *
     * @param bary Source buffer
     * 
     * @param ofs Offset in source buffer
     * 
     * @param len Number of bytes in source buffer
     */
    public final static boolean[] decode( byte[] bary, int ofs, int len){
	if ( null == bary)
	    return null;
	else {
	    boolean boolary[] = null;
	    for ( int c = ofs, cc; c < len; c++){
		if ( 1 == (bary[c] & BOOL_BIT_7)){
		    // seven bits used
		    boolary = grow_many(boolary,7);
		    cc = boolary.length;

		    boolary[cc-6] = (1 == (bary[c] & BOOL_BIT_0));
		    boolary[cc-5] = (1 == (bary[c] & BOOL_BIT_1));
		    boolary[cc-4] = (1 == (bary[c] & BOOL_BIT_2));
		    boolary[cc-3] = (1 == (bary[c] & BOOL_BIT_3));
		    boolary[cc-2] = (1 == (bary[c] & BOOL_BIT_4));
		    boolary[cc-1] = (1 == (bary[c] & BOOL_BIT_5));
		    boolary[cc] = (1 == (bary[c] & BOOL_BIT_6));
		}
		else if ( 1 == (bary[c] & BOOL_BIT_6)){
		    // six bits used
		    boolary = grow_many(boolary,6);
		    cc = boolary.length;

		    boolary[cc-5] = (1 == (bary[c] & BOOL_BIT_0));
		    boolary[cc-4] = (1 == (bary[c] & BOOL_BIT_1));
		    boolary[cc-3] = (1 == (bary[c] & BOOL_BIT_2));
		    boolary[cc-2] = (1 == (bary[c] & BOOL_BIT_3));
		    boolary[cc-1] = (1 == (bary[c] & BOOL_BIT_4));
		    boolary[cc] = (1 == (bary[c] & BOOL_BIT_5));
		}
		else if ( 1 == (bary[c] & BOOL_BIT_5)){
		    // five bits used
		    boolary = grow_many(boolary,5);
		    cc = boolary.length;

		    boolary[cc-4] = (1 == (bary[c] & BOOL_BIT_0));
		    boolary[cc-3] = (1 == (bary[c] & BOOL_BIT_1));
		    boolary[cc-2] = (1 == (bary[c] & BOOL_BIT_2));
		    boolary[cc-1] = (1 == (bary[c] & BOOL_BIT_3));
		    boolary[cc] = (1 == (bary[c] & BOOL_BIT_4));
		}
		else if ( 1 == (bary[c] & BOOL_BIT_4)){
		    // four bits used
		    boolary = grow_many(boolary,4);
		    cc = boolary.length;

		    boolary[cc-3] = (1 == (bary[c] & BOOL_BIT_0));
		    boolary[cc-2] = (1 == (bary[c] & BOOL_BIT_1));
		    boolary[cc-1] = (1 == (bary[c] & BOOL_BIT_2));
		    boolary[cc] = (1 == (bary[c] & BOOL_BIT_3));
		}
		else if ( 1 == (bary[c] & BOOL_BIT_3)){
		    // three bits used
		    boolary = grow_many(boolary,3);
		    cc = boolary.length-1;

		    boolary[cc-2] = (1 == (bary[c] & BOOL_BIT_0));
		    boolary[cc-1] = (1 == (bary[c] & BOOL_BIT_1));
		    boolary[cc] = (1 == (bary[c] & BOOL_BIT_2));
		}
		else if ( 1 == (bary[c] & BOOL_BIT_2)){
		    // two bits used
		    boolary = grow_many(boolary,2);
		    cc = boolary.length-1;

		    boolary[cc-1] = (1 == (bary[c] & BOOL_BIT_0));
		    boolary[cc] = (1 == (bary[c] & BOOL_BIT_1));
		}
		else if ( 1 == (bary[c] & BOOL_BIT_1)){
		    // one bit used
		    boolary = grow_many(boolary,1);

		    boolary[boolary.length-1] = (1 == (bary[c] & BOOL_BIT_0));
		}
		else
		    continue; // no bits used
	    }
	    return boolary;
	}
    }
}
