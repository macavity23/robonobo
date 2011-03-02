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
 * <p> Handling code <code>application/x-www-form-urlencoded</code> </p>
 * 
 * @author John Pritchard (john@syntelos.org)
 */
public abstract class url {

    public final static String decode( String source){
	if (null == source)
	    return null;
	else
	    return decode( source, source.toCharArray(), 0, source.length());
    }
    public final static String decode( char[] source, int ofs, int many){
	if (null == source || 0 > ofs || 1 > many)
	    return null;
	else
	    return decode( null, source, ofs, many);
    }
    /**
     * @param origin Possibly non- null origin of the character array
     * source, can be returned if the character array source has
     * nothing to decode
     * @param source Must be non- null
     * @param ofs Must be valid
     * @param many Must be valid
     */
    public final static String decode( String origin, char[] source, int ofs, int many){
	chbuf re = new chbuf(many);
	char ch;
	for (int cc = ofs, len = (ofs+many); cc < len; cc++){
	    ch = source[cc];
	    if ( '%' == ch){
		cc += 1;
		if (cc < len && (cc+1) < len)
		    re.append( char_decode( source, cc));
		cc += 1;
	    }
	    else
		re.append(ch);
	}
	if (null != origin && re.length() == origin.length())
	    return origin;
	else
	    return re.toString();
    }
    public final static String encode( String source){
	if (null == source)
	    return null;
	else
	    return encode( source, source.toCharArray(), 0, source.length());
    }
    public final static String encode( char[] source, int ofs, int many){
	if (null == source || 0 > ofs || 1 > many)
	    return null;
	else
	    return encode( null, source, ofs, many);
    }
    /**
     * @param origin Possibly non- null origin of the character array
     * source, can be returned if the character array source has
     * nothing to encode
     * @param source Must be non- null
     * @param ofs Must be valid
     * @param many Must be valid
     * @return Encode everything but the set of alpha numeric ASCII characters
     */
    public final static String encode( String origin, char[] source, int ofs, int many){
	chbuf re = new chbuf(many);
	char ch;
	char[] encbuf = new char[3];
	enc:
	for (int cc = ofs, len = (ofs+many); cc < len; cc++){
	    ch = source[cc];
	    switch(ch){
	    case '0':
	    case '1':
	    case '2':
	    case '3':
	    case '4':
	    case '5':
	    case '6':
	    case '7':
	    case '8':
	    case '9':
	    case 'a':
	    case 'b':
	    case 'c':
	    case 'd':
	    case 'e':
	    case 'f':
	    case 'g':
	    case 'h':
	    case 'i':
	    case 'j':
	    case 'k':
	    case 'l':
	    case 'm':
	    case 'n':
	    case 'o':
	    case 'p':
	    case 'q':
	    case 'r':
	    case 's':
	    case 't':
	    case 'u':
	    case 'v':
	    case 'w':
	    case 'x':
	    case 'y':
	    case 'z':
	    case 'A':
	    case 'B':
	    case 'C':
	    case 'D':
	    case 'E':
	    case 'F':
	    case 'G':
	    case 'H':
	    case 'I':
	    case 'J':
	    case 'K':
	    case 'L':
	    case 'M':
	    case 'N':
	    case 'O':
	    case 'P':
	    case 'Q':
	    case 'R':
	    case 'S':
	    case 'T':
	    case 'U':
	    case 'V':
	    case 'W':
	    case 'X':
	    case 'Y':
	    case 'Z':
	    case '.':
	    case '/':
	    case '-':
	    case '_':

		re.append(ch);
		continue enc;

	    default:
		char_encode(ch,encbuf,0);
		re.append(encbuf,0,3);
		continue enc;
	    }
	}
	if (null != origin && re.length() == origin.length())
	    return origin;
	else
	    return re.toString();
    }


    /**
     * Two ASCII hex digits into one character value
     * @param cary Character array source of two (ASCII) hex digits
     * must not be null.  Accepts <code>ofs</code> pointing to '%', or
     * to the first hex char.
     * @param ofs Offset into character array source for two (ASCII)
     * hex digits must be valid, length for this offset and one more
     * must be valid.
     */
    public final static char char_decode( char[] cary, int ofs){

	int many = ((null != cary)?(cary.length):(0));

	boolean fin = false;

	if ( 0 > ofs) 
	    ofs = 0;

	char re = (char)0;

        int ch;
	parse:
        for ( int cc = ofs, len = many; cc < len; cc++){

	    ch = cary[cc];
	    switch(ch){
	    case '0':
	    case '1':
	    case '2':
	    case '3':
	    case '4':
	    case '5':
	    case '6':
	    case '7':
	    case '8':
	    case '9':
		if (fin){
		    re += (char)(ch-'0');
		    return re;
		}
		else {
		    fin = true;
		    re += (char)((ch-'0')<<4);
		    continue parse;
		}
	    case 'a':
	    case 'b':
	    case 'c':
	    case 'd':
	    case 'e':
	    case 'f':
		if (fin){
		    re += (char)((ch-'a')+10);
		    return re;
		}
		else {
		    fin = true;
		    re += (char)(((ch-'a')+10)<<4);
		    continue parse;
		}
	    case 'A':
	    case 'B':
	    case 'C':
	    case 'D':
	    case 'E':
	    case 'F':
		if (fin){
		    re += (char)((ch-'A')+10);
		    return re;
		}
		else {
		    fin = true;
		    re += (char)(((ch-'A')+10)<<4);
		    continue parse;
		}
	    case '%':
		continue parse;
	    default:
		break parse;
            }
        }
	throw new IllegalArgumentException(String.valueOf(ofs));
    }

    private final static char[] hexchars = { '0', '1', '2', '3', '4', '5', '6', 
'7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f' };

    /**
     * @param ch Character value to encode into <code>'%'</code> and two hex digits
     * @param target Character array for hex digits
     * @param ofs Offset into character array for three characters must
     * be valid, and array length for one more offset must be valid
     */
    public final static void char_encode( char ch, char[] target, int ofs){
	int value = ch;
	target[ofs++] = '%';
	target[ofs++] = hexchars[(value>>4)&0xf];
	target[ofs] = hexchars[(value&0xf)];
	return;
    }


    /**
     * <p> Parse query string into map. </p>
     */
    public final static objmap querystring( String query){
	return querystring(null,query);
    }
    /**
     * <p> Parse query string into map. </p>
     */
    public final static objmap querystring( objmap dict, String query){
	if (null == query)
	    return dict;
	else {
	    char[] string = query.toCharArray();
	    int string_len = string.length;
	    if (1 > string_len)
		return dict;
	    else {
		if (null == dict)
		    dict = new objmap();
		//
		int idx = 0;
		String name = null, value = null;
		for (int scan = 0; scan < string_len; scan++){
		    switch(string[scan]){
		    case '&':
			value = new java.lang.String(string,idx,(scan-idx));
			idx = (scan+1);
			dict.put(name,value);
			name = null;
			value = null;
			break;
		    case '=':
			name = new java.lang.String(string,idx,(scan-idx));
			idx = (scan+1);
			break;
		    default:
			break;
		    }
		}
		/*
		 * Tail case
		 */
		if (0 < idx && null != name){
		    value = new java.lang.String(string,idx,(string_len-idx));
		    dict.put(name,value);
		}
		return dict;
	    }
	}
    }
}
