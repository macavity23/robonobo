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

import java.io.DataInput;
import java.io.DataInputStream;
import java.util.StringTokenizer;

/**
 * Tool parses string with '%format%' replaceable tokens, inserts
 * arguments.  Syntax- compatible with cog template (as a subfeature
 * of "cog/build"), but does not interface to cog template features
 * from here.  
 *
 * <p> A "format" directive can optionally index the "argv" with a
 * "%format[idx]%" syntax, where "idx" is an index into "argv".  In
 * this case the running "argc" index into "argv" is not incremented.
 *
 * <p> A "format" directive can optionally apply processing commands
 * to the target string with a "%format(cmd args)%" syntax, where
 * "cmd" is interpreted by the "cmd" function, see "pad" and "col",
 * below.
 *
 * <p> By default, each instance of a "%format%" directive uses and
 * increments an internal index into the "argv".  Each call to the
 * "format" function gets a fresh internal "argv" index, "argc".  
 *
 * @see #cmd
 * @see #pad
 * @see #col
 * @see #format(java.lang.String,java.lang.String[])
 * @see #format(java.lang.String,java.lang.String)
 * @see #format(java.lang.String[][],java.lang.String[])
 * @see #format(java.lang.String[][],java.lang.String[],java.lang.String) 
 *
 * @author John Pritchard (john@syntelos.org)
 */
public abstract class format {

    /**
     * General user interface parses src for "%format%" elements.  
     * 
     * @param templ_src Source to be parsed by 'utl/templ' for "%...%"
     * directives.
     * 
     * @param argv Arguments to "%format%" directives (dynamic
     * replacement text).
     * 
     * @exception IllegalArgumentException For "%format%" syntax
     * errors, or "argv" array indexing (bounds) errors.  */
    public static String format ( String templ_src, String[] argv) throws IllegalArgumentException {

	return format( templ.parse(templ_src), argv);
    }

    /**
     * Format with one argument, creates stringary for you.
     */
    public static String format ( String templ_src, String arg) throws IllegalArgumentException {
	String[] argv = new String[1];
	argv[0] = arg;
	return format( templ.parse(templ_src), argv);
    }

    /**
     * "format"
     */
    private final static String default_format_key = "format";

    /**
     * Replace instances of "%format%" with elements of "argv".
     * 
     * @param srcary Source parsed by 'utl/templ' for "%...%"
     * directives.
     * 
     * @param argv Arguments to "%format%" directives (dynamic
     * replacement text).
     * 
     * @exception IllegalArgumentException For "%format%" syntax
     * errors, or "argv" array indexing (bounds) errors.  */
    public static String format ( String[][] templ_srcary, String[] argv) throws IllegalArgumentException {
	return format( templ_srcary, argv, default_format_key);
    }

    /**
     * User format key replaces the default "format" string.
     * 
     * @param srcary Source parsed by 'utl/templ' for "%...%"
     * directives.
     * 
     * @param argv Arguments to format directives (dynamic
     * replacement text).
     * 
     * @param format_key Optional string to identify format operators.  Default
     * "format".
     * 
     * @exception IllegalArgumentException For format syntax
     * errors, or "argv" array indexing (bounds) errors.  */
    public static String format ( String[][] srcary, String[] argv, String format_key) throws IllegalArgumentException {

	if ( null == srcary)
	    throw new IllegalArgumentException("Null source array argument.");

	else if ( null == format_key)

	    format_key = default_format_key;

	String format_key_prefix = "%"+format_key;

	String s, ss, sub[];

	int ii, idx, argc = 0, srclen = srcary.length, sublen;

	linebuf lb = new linebuf(((srclen>1)?(srclen):(2)));

	for ( int c = 0, lidx = 0, cc; c < srclen; c++, lidx++){

	    sub = srcary[c];

	    if ( null != sub){

		sublen = sub.length;

		for ( cc = 0;  cc < sublen; cc++){

		    s = sub[cc];

		    if ( null != s){

			if ( s.startsWith(format_key_prefix)){ // "%format"

			    idx = s.lastIndexOf("[");

			    if ( 0 < idx){

				ss = s.substring(idx+1);

				idx = ss.lastIndexOf("]");

				ss = ss.substring(0,idx).trim();

				try {
				    ii = Integer.parseInt(ss);
				}
				catch ( NumberFormatException nfx){
				    throw new IllegalArgumentException("Expected a number for `idx' in \""+format_key_prefix+"[idx]%\", in your \""+sub[cc]+"\", in \""+new linebuf(sub,"").toString()+"\".");
				}

				if ( 0 > ii || null == argv || ii >= argv.length)
				    throw new IllegalArgumentException("Format index ("+ii+") in your \""+sub[cc]+"\", in \""+new linebuf(sub,"").toString()+"\", is out of bounds for input argv {"+(new linebuf("`",argv,"',").toString())+"}.");

				idx = s.lastIndexOf("(");
				
				if ( 0 < idx){

				    ss = s.substring(idx+1);

				    idx = ss.lastIndexOf(")");

				    ss = ss.substring(0,idx).trim();

				    cmd ( lb, lidx, ss, argv[ii], srcary, c, cc);

				    lidx = lb.index();
				}
				else {

				    lb.index_append(lidx,argv[ii]);
				}
			    
			    }
			    else {
				idx = s.lastIndexOf("(");

				if ( null == argv || argc >= argv.length)
				    throw new IllegalArgumentException("Format index ("+argc+") implied in your \""+sub[cc]+"\", in \""+new linebuf(sub,"").toString()+"\", is out of bounds for input argv {"+(new linebuf("`",argv,"',").toString())+"}.");

				if ( 0 < idx){

				    ss = s.substring(idx+1);

				    idx = ss.lastIndexOf(")");

				    ss = ss.substring(0,idx).trim();

				    cmd ( lb, lidx, ss, argv[argc++], srcary, c, cc);

				    lidx = lb.index();
				}
				else {

				    lb.index_append( lidx, argv[argc++]);
				}
			    }
			}
			else
			    lb.index_append( lidx, sub[cc]);
		    }
		}
	    }
	    else 
		lb.println();
	}

	return lb.toString();
    }

