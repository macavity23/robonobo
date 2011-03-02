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

import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

/**
 * SGML Lexer splits document text string into lines and columns
 * delimited by tags.  
 *
 * <p> <b>Static function</b>
 *
 * <p> The `parse' function returns a two dimensional array.  The
 * first dimension is lines, the second we'll sometimes call "columns"
 * -- although each line can have an irregular number of columns (zero
 * or more second dimension elements per line).
 *
 * <p> This is less than an SGML tokenizer, but a pre- tokenizer
 * preserving lines, quoted attributes, and creating arrays within
 * lines for tags and non- tags.  
 *
 * <P> Each element of a line is checked for starting or ending with a
 * less- than (<tt>"<b>&lt;</b>"</tt>) or greater- than
 * (<tt>"<b>&gt;</b>"</tt>) character to see if it is a tag, or part
 * of a tag.  Tags can span multiple lines, so the opening less- than
 * (LT) character may not be closed by a closing greater- than (GT)
 * character till the next line.
 *
 * <p> The `parseValidate' function only returns a non- null result
 * when the source text contains a <tt>"<b>&lt; - &gt;</b>"</tt> valid
 * document possessing at least one <tt>"<b>&lt; - &gt;</b>"</tt> tag.
 *
 * @author John Pritchard (john@syntelos.org)
 */
public abstract class sgmlp {

    private final static String[][] add ( String[][] src, String[] element){
	if ( null == src)
	    src = new String[1][];
	else {
	    String[][] copier = new String[src.length+1][];
	    System.arraycopy(src,0,copier,0,src.length);
	    src = copier;
	}
	src[src.length-1] = element;
	return src;
    }

    /**
     * Parse as a template, returning null if there are no template
     * objects in the text.  
     * 
     * <p> "Server- side" parse validation goes into quotes.
     */
    public final static String[][] parseValidateServer ( String text){
	return parse(text,true,false);
    }
    /**
     * Parse as a template, returning null if there are no template
     * objects in the text.  
     * 
     * <p> "Client- side" parse validation ignores everything in quotes.
     */
    public final static String[][] parseValidateClient ( String text){
	return parse(text,true,true);
    }
    /**
     * Parse text string into lines and columns.  One column per line
     * unless there are "%...%" tokens in the line, in which case the
     * "%...%" tokens are separated.
     *
     * @param text Text string */
    public final static String[][] parse ( String text){
	return parse(text,false,true);
    }
    /**
     * Parse as a template, returning null if there are no template
     * objects in the text.  
     * 
     * <p> "Server- side" parse validation goes into quotes.
     */
    public final static String[][] parseValidateServer ( InputStream in){
	return parse(in,true,false);
    }
    /**
     * Parse as a template, returning null if there are no template
     * objects in the text.  
     * 
     * <p> "Client- side" parse validation ignores everything in quotes.
     */
    public final static String[][] parseValidateClient ( InputStream in){
	return parse(in,true,true);
    }
    /**
     * Parse text string into lines and columns.  One column per line
     * unless there are "%...%" tokens in the line, in which case the
     * "%...%" tokens are separated.
     *
     * @param text Text string */
    public final static String[][] parse ( InputStream in){
	return parse(in,false,true);
    }
    /**
     * 
     * @param text Source with CRLF or LF newlines
     *
     * @param validate If source doesn't contain <tt>&lt;sgml
     * tokens&gt;</tt>, return null.  
     * 
     * @param blindquotes If anything in quotes is blindly ignored. 
     */
    public final static String[][] parse ( String text, boolean validate, boolean blindquotes) {

	StringTokenizer strtok = new StringTokenizer(text,"\r\n",true);

	String[][] src = null;

	int c = 0, cc = 0;

	String s1;

	try {
	    /* parse input into lines */

	    String[] tmp;

	    while (true){

		s1 = strtok.nextToken();

		if ( 1 == s1.length()){

		    switch(s1.charAt(0)){

		    case '\r':

			break;

		    case '\n':
			cc += 1;
			if ( 2 <= cc)
			    src = add(src,null);

			break;

		    default:
			cc = 0;

			tmp = new String[1];
			tmp[0] = s1;
			src = add(src,tmp);

			break;
		    }
		}
		else {
		    cc = 0;

		    tmp = new String[1];
		    tmp[0] = s1;
		    src = add(src,tmp);
		}
	    }
	}
	catch ( NoSuchElementException nsx){}

	return _parse(src, validate, blindquotes);
    }

