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
 * <p> This class contains static tools for doing UTF-8 encoding and
 * decoding.</p>
 *
 * <p> UTF-8 is ASCII- transparent.  It supports character sets
 * requiring more than the seven bit ASCII base range of UTF-8,
 * including Unicode, ISO-8859, ISO-10646, etc..</p>
 * 
 * <p> We do not use an ISO UCS code signature, and we do not use a
 * Java Data I/O- style strlen prefix.  </p>
 *
 * @author John Pritchard (jdp@syntelos)
 */
public abstract class utf8 {

    /**
     * Decode UTF-8 input, terminates decoding at a null character,
     * value 0x0.
     * 
     * @exception IllegalStateException Bad format.
     */
    public final static char[] decode( byte[] code){

	if ( null == code) return null;

	return decode(code,0,code.length);
    }
    /**
     * Decode UTF-8 input, terminates decoding at a null character,
     * value 0x0.
     * 
     * @exception IllegalStateException Bad format.
     */
    public final static char[] decode( byte[] code, int off, int many){

	if ( null == code || 0 >= code.length) 
	    return null;
	else {
	    chbuf strbuf = new chbuf(code.length);
	    int trm = (off+many);
	    int ch, ch2, ch3;
	    char tmpc;
	    for ( int cc = off; cc < trm; ){
		ch = (code[cc]&0xff);
		switch (ch >> 4) {
		case 0:
		case 1:
		case 2:
		case 3:
		case 4:
		case 5:
		case 6:
		case 7:
		    cc += 1;
		    tmpc = (char)ch; // for debugging
		    strbuf.append(tmpc);
		    break;
		case 12: 
		case 13:
		    cc += 2;
		    if (cc > trm)
			throw new IllegalStateException();
		    else {
			ch2 = (int) (code[cc-1]&0xff);
			if (0x80 != (ch2 & 0xC0))
			    throw new IllegalStateException();
			else {
			    tmpc = (char)(((ch & 0x1F) <<6)|(ch2 & 0x3F));
			    strbuf.append(tmpc);
			}
		    }
		    break;
		case 14:
		    cc += 3;
		    if (cc > trm)
			throw new IllegalStateException();
		    else {
			ch2 = (code[cc-2]&0xff);
			ch3 = (code[cc-1]&0xff);
			if ((0x80 != (ch2 & 0xC0)) || (0x80 != (ch3 & 0xC0)))
			    throw new IllegalStateException();
			else {
			    tmpc = (char)(((ch  & 0x0F) << 12)|
					  ((ch2 & 0x3F) << 6) |
					  ((ch3 & 0x3F) << 0));
			    strbuf.append(tmpc);
			}
		    }
		    break;
		default:
		    throw new IllegalStateException();		  
		}
	    }
	    return strbuf.toCary();
	}
    }

    /**
     * Encode string in UTF-8.
     */
    public final static byte[] encode( char[] str){

	if ( null == str || 0 >= str.length) return null;

	bbuf bytbuf = encode( str, null);

	return bytbuf.toByteArray();
    }

    /**
     * Encode string in UTF-8.
     */
    public final static bbuf encode( char[] str, bbuf bytbuf){

	if ( null == bytbuf) bytbuf = new bbuf( str.length);

	if ( null == str || 0 >= str.length) return bytbuf;

	char ch, sch;

	for ( int cc = 0, len = str.length; cc < len; cc++){

	    ch = str[cc];
	    if ((0x0 < ch) && (0x80 > ch))
		bytbuf.write(ch);
	    else if (0x07FF < ch){
		bytbuf.write(0xE0 | ((ch >> 12) & 0x0F));
		bytbuf.write(0x80 | ((ch >>  6) & 0x3F));
		bytbuf.write(0x80 | (ch & 0x3F));
	    }
	    else {
		bytbuf.write(0xC0 | ((ch >>  6) & 0x1F));
		bytbuf.write(0x80 | (ch & 0x3F));
	    }
	}
	return bytbuf;
    }

    /**
     * Encode string in UTF-8.
     */
    public final static byte[] encode ( String s){

	if ( null == s)
	    return null;
	else {

	    bbuf bytbuf = encode(s.toCharArray(),null);

	    if ( 0 < bytbuf.length())

		return bytbuf.toByteArray();
	    else
		return null;
	}
    }