    /**
     * Interprets format "(cmd args)" functions.
     *
     * <ul>
     *
     * <li> <b>(pad int)</b> -- Use "pad" on target to left- justify in
     * column of width "int".
     *
     * <li> <b>(pad int false)</b> -- Use the "pad" function on target
     * to right justify in column of width "int".
     *
     * <li> <b>(col int1 int2)</b> -- Use the "col" function to put
     * target into column "int1", with width "int2".
     *
     * </ul>
     * 
     * @param editbuf Buffer to write into
     *
     * @param lidx Buffer line index for this operation
     *
     * @param cmd The "(cmd args)" string
     * 
     * @param target The argv element applied to this command
     *
     * @param srcary The full text used in error reporting
     *
     * @param srci The full text primary index used in error reporting
     *
     * @param srcii The full text secondary index used in error reporting
     *
     * @see #pad
     * @see #col */
    public final static void cmd ( linebuf editbuf, int lidx, String cmd, String target, String[][] srcary, int srci, int srcii) {

	String[] cmdary = linebuf.toStringArray(cmd,"( ,)");

	String cc = cmdary[0];

	if ( "pad".equals(cc)){

	    if ( 1 < cmdary.length && null != cmdary[1]){

		boolean ljust = true;

		int len = 0;

		cc = cmdary[1];

		try {
		    
		    len = Integer.parseInt(cc);
		}
		catch ( NumberFormatException nfx){
		    throw new IllegalArgumentException("Expected a number for `pad(int)', found \""+cc+"\" in your \""+srcary[srci][srcii]+"\", in \""+new linebuf(srcary[srci],"").toString()+"\".");
		}

		if ( 0 > len)
		    throw new IllegalArgumentException("PAD length argument ("+len+") in your \""+srcary[srci][srcii]+"\", in \""+new linebuf(srcary[srci],"").toString()+"\", is invalid (negative).");

		if ( 2 < cmdary.length){

		    cc = cmdary[2];

		    if ( null != cc)
			ljust = cc.toLowerCase().equals("true"); //Boolean.toBoolean
		}

		pad (editbuf,lidx,target,len,ljust);

		// lidx = editbuf.index();
	    }
	    else
		throw new IllegalArgumentException("PAD format command, `"+cmd+"', in your \""+srcary[srci][srcii]+"\", in \""+new linebuf(srcary[srci],"").toString()+"\", requires at least a `len' argument.");
	}
	else if ( "col".equals(cc)){

	    if ( 3 == cmdary.length){

		int col0 = -1, cwide = -1;

		cc = cmdary[1];

		try {
		    
		    col0 = Integer.parseInt(cc);
		}
		catch ( NumberFormatException nfx){
		    throw new IllegalArgumentException("Expected an integer for `col0' in `col(col0,colwid)', found \""+cc+"\" in your \""+srcary[srci][srcii]+"\", in \""+new linebuf(srcary[srci],"").toString()+"\".");
		}

		cc = cmdary[2];

		try {
		    
		    cwide = Integer.parseInt(cc);
		}
		catch ( NumberFormatException nfx){
		    throw new IllegalArgumentException("Expected an integer for `colwid' in `col(col0,colwid)', found \""+cc+"\" in your \""+srcary[srci][srcii]+"\", in \""+new linebuf(srcary[srci],"").toString()+"\".");
		}

		col (editbuf,lidx,target,col0,cwide);

		// lidx = editbuf.index();
	    }
	    else
		throw new IllegalArgumentException("COL format command, `"+cmd+"', in your \""+srcary[srci][srcii]+"\", in \""+new linebuf(srcary[srci],"").toString()+"\", requires two arguments.");
	}
	else 
	    throw new IllegalArgumentException("Format command `"+cmd+"' in your \""+srcary[srci][srcii]+"\", in \""+new linebuf(srcary[srci],"").toString()+"\", not recognized.");
    }