    /**
     * 
     * @param in Source
     *
     * @param validate If source doesn't contain <tt>&lt;sgml
     * tokens&gt;</tt>, return null.  
     * 
     * @param blindquotes If anything in quotes is blindly ignored. 
     */
    public final static String[][] parse ( InputStream in, boolean validate, boolean blindquotes) {

	String src[][] = null;

	try {
	    DataInputStream din;

	    if ( in instanceof DataInputStream)

		din = (DataInputStream)in;
	    else
		din = new DataInputStream (in);

	    String line, tmp[];

	    while ( null != (line = din.readLine())){

		if ( 0 == line.length())

		    src = add(src,null);

		else {
		    tmp = new String[1];
		    tmp[0] = line;
		    src = add(src,tmp);
		}
	    }
	    return _parse( src, validate, blindquotes);
	}
	catch ( IOException iox){

	    iox.printStackTrace();

	    return null;
	}
    }

    private final static String[][] _parse ( String[][] src, boolean validate, boolean blindquotes){

	if ( null == src) return null;

	// TODO (blindquotes)?(HTML-COMMENTS) [[for correct client side interp]]

	/*
	 * Parse lines for tags and tagged- data
	 */
	char line[], quot = 0;

	int llen, intag = 0, /* sgml_validity = 0, */ sgml_tagcount = 0, idx;

	String[] linary;

	for ( int c = 0, cc; c < src.length; c++){

	    linary = src[c];

	    if ( null != linary){

		line = linary[0].toCharArray();

		llen = line.length;

		for ( cc = 0; cc < llen; cc++){

		    switch(line[cc]){

		    case '<':

			if (blindquotes){

			    if ( 0 < quot)

				break;

			    else if (0 < intag)

				break; // '<' within '<'

			}

			// sgml_validity += 1;

			intag += 1;

			if ( 0 < cc)
			    src[c] = _parse_splitter(src[c],cc);

			break;

		    case '>':

			if ( blindquotes && 0 < quot)

			    break;

			else if (0 < intag){

			    //sgml_validity -= 1; 

			    sgml_tagcount += 1;

			    intag -= 1; // multiline tag

			    idx = cc+1;

			    if ( idx < llen)
				src[c] = _parse_splitter( src[c], idx);
			}
			break;

		    case '\'':
		    case '`':
		    case '"':

			if (blindquotes){

			    if (0 < intag){

				if ( 0 == quot)
				    quot = line[cc];
				else if ( quot == line[cc])
				    quot = 0;
			    }
			    else if ( 0 < quot && quot == line[cc])
				quot = 0;
			}
			break;

		    default:
			break;
		    }
		}
	    }
	}

	if (validate){

	    if ( 0 < sgml_tagcount) /* && 0 == sgml_validity //JS comparison "<>"  */

		return src;
	    else
		return null;
	}
	else
	    return src;
    }



    /**
     * This doesn't check for appending a zero- length substring
     */
    private final static String[] _parse_splitter( String[] line, int atidx){

	int llen = line.length;

	if ( 1 < llen){

	    for ( int clen = (llen-1), cc = 0; cc < clen; cc++)
		atidx -= line[cc].length();

	    if ( 0 < atidx){

		String s = line[llen-1];

		String s0 = s.substring(0,atidx);
		String s1 = s.substring(atidx);

		String[] copier = new String[llen+1];

		System.arraycopy( line, 0, copier, 0, llen);

		copier[llen-1] = s0;
		copier[llen]   = s1;

		return copier;
	    }
	    else
		return line;
	}
	else if ( 0 < atidx){

	    String s = line[0];

	    String s0 = s.substring(0,atidx);
	    String s1 = s.substring(atidx);

	    line = new String[2];

	    line[0] = s0;
	    line[1] = s1;

	    return line;
	}
	else
	    return line;
    }