    /**
     * Add null padding to paddedlen if necessary.
     */
    public final static byte[] encode ( String s, int paddedlen){

	if ( null == s)
	    return null;
	else {
	    bbuf bytbuf = encode(s.toCharArray(),null);

	    int bblen = bytbuf.length();

	    int delta = paddedlen- bblen;

	    if ( 0 < delta)
		bytbuf.nwrite( (byte)0, delta);

	    return bytbuf.toByteArray();
	}
    }

    /**
     * Returns the length of the string encoded in UTF-8.
     */
    public final static int encoded ( String s){

	if ( null == s)
	    return 0;
	else
	    return encoded(s.toCharArray());
    }

    /**
     * Returns the length of the string encoded in UTF-8.
     */
    public final static int encoded( char[] str){

	if ( null == str || 0 >= str.length) return 0;

	int bytlen = 0;

	char ch, sch;

	for ( int c = 0; c < str.length; c++){

	    ch = str[c];

	    if (  0x7f >= ch)

		bytlen++;

	    else if ( 0x7ff >= ch)

		bytlen += 2;

	    else 
		bytlen += 3;

	}

	return bytlen;
    }

    /**
     * The ubiquitous exclusive- or hash.  XORs each byte into the
     * long integer from ascending input bits order into ascending
     * hash bits order.
     *
     * <p> The hashing order reverses binary numeric input in hashing
     * it: the big- endian byte zero of binary numeric input would be
     * a high byte.
     * 
     * <pre>
     * |     long hash: 8 bytes, 64 bits     |
     * | high                            low |
     *
     * b[7] b[6] b[5] b[4] b[3] b[2] b[1] b[0] 
     *   
     * b[f] b[e] b[d] b[c] b[b] b[a] b[9] b[8] 
     * 
     *  ...
     *
     * </pre>
     * 
     * @param b Binary input to hash.
     * 
     * @exception IllegalArgumentException For a null argument.
     */
    public final static long xor64 ( byte[] b){
	if ( null == b) throw new IllegalArgumentException("Null argument for hash function.");

	long accum = 0, tmp;

	int shift ;

	for ( int c = 0, uc = b.length- 1; c < b.length; c++, uc--){

	    shift = ((uc % 8) * 8);

	    tmp = ((long)b[c] << shift);

	    accum ^= tmp;
	}
	return accum;
    }

    /**
     * Hash the ASCII string (the low byte of wide character values).
     * 
     * @param str String to hash
     * 
     * @exception IllegalArgumentException For null argument.
     */
    public final static long xor64_ascii ( String str){
	if ( null == str) throw new IllegalArgumentException("Null argument for hash function.");

	int strlen = str.length();

	byte[] bb = new byte[strlen];

	str.getBytes(0,strlen,bb,0);

	return xor64(bb);
    }

    /**
     * Hash the UTF-8 string.
     * 
     * @param str String encoded into UTF-8, then hashed.
     * 
     * @exception IllegalArgumentException For null argument or empty
     * string.
     */
    public final static long xor64 ( String str){

	return xor64( encode(str));
    }

    /**
     * Unique file path and state hash: a file will always have the
     * same hash until it is modified.
     * 
     * <p> Hash of path and last modified date as used for the server
     * response "ETag" header in "HTTP/1.1".
     *
     * <p> The file's "path" is the String value with which the object
     * was constructed, not necessarily its absolute path or canonical
     * path.  Its hash is the xor- fold of the low bytes of the string
     * as ASCII characters.  Foregoing UTF encoding for all paths,
     * using only the low ASCII bytes, is seen as a "good enough"
     * solution for performance reasons: it will not suffer any loss
     * of consistency.
     *
     * <p> Many users (both clients and servers) will construct their
     * file objects using their absolute path for many reasons, and so
     * this function hashes only the user's explicit input.
     * 
     * @param f File path is hashed, then XOR'ed with its last
     * modified timestamp (in milliseconds since Jan 1 1970 UT).
     * 
     * @exception IllegalArgumentException For null argument.
     */
    public final static String ETag ( java.io.File f){
	if ( null == f) throw new IllegalArgumentException("Null file argument to ETag function.");

	String pathname = f.getPath();

	long dirhash = xor64_ascii(pathname);

	long dirtims = f.lastModified();

	return Long.toHexString(dirhash ^ dirtims);
    }

}