    /**
     * Pad with space, or truncate to length.
     * 
     * @param editbuf Buffer to write into
     *
     * @param lidx Buffer line index for this operation
     * 
     * @param s String
     *
     * @param len Length of output 
     *
     * @param leftj  Left or right justified.
     *
     * @see #cmd
     */
    public final static void pad ( linebuf editbuf, int lidx, String s, int len, boolean leftj){

	if ( null == s){

	    char[] ret = new char[len--];

	    while ( len >= 0){

		ret[len--] = ' ';
	    }

	    editbuf.index_append( lidx, ret);
	}
	else {
	    char[] cary = linebuf.detab(s);

	    int slen = (null != cary)?(cary.length):(0);

	    if ( slen > len)

		editbuf.index_append( lidx, chbuf.substring(cary,0,len));

	    else if ( slen == len)

		editbuf.index_append( lidx, cary);

	    else {

		char[] ret = new char[len];

		if (leftj){

		    if ( 0 < slen) System.arraycopy(cary,0,ret,0,slen);

		    while ( slen < len){

			ret[slen++] = ' ';
		    }
		}
		else {
		    int ri = len-slen;

		    if ( 0 < slen) System.arraycopy(cary,0,ret,ri--,slen);

		    while ( ri >= 0){

			ret[ri--] = ' ';
		    }
		}

		editbuf.index_append( lidx, ret);
	    }
	}
    }

    /**
     * Wrap string into column using return elements for lines and the
     * ASCII SPACE character for offsetting.
     *
     * @param editbuf Buffer to write column into
     *
     * @param lidx Buffer first- line index.
     * 
     * @param s String source to fit into column
     *
     * @param col0 Column specification for first column index
     *
     * @param cwide Column specification for width 
     *
     * @see #cmd
     */
    public final static void col ( linebuf editbuf, int lidx, String s, int col0, int cwide){
	int slen = s.length();

	if ( slen <= cwide){

	    editbuf.index_column_overwrite(lidx,s,col0,cwide);

	    return ;
	}
	else {

	    StringTokenizer strtok = new StringTokenizer(s," \t");

	    chbuf sbuf = new chbuf(slen);

	    String tok;

	    char[] sub;

	    int sbuflen;

	    while (strtok.hasMoreTokens()){

		tok = chbuf.cat(strtok.nextToken()," ");

		sbuf.append(tok);

		sbuflen = sbuf.length();

		if ( sbuflen < cwide){

		    continue;
		}
		else if ( sbuflen == cwide){

		    sub = sbuf.toCary();

		    editbuf.index_column_overwrite(lidx++,sub,col0,cwide);

		    sbuf.reset();
		}
		else {

		    sbuf.popBuf(tok.length());

		    sub = sbuf.toCary();

		    editbuf.index_column_overwrite(lidx++,sub,col0,cwide);

		    sbuf.reset();

		    sbuf.append(tok);
		}
	    }

	    if ( 0 < sbuf.length()){

		sub = sbuf.toCary();

		editbuf.index_column_overwrite(lidx++,sub,col0,cwide);
	    }
	    return ;
	}
    }

    private final static String main_usage = " Usage: cat format-text | format [args]+";
    /**
     * Command line filter tool applies its arguments to the
     * `format'ed input stream.  */
    public static void main ( String[] argv){
	try {
	    if ( null == argv || 0 >= argv.length || (1 == argv.length && "?".equals(argv[0])))
		throw new IllegalArgumentException(main_usage);

	    DataInput din = new DataInputStream(System.in);

	    String line;

	    linebuf lb = new linebuf(1024);

	    while (null != (line = din.readLine()))
		lb.append(line);

	    String fmtxt = lb.toString();

	    if ( null != fmtxt)
		System.out.println(format(fmtxt,argv));
	    else
		System.out.println();
	}
	catch ( IllegalArgumentException ilx){
	    System.err.println(ilx.getMessage());
	}
	catch ( Exception exc){
	    exc.printStackTrace();
	}
    }
}