    /**
     * Interned "="
     */
    public final static String EQ = "=".intern();

    /**
     * Split a tag into tokens according to tag syntax, preserving
     * quoted attribute values, stripping leading SGML tag "start" and
     * "end" ('&lt;', '&gt;') characters.  Does not require any
     * particular elements of a tag, does not require a start or end
     * character.  Stops at an SGML tag end character that is not
     * within a symmetrically quoted string.
     *
     * <p> Tag attributes are guaranteed as three tokens, as
     * available: name string, equals character string, value string.
     * This equals string is "interned" so that in processing the
     * result, each element can be compared by value to a similarly
     * interned string ("=".intern()) using the java equivalent value
     * ("==") operator.
     *
     * <p> Returned tokens are all trim: no leading or trailing
     * whitespace.
     * 
     * @param tag A whole or part of an SGML tag.  Ignores anything
     * before the tag open character ('&lt;'). */
    public final static String[] tag_tokenizer ( String tag){
	if ( null == tag) 
	    return null;
	else {
	    int len = tag.length();

	    if ( 0 >= len) return null;

	    linebuf buf = new linebuf();

	    char quot = 0, ch, cary[] = tag.toCharArray();

	    int mark = 0, tokl;

	    String tok;

	    for ( int c = 0; c < len; c++){

		ch = cary[c];

		switch(ch){

		case '<':

		    if ( 0 == quot)

			mark = c+1;

		    break;

		case '>':

		    if ( 0 == quot){

			tokl = c-mark;

			if ( 0 < tokl){

			    tok = new String(cary,mark,tokl);

			    buf.append(tok);
			}

			return buf.toStringArray();
		    }
		    else
			break;

		case '=':
		    if ( 0 == quot){

			tokl = c-mark;

			if ( 0 < tokl){

			    tok = new String(cary,mark,tokl);

			    buf.append(tok);
			}

			buf.append(EQ); // "=".intern()

			mark = c+1;
		    }
		    break;

		case '\'':
		case '`':
		case '"':

		    if ( 0 == quot){

			quot = ch;

			mark = c;
		    }
		    else if ( ch == quot){

			quot = 0;

			tokl = c-mark;

			if ( 0 < tokl){

			    tok = new String(cary,mark,tokl);

			    buf.append(tok);
			}
			mark = c+1;
		    }
		    break;

		case ' ':
		case '\t':

		    if ( 0 == quot){

			tokl = c-mark;

			if ( 0 < tokl){

			    tok = new String(cary,mark,tokl);

			    buf.append(tok);
			}
			mark = c+1;
		    }
		    break;

		default:
		    break;
		}
	    }

	    return buf.toStringArray();
	}
    }


    /**
     * Normalize an SGML tag attribute value, stripping symmetric
     * quotes, returning null for empty strings.  
     *
     * @param att_value Must not include leading or trailing
     * whitespace, or otherwise be a string other than a bare tag
     * attribute value.  */
    public final static String trim_value( String att_value){
	if ( null == att_value) 
	    return null;
	else {
	    int len = att_value.length();

	    if ( 0 >= len) return null;

	    char ch = att_value.charAt(0);

	    switch(ch){

	    case '\'':
	    case '`':
	    case '"':

		if ( ch == att_value.charAt(len-1))

		    return att_value.substring(1,len-2);

		else
		    return att_value;

	    default:
		return att_value;
	    }
	}
    }


    private final static String TEXT_PREFIX = "{\\{\\{";

