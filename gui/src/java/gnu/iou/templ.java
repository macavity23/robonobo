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

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

/**
 * Parse input `String' text into lines, and then columns split by
 * "%name%" tokens.
 *
 * <p> For example, one line containing one special "%name%" token
 * would be parsed into three columns.
 * <pre>
 * {"The ", "%quick%", " brown fox is famous."}
 * </pre>
 * 
 * <p> Instance object for generating output into during parsing of
 * "src".  
 * 
 * @author John Pritchard (john@syntelos.org)
 */
public abstract class templ {


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
     * objects in the text.  */
    public final static String[][] parseValidate ( String text){
	return parse(text,true);
    }
    /**
     * Parse text string into lines and columns.  One column per line
     * unless there are "%...%" tokens in the line, in which case the
     * "%...%" tokens are separated.
     *
     * @param text Text string */
    public final static String[][] parse ( String text){
	return parse(text,false);
    }
    /**
     * @param text Template text
     * @param validate If true, return null if there are no template tokens.
     */
    public final static String[][] parse ( String text, boolean validate){

	if ( null == text) return null;

	StringTokenizer strtok = new StringTokenizer(text,"\r\n",true);

	String[][] src = null;

	int c = 0, cc = 0;

	String s1, s2, s3;

	boolean validate_havetok = false;

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

	/* parse lines for %template directives% */

	char[] line;

	int mark, markPC1, markPC2;

	String[] reline = null, copier;



	for ( c = 0; c < src.length; c++){

	    if ( null != src[c]){

		reline = null;

		line = src[c][0].toCharArray();

		mark = 0;

		for ( cc = 0; cc < line.length; cc++){

		    switch(line[cc]){

		    case '%':

			markPC1 = cc++;

			markPC2 = -1;

			s1 = null;
			s2 = null;
			s3 = null;

			fl2: for ( ; cc < line.length; cc++){

			    switch(line[cc]){

			    case '%':
				markPC2 = cc;
				break fl2;

			    default:
				break;
			    }
			}
			if ( markPC2 > markPC1){

			    validate_havetok = true;

			    /* slice */

			    if ( 0 < markPC1){

				s1 = new String(line,mark,markPC1-mark);

				s2 = new String(line,markPC1,markPC2-markPC1+1);

				mark = cc +1; // cc = markPC2

				if ( 0 < (line.length-mark))
				    s3 = new String(line,mark,line.length-mark);

			    }
			    else if ( 0 < (line.length-(cc+1))){

				s1 = new String(line,markPC1,markPC2-markPC1+1);

				mark = cc +1; // cc = markPC2

				s2 = new String(line,mark,line.length-mark);

			    }

			    /* reline */

			    if ( null != s3){

				if ( null == reline)
				    reline = new String[3];
				else {
				    copier = new String[reline.length+2];
				    System.arraycopy(reline,0,copier,0,reline.length);
				    reline = copier;
				}

				reline[reline.length-3] = s1;

				reline[reline.length-2] = s2;

				reline[reline.length-1] = s3;

				src[c] = reline;
			    }
			    else if ( null != s2){

				if ( null == reline)
				    reline = new String[2];
				else {
				    copier = new String[reline.length+1];
				    System.arraycopy(reline,0,copier,0,reline.length);
				    reline = copier;
				}

				reline[reline.length-2] = s1;

				reline[reline.length-1] = s2;

				src[c] = reline;
			    }

			}
			break;

		    default:
			break;
		    }
		}
	    }
	}

	if (validate){

	    if (validate_havetok)

		return src;
	    else
		return null;
	}
	else
	    return src;
    }

    private final static String TEXT_PREFIX = "{\\{\\{";

    private final static String LINE_LEFT = "{\\{";

    private final static String LINE_RIGHT = "}/}";

    private final static String LINE_INFIX = "|,|";

    private final static String TEXT_SUFFIX = "}/}/}";

    private final static String[] helpary = {
	null,
	" Usage: templ filename",
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
	"\tIn this way, the tokenization of the text is clearly readable.",
	null
    };

    private final static String help = new linebuf(helpary).toString();

    public static void main( String[] argv){
	try {
	    if ( null == argv || 1 != argv.length)
		throw new IllegalArgumentException(help);

	    File inf = new File(argv[0]);

	    if (! (inf.exists() && inf.canRead()))
		throw new IllegalArgumentException("Can't read file `"+inf+"'");

	    byte[] inbuf = new byte[ (int)inf.length()];

	    InputStream in = new FileInputStream(inf);

	    in.read(inbuf);

	    String[][] pars = parse(new String(inbuf));

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
	catch ( IllegalArgumentException ilx){
	    System.err.println(ilx.getMessage());
	}
	catch ( Exception exc){
	    exc.printStackTrace();
	}
    }

}