    private final static String LINE_LEFT = "{\\{";

    private final static String LINE_RIGHT = "}/}";

    private final static String LINE_INFIX = "|,|";

    private final static String TEXT_SUFFIX = "}/}/}";

    private final static String[] helpary = {
	null,
	" Usage: sgmlp -f filename [ -s | -c ]",
	null,
	" Description",
	null,
	"\tDisplays parsed input file using tryglyph token delimiters.",
	null,
	"\tUses \""+TEXT_PREFIX+"\" before the text.",
	null,
	"\tUses \""+LINE_LEFT+"\" on the left hand side of a line.",
	null,
	"\tUses \""+LINE_INFIX+"\" among tokens within a line.",
	null,
	"\tUses \""+LINE_RIGHT+"\" on the right hand side of a line.",
	null,
	"\tUses \""+TEXT_SUFFIX+"\" after the text.",
	null,
	"\tIn this way, the tokenization of the text is readable.",
	null,
	" Options",
	null,
	"\t-s\t-- Use server side parsing model.",
	"\t  \t   Enter quoted content.",
	null,
	"\t-c\t-- Use client side parsing model (default).",
	"\t  \t   Ignore quoted content.",
	null,
    };

    private final static String help = new linebuf(helpary).toString();

    public static void main( String[] argv){
	try {
	    File inf = null;

	    boolean validate_server = false, validate_client = false;

	    if ( null == argv || 1 > argv.length)
		throw new IllegalArgumentException(help);
	    else {
		int alen = argv.length;
		String arg; 

		for ( int argc = 0; argc < alen; argc++){
		    arg = argv[argc];

		    if ( 2 > arg.length())
			throw new IllegalArgumentException("Unrecognized argument `"+arg+"'");
		    else {
			switch(arg.charAt(0)){
			case '-':
			case '/':
			    switch(arg.charAt(1)){
			    case 'h':
			    case 'H':
			    case '?':
				throw new IllegalArgumentException(help);

			    case 'f':
				argc += 1;
				if ( argc < alen){

				    arg = argv[argc];

				    inf = new File(arg);
				}
				else
				    throw new IllegalArgumentException("Argument `-f' requires filename.");
				break;

			    case 's':
				validate_server = (!validate_server);
				break;

			    case 'c':
				validate_client = (!validate_client);
				break;

			    default:
				throw new IllegalArgumentException("Unrecognized argument `"+arg+"'");
			    }
			    break;
			default:
			    throw new IllegalArgumentException("Unrecognized argument `"+arg+"'");
			}
		    }
		}
	    }

	    if ( null == inf)
		throw new IllegalArgumentException(help);
	    else if (! (inf.exists() && inf.canRead()))
		throw new IllegalArgumentException("Can't read file `"+inf+"'");

	    InputStream in = new FileInputStream(inf);

	    try {
		String[][] pars = null;

		if (validate_server)
		    pars = parseValidateServer(in);
		else if (validate_client)
		    pars = parseValidateClient(in);
		else
		    pars = parse(in);

		if ( null != pars){
		    System.out.println(TEXT_PREFIX);

		    for ( int c = 0, cc; c < pars.length; c++){

			System.out.print("\t"+LINE_LEFT);

			for ( cc = 0; null != pars[c] && cc < pars[c].length; cc++){

			    if ( null != pars[c][cc])
				System.out.print(pars[c][cc]);

			    if ( cc < pars[c].length-1)
				System.out.print(LINE_INFIX);
			}
			System.out.println(LINE_RIGHT);
		    }

		    System.out.println(TEXT_SUFFIX);
		}
		else
		    System.out.println("Not SGML.");
	    }
	    finally {
		in.close();
	    }
	}
	catch ( IllegalArgumentException ilx){
	    System.err.println(ilx.getMessage());
	}
	catch ( Exception exc){
	    exc.printStackTrace();
	}
    }

}
