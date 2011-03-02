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
import java.io.IOException;
import java.util.Enumeration;
import java.util.NoSuchElementException;
import java.util.StringTokenizer;

/**
 * <p> Line oriented string buffer, or dynamic string array container.
 * Plus string utility methods.  For an array of strings this is
 * faster than a Vector which is synchronized and generalized (not
 * String).  
 *
 * <p> This uses character arrays internally because more intensive
 * use of the line buffer transforms Strings to character arrays and
 * back into String objects and then back into character arrays.  And
 * in the very common <code>`toString()'</code> case a String will be
 * concatenated as character arrays, too.
 * 
 * @author John Pritchard (john@syntelos.org)
 */
public class linebuf {

    public final static char[] default_line_separator = {'\r','\n'};


    protected char[][] buf ;

    protected int p = 0;

    private int f ;

    private char[] output_line_sep = default_line_separator;

    private char[] output_prefix = null;

    /**
     * @param growthfactor Set internal buffer allocation unit length
     * (number of lines). */
    public linebuf ( int growthfactor){
	super();

	if ( 0 < growthfactor){

	    this.f = growthfactor;

	    buf = new char[f][];
	}
	else
	    throw new IllegalArgumentException("Growth factor must be greater than zero.");
    }

    /**
     * Default growth factor of ten. */
    public linebuf(){
	this(10);
    }

    /**
     * Append argument line.
     *
     * @param line One line to append.
     */
    public linebuf ( String line){
	this(10);

	append(line);
    }

    public linebuf ( Object obj){
	this(10);

	append(obj);
    }
    
    /**
     * Append argument lines.
     *
     * @param lineset Lines to append.
     */
    public linebuf ( String[] lineset){
	this((null != lineset && 0 < lineset.length)?(lineset.length):(10));

	append(lineset);
    }

    /**
     * Append argument lines.
     *
     * @param lineset Lines to append.
     *
     * @param line_sep Linebuf string element (line) separator.  */
    public linebuf ( String[] lineset, String line_sep){
	this(null,lineset,line_sep);
    }

    /**
     * Append argument lines.
     *
     * @param prefix Output prefix.
     *
     * @param lineset Lines to append using
     * <code>Object.toString().</code>
     *
     * @param line_sep Linebuf string element (line) separator.  */
    public linebuf ( String prefix, Object[] lineset, String line_sep){
	this((null != lineset && 0 < lineset.length)?(lineset.length):(10));

	if ( null != lineset){
	    Object ob;

	    int many = lineset.length;

	    for ( int lc = 0; lc < many; lc++){

		ob = lineset[lc];

		if ( null != ob)
		    append(ob);
		else
		    println();
	    }
	}

	if ( null != line_sep)
	    this.output_line_sep = line_sep.toCharArray();

	if ( null != prefix)
	    this.output_prefix = prefix.toCharArray();
    }
    /**
     * Append argument lines.
     *
     * @param prefix Output prefix.
     *
     * @param lineset Lines to append.
     *
     * @param line_sep Linebuf string element (line) separator.  
     */
    public linebuf ( String prefix, String[] lineset, String line_sep){
	this((null != lineset && 0 < lineset.length)?(lineset.length):(10));

	append(lineset);

	if ( null != line_sep)
	    this.output_line_sep = line_sep.toCharArray();

	if ( null != prefix)
	    this.output_prefix = prefix.toCharArray();
    }

    /**
     * Append argument lines.
     *
     * @param prefix Output prefix.
     *
     * @param lineset Lines to append using
     * <code>Object.toString().</code>
     *
     * @param line_sep Linebuf string element (line) separator.  */
    public linebuf ( String prefix, Enumeration lineset, String line_sep){
	this(10);

	if ( null != lineset){

	    Object ob;

	    while (lineset.hasMoreElements()){

		ob = lineset.nextElement();

		if ( null != ob)
		    append(ob);
		else
		    println();
	    }
	}

	if ( null != line_sep)
	    this.output_line_sep = line_sep.toCharArray();

	if ( null != prefix)
	    this.output_prefix = prefix.toCharArray();
    }

    /**
     * @returns Index of last line used, or zero.
     */
    public int index(){
	if ( 0 < p)
	    return p-1;
	else
	    return 0;
    }

    /**
     * @param idx Line index in this buffer.
     */
    public String index ( int idx){
	if ( idx >= 0 && idx < buf.length)
	    return new String(buf[idx]);
	else
	    return null;
    }

    /**
     * @param idx Line index in this buffer.
     */
    public char[] index_cary ( int idx){
	if ( idx >= 0 && idx < buf.length)
	    return buf[idx];
	else
	    return null;
    }

    /**
     * @param idx Line index in this buffer.
     *
     * @param val Line text
     */
    public void index ( int idx, String val){
	if ( idx >= 0 ){

	    if ( idx < buf.length)

		buf[idx] = detab(val);

	    else {

		while ( idx >= buf.length){

		    char[][] copier = new char[buf.length+f][];

		    System.arraycopy(buf,0,copier,0,buf.length);

		    buf = copier;
		}

		buf[idx] = detab(val);
	    }
	    
	    if ( idx >= p) p = idx+1;
	    
	}
	else
	    throw new ArrayIndexOutOfBoundsException("Index `"+idx+"' is invalid.");
    }

    /**
     * @param idx Line index in this buffer.
     *
     * @param val Line text
     */
    public void index ( int idx, char[] val){
	if ( idx >= 0 ){

	    if ( idx < buf.length)

		buf[idx] = val;

	    else {

		while ( idx >= buf.length){

		    char[][] copier = new char[buf.length+f][];

		    System.arraycopy(buf,0,copier,0,buf.length);

		    buf = copier;
		}

		buf[idx] = val;
	    }
	    
	    if ( idx >= p) p = idx+1;
	    
	}
	else
	    throw new ArrayIndexOutOfBoundsException("Index `"+idx+"' is invalid.");
    }

    /**
     * @param idx Line index in this buffer.
     *
     * @param val Line text to prepend to any current text in line
     */
    public void index_prepend ( int idx, String val){
	if ( idx >= 0 ){

	    while ( idx >= buf.length){

		char[][] copier = new char[buf.length+f][];

		System.arraycopy(buf,0,copier,0,buf.length);

		buf = copier;
	    }

	    if ( null != buf[idx])

		buf[idx] = chbuf.cat(detab(val),buf[idx]);

	    else
		buf[idx] = detab(val);

	    if ( idx >= p) p = idx+1;

	}
	else
	    throw new ArrayIndexOutOfBoundsException("Index `"+idx+"' is invalid.");
    }

    /**
     * @param idx Line index in this buffer.
     *
     * @param val Line text
     */
    public void index_append ( int idx, String val){
	index_append(idx,detab(val));
    }
    /**
     * @param idx Line index in this buffer.
     *
     * @param val Line text
     */
    public void index_append ( int idx, char[] val){
	if ( idx >= 0){

	    while ( idx >= buf.length){

		char[][] copier = new char[buf.length+f][];

		System.arraycopy(buf,0,copier,0,buf.length);

		buf = copier;
	    }

	    if ( null != buf[idx])

		buf[idx] = chbuf.cat(buf[idx],val);

	    else 
		buf[idx] = val;

	    if ( idx >= p) p = idx+1;

	}
	else
	    throw new ArrayIndexOutOfBoundsException("Index `"+idx+"' is invalid.");
    }
    /**
     * <p> Not as fast as chbuf for many chars.</p>
     * @param idx Line index in this buffer.
     * @param ch Line character
     */
    public void index_append ( int idx, char ch){
	if ( idx >= 0){

	    while ( idx >= buf.length){

		char[][] copier = new char[buf.length+f][];

		System.arraycopy(buf,0,copier,0,buf.length);

		buf = copier;
	    }

	    if ( null != buf[idx]){
		char[] bul = buf[idx];
		int blen = bul.length;
		char[] copier = new char[blen+1];
		System.arraycopy(bul,0,copier,0,blen);
		copier[blen] = ch;
		buf[idx] = copier;
	    }
	    else 
		buf[idx] = new char[]{ch};

	    if ( idx >= p) p = idx+1;

	}
	else
	    throw new ArrayIndexOutOfBoundsException("Index `"+idx+"' is invalid.");
    }

    public void line_append ( String s){

	index_append( p-1, s.toCharArray());
    }

    /**
     * Use the first <code>`colw'</code> characters of
     * <code>`col'</code> to overwrite the <code>"[col0,colw)"</code>
     * segment of the line.
     *
     * @param idx Line index in this buffer.
     *
     * @param col Column text
     *
     * @param col0 Column specification first column index
     *
     * @param colw Column specification width
     * */
    public void index_column_overwrite ( int idx, String col, int col0, int colw){
	index_column_overwrite(idx,detab(col),col0,colw);
    }

    /**
     * Use the first <code>`colw'</code> characters of
     * <code>`col'</code> to overwrite the <code>"[col0,colw)"</code>
     * segment of the line.
     *
     * @param idx Line index in this buffer.
     *
     * @param col Column text
     *
     * @param col0 Column specification first column index
     *
     * @param colw Column specification width
     * */
    public void index_column_overwrite ( int idx, char[] col, int col0, int colw){

	if ( null == col) 

	    return;

	else {
	    // Setup destination

	    char[] tcary = index_cary(idx);

	    int linlen = col0+1+colw;

	    if ( null != tcary){

		if ( tcary.length < linlen){

		    char[] copier = new char[linlen];

		    for ( int cc = tcary.length; cc < linlen; cc++)
			copier [cc] = ' ';

		    System.arraycopy(tcary,0,copier,0,tcary.length);

		    tcary = copier;
		}
	    }
	    else {
		tcary = new char[linlen];

		for ( int cc = 0; cc < col0; cc++)
		    tcary [cc] = ' ';
	    }

	    // Write column from `col'

	    if ( col.length < colw)
		colw = col.length;

	    System.arraycopy(col,0,tcary,col0,colw);

	    index( idx, tcary);

	    return;
	}
    }

    /**
     * Convenience method to prepend string value to elements
     * currently in the line buffer.  Has no effect on elements
     * subsequently appended to line buffer, or any other future
     * operations. 
     * 
     * <p> Makes null elements non- null!
     *
     * @param val String value to prepend to existing elements of the
     * buffer.  */
    public linebuf lines_prepend ( String val){

	char[] valc = detab(val);

	for ( int cc = 0; cc < p; cc++){

	    if ( null != buf[cc])

		buf[cc] = chbuf.cat(valc,buf[cc]);
	    else
		buf[cc] = chbuf.copy(valc);
	}
	return this;
    }

    /**
     * Convenience method to append string value to elements
     * currently in the line buffer.  Has no effect on elements
     * subsequently appended to line buffer, or any other future
     * operations. 
     * 
     * <p> Makes null elements non- null!
     *
     * @param val String value to append to existing elements of the
     * buffer.  */
    public linebuf lines_append ( String val){

	char[] valc = detab(val);

	for ( int cc = 0; cc < p; cc++){

	    if ( null != buf[cc])

		buf[cc] = chbuf.cat(buf[cc],valc);
	    else
		buf[cc] = chbuf.copy(valc);
	}
	return this;
    }

    /**
     * Append null line, return this. */
    public linebuf println (){

	final String line = null;

	return append( line);
    }

    /**
     * Append line, return this.
     *
     * <p> Will insert null lines. */
    public linebuf append ( char[] line){

	if ( p >= buf.length){

	    char[][] copier = new char[buf.length+f][];

	    System.arraycopy(buf,0,copier,0,buf.length);

	    buf = copier;
	}
	buf[p++] = detab(line);

	return this;
    }

    /**
     * Append line, return this.
     *
     * <p> Will insert null lines. 
     */
    public linebuf append ( char[] line, int ofs, int len){

	if ( p >= buf.length){

	    char[][] copier = new char[buf.length+f][];

	    System.arraycopy(buf,0,copier,0,buf.length);

	    buf = copier;
	}
	buf[p++] = detab(line,ofs,len);

	return this;
    }

    /**
     * Append line, return this.
     *
     * <p> Will insert null lines. */
    public linebuf append ( String line){

	if ( p >= buf.length){

	    char[][] copier = new char[buf.length+f][];

	    System.arraycopy(buf,0,copier,0,buf.length);

	    buf = copier;
	}

	buf[p++] = detab(line);

	return this;
    }

    /**
     * Will insert null line elements. */
    public linebuf append ( String[] lines){

	if ( null == lines || 0 >= lines.length)
	    return this;
	else {
	    int len = lines.length;

	    for ( int cc = 0; cc < len; cc++){
		append(lines[cc]);
	    }

	    return this;
	}
    }

    private final static String nil = null;

    /**
     * Will insert null line elements. */
    public linebuf append ( Object[] oary){

	if ( null == oary || 0 >= oary.length)

	    return this;

	else {

	    int len = oary.length;

	    for ( int cc = 0; cc < len; cc++){

		append( oary[cc]);
	    }

	    return this;
	}
    }

    public linebuf append ( Object o){

	if ( null == o){

	    append( nil);

	    return this;
	}
	else if ( o instanceof String){

	    append( (String)o);

	    return this;
	}
	else {
	    append(new chbuf(o).toString());

	    return this;
	}
    }

    /**
     * Split input string `s' on delimiter characters, and add each
     * resulting token as an element- line. */
    public linebuf append ( String s, String delimiters){

	StringTokenizer strtok = new StringTokenizer(s,delimiters);

	while(strtok.hasMoreTokens())
	    append(strtok.nextToken());

	return this;
    }

    /**
     * Returns lines or null. */
    public String[] toStringArray(){

	if ( 0 < p){

	    String[] ret = new String[p];

	    char[] line;

	    for ( int cc = 0; cc < p; cc++){

		line = buf[cc];

		if ( null != line)
		    ret[cc] = new String(line);
	    }

	    return ret;
	}
	else
	    return null;
    }

    /**
     * Returns lines or null. */
    public char[][] toCharCharArray(){

	if ( 0 < p){
	    int len = p;

	    char[][] bbuf = this.buf;

	    char[][] copier = new char[len][];

	    System.arraycopy(bbuf,0,copier,0,len);

	    return copier;
	}
	else
	    return null;
    }

    /**
     * Reset the line separator used by `toString'.  By default it is
     * the local platform line separator, typically CRLF for MacOs or
     * Windows, and LF for Unix, Linux, Be, Inferno, Plan9, NeXt or
     * MacOsX. */
    public linebuf line_separator( String sep){
	if ( null != sep)
	    this.output_line_sep = sep.toCharArray();
	return this;
    }

    public String line_separator(){

	if ( null != this.output_line_sep)

	    return new String(this.output_line_sep);
	else
	    return null;
    }

    /**
     * Invert (reverse) the order of strings in the linebuffer.
     *
     * <p> <b>Reminder: Not MT SAFE,</b> although MT robust.  Works on
     * a copy of the internal buffer, then replaces the internal
     * buffer with the inverted one.  Changes made to the internal
     * buffer by another thread during this operation will be lost. */
    public linebuf invert(){


	if ( 0 >= p) 

	    return this;

	else {
	    int len = p;

	    char[][] copier = new char[len][];

	    System.arraycopy(buf,0,copier,0,p);

	    char[] s;

	    for ( int top = (len-1), bot = 0; bot < len; bot++, top--){

		if ( bot >= top)
		    break;
		else {
		    s = copier[bot];

		    copier[bot] = copier[top];

		    copier[top] = s;
		}
	    }

	    buf = copier;

	    return this;
	}
    }

    /**
     * Terminate each line with the line separator string, by default
     * this is the local platform newline (eg, CRLF (Mac/ Win) or LF
     * (Unix)). */
    public String toString(){

	char[] cary = toCharArray();

	if ( null == cary)
	    return null;
	else
	    return new String(cary);
    }

    /**
     * Terminate each line with the line separator string, by default
     * this is the local platform newline (eg, CRLF (Mac/ Win) or LF
     * (Unix)). */
    public char[] toCharArray(){

	if ( 0 < p){

	    char[][] bbuf = buf;

	    int len = p;

	    chbuf strbuf = new chbuf(len*50);

	    if ( null != output_prefix)
		strbuf.append(output_prefix);

	    for ( int cc = 0; cc < len; cc++){

		if ( 0 < cc) 
		    strbuf.append(output_line_sep);

		strbuf.append(buf[cc]);
	    }

	    return strbuf.toCary();
	}
	else 
	    return null;
    }

    /**
     * Discard content, reuse buffer. */
    public linebuf reset(){

	for ( int cc = 0; cc < p; cc++){
	    buf[cc] = null;
	}

	p = 0;

	return this;
    }

    /**
     * Number of lines. */
    public int length(){
	return p;
    }

    public void backspace( int rowidx, int colidx){
	
	char[] line = index_cary(rowidx);

	if ( null != line){

	    int linlen = line.length-1;

	    if ( 0 < linlen){

		char[] copier = new char[linlen];

		System.arraycopy(line, 0, copier, 0, linlen);

		colidx += 1;

		if ( colidx < linlen)
		    System.arraycopy(line, colidx, copier, colidx, (linlen-colidx));

		index( rowidx, copier);
	    }
	    else {

		index( rowidx, (char[])null);
	    }
	}
    }

    /**
     * Delete the first line
     */
    public void pop (){
	if ( 0 < p){
	    System.arraycopy( buf, 1, buf, 0, p-1);
	    p -= 1;
	}
    }

    public void delete ( int rowidx, int colidx){

	char[] line = index_cary(rowidx);

	if ( null != line){

	    int linlen = line.length-1;

	    if ( 0 < linlen){

		char[] copier = new char[linlen];

		System.arraycopy(line, 0, copier, 0, linlen);

		colidx += 2;

		if ( colidx < linlen)
		    System.arraycopy(line, colidx, copier, colidx, (linlen-colidx));

		index(rowidx,copier);
	    }
	    else {

		index(rowidx, (char[])null);
	    }
	}
    }

    public void insert ( char key, int rowidx, int colidx){

	char[] line = index_cary(rowidx);

	if ( null != line){

	    int linlen = line.length;

	    if (colidx < linlen){
		
		line[colidx] = key;
	    }
	    else {
		if ( colidx > linlen){

		    char[] copier = new char[linlen+1];

		    System.arraycopy(line,0,copier,0,linlen);

		    line = copier;

		    line[linlen] = key;
		}
		else {
		    
		    char[] copier = new char[linlen+1];

		    System.arraycopy(line,0,copier,0,linlen);

		    line = copier;

		    line[colidx] = key;
		}

		index(rowidx,line);
	    }
	}
	else {
	    line = new char[1];

	    line[0] = key;

	    index(rowidx,line);
	}
    }


    public final static String toString ( Object[] oary){
	if ( null == oary)
	    return null;
	else if ( 1 == oary.length){

	    Object obj = oary[0];

	    if ( obj instanceof String)

		return (String)obj;

	    else
		return new linebuf(obj).toString();
	}
	else 
	    return new linebuf(null,oary,null).toString();
    }

    public final static String toString ( Object obj){

	if ( obj instanceof String)

	    return (String)obj;
	else
	    return new linebuf(obj).toString();
    }

    public final static String[] toStringArray ( Object o){
	if ( o instanceof String){

	    String[] ret = {(String)o};

	    return ret;
	}
	else {
	    linebuf lb = new linebuf();
	    lb.append(o);
	    return lb.toStringArray();
	}
    }

    public final static String[] toStringArray ( DataInputStream din) throws IOException {

	linebuf lb = new linebuf();

	String line;

	while (null != (line = din.readLine())){
	    lb.append(line);
	}

	return lb.toStringArray();

    }

    public final static String[] toStringArray ( Enumeration en){
	linebuf lb = toStringArray(en,null);

	if ( null == lb)
	    return null;
	else
	    return lb.toStringArray();
    }

    public final static linebuf toStringArray ( Enumeration en, linebuf lb){

	if ( null == en)
	    return lb;

	if ( null == lb)
	    lb = new linebuf();

	try {
	    String str;

	    while ( en.hasMoreElements()){

		str = en.nextElement().toString();

		lb.append(str);
	    }
	} catch ( NoSuchElementException nsx){
	    nsx.printStackTrace();
	}
	return lb;
    }

    /**
     * Parses input using default delimiters of carriage return and
     * newline (CR and LF). */
    public final static String[] toStringArray ( String s){
	linebuf lb = toStringArray(s,null,"\r\n");

	return lb.toStringArray();
    }

    public final static String[] toStringArray ( String s, String delimiters){

	if ( null == s)
	    return null;
	else {
	    linebuf lb = toStringArray(s,null,delimiters);

	    return lb.toStringArray();
	}
    }

    /**
     * Uses default delimiters of carriage return and newline (CR and
     * LF). */
    public final static linebuf toStringArray ( String s, linebuf lb){
	return toStringArray(s,lb,"\r\n");
    }

    public final static linebuf toStringArray ( String s, linebuf lb, String delimiters){

	if ( null == s)
	    return lb;
	else if ( null == lb)
	    lb = new linebuf();

	if ( null == delimiters){
	    lb.append(s);
	    return lb;
	}
	else {

	    StringTokenizer strtok = new StringTokenizer(s,delimiters);

	    while(strtok.hasMoreTokens()){
		lb.append(strtok.nextToken());
	    }
	    return lb;
	}
    }

    public final static String[] toStringArray( String s, char sep){
	if ( null == s)
	    return null;
	else {
	    char[] cary = s.toCharArray();

	    String[] lb = null;  int lbx = 0;

	    for ( int mark = 0, len = cary.length, cc = 0; cc < len; cc++){

		if ( sep == cary[cc]){

		    if ( 0 < (cc-mark)){
			
			//lb.append( cary, mark, cc)
			if ( null == lb)
			    lb = new String[1];
			else {
			    lbx = lb.length;
			    String[] copier = new String[lbx+1];
			    System.arraycopy(lb,0,copier,0,lbx);
			}
			lb[lbx] = new String(cary,mark,cc);
		    }
		    mark = cc+1;
		}
	    }
	    return lb;
	}
    }


    /**
     * Use the `term' argument literally, not as a set of delimiter
     * characters but as a whole term delimiting substrings in `s'.
     *
     * @param s String source
     *
     * @param term Substring delimiter
     */
    public final static String[] toStringArray_term ( String s, String term){

	if ( null == s)
	    return null;
	else {
	    linebuf lb = new linebuf();

	    char[] src_cary = s.toCharArray(), term_cary = term.toCharArray();

	    int idx0 = 0, idx1 = chbuf.indexOf(src_cary,term_cary);

	    if ( 0 < idx1){

		  int next_term = term_cary.length;

		while (true){

		    lb.append(chbuf.substring(src_cary,idx0,idx1));

		    idx1 += next_term;

		    idx0 = idx1;

		    idx1 = chbuf.indexOf(src_cary,term_cary,idx0);

		    if ( 0 < idx1)

			continue;

		    else {

			idx1 = src_cary.length;

			if ( idx0 < idx1)
			    lb.append(chbuf.substring(src_cary,idx0,idx1));

			break;
		    }
		}
	    }
	    else
		lb.append(s);

	    return lb.toStringArray();
	}
    }

    public final static String[] invert ( String s, String delim){
	linebuf lb = invert(s,null,delim);

	return lb.toStringArray();
    }
    public final static linebuf invert ( String s, linebuf lb, String delimiters){

	if ( null == s)
	    return lb;

	if ( null == lb)
	    lb = new linebuf();

	StringTokenizer strtok = new StringTokenizer(s,delimiters);

	while(strtok.hasMoreTokens()){
	    lb.append(strtok.nextToken());
	}
	return lb.invert();
    }

    /**
     * Insert 'lvl' tabs before each line of a multiline text, else
     * return text (use leveltabs for a single line).  */
    public final static String indent ( int lvl, String text){

	linebuf lb = toStringArray(text,null,"\r\n");

	String tabs = null;

	if ( 0 < lvl){

	    char[] tabsary = new char[lvl];

	    for ( int c = 0; c < lvl; c++)
		tabsary[c] = '\t';

	    tabs = new String(tabsary);
	}

	lb.lines_prepend(tabs);

	return lb.toString();
    }

    /**
     * Parse series of space separated, optionally quoted strings. */
    public final static String[] space_quoted ( String line){
	if ( null == line) 
	    return null;

	line = line.trim();

	linebuf lb = new linebuf();

	chbuf strbuf = new chbuf();

	StringTokenizer strtok = new StringTokenizer( line, " \t`'\"", true);

	String sok;  char cok = 0, lc;  char quote = (char)0;

	loop: while(strtok.hasMoreTokens()){

	    sok = strtok.nextToken();

	    if ( 1 == sok.length()){

		lc = cok;

		cok = sok.charAt(0);

		switch(cok){

		case ' ':
		case '\t':
		    if ( 0 < quote)
			strbuf.append(cok);

		    continue loop;

		case '"': 
		case '`':
		case '\'':

		    if ( '\\' == lc){ // backslash- quoted quote

			strbuf.popChar(); // pop backslash

			strbuf.append(cok); // push quote
		    }
		    else if ( quote == cok){

			lb.append(strbuf.toString());

			strbuf.reset();

			quote = (char)0;
		    }
		    else if ( 0 == quote)
			quote = cok;
		    else 
			strbuf.append(cok);

		    continue loop;

		default:
		    break;
		}

		if ( 0 < quote)

		    strbuf.append(sok);

		else {

		    strbuf.append(sok);

		    lb.append(strbuf.toString());

		    strbuf.reset();
		}

	    }
	    else if ( 0 < quote){
		strbuf.append(sok);
	    }
	    else {
		strbuf.append(sok);

		lb.append(strbuf.toString());

		strbuf.reset();
	    }
	}

	return lb.toStringArray();
    }

    /**
     * Convert multiline object descriptor onto a single line.
     */
    public final static String toStringline ( Object o){

	if ( null == o)

	    return null;

	else if ( o instanceof String){
	    
	    linebuf lb = toStringArray( (String)o, null, "\t\r\n");

	    lb.line_separator("; ");

	    return lb.toString();
	}
	else {

	    linebuf lb = new linebuf();

	    lb.line_separator("; ");

	    lb.append(o);

	    return lb.toString();
	}
    }

    /**
     * Convert each tab to five spaces.
     *
     * @param s String to detabify
     */
    public final static char[] detab ( String s){

	return detab( (char[])((null != s)?(s.toCharArray()):(null)));
    }
    public final static char[] detab ( char[] scary){

	if ( null == scary)

	    return null;
	else
	    return detab(scary,0,scary.length);
    }

    private final static char[] tabs = {' ',' ',' ',' ',' '};

    public final static char[] detab ( char[] scary, int ofs, int len){

	if ( null == scary)

	    return null;

	else {

	    chbuf cb = new chbuf();

	    char ch;

	    for ( int cc = ofs; cc < len; cc++){

		ch = scary[cc];

		if ( '\t' == ch)
			
		    cb.append(tabs);
		else
		    cb.append(ch);
	    }

	    return cb.toCary();
	}
    }

    /**
     * Convert each tab to a space.  Used by
     * <code>`index_column_overwrite'</code> which in the context of
     * column formatting with spaces, truncates any tabs for "belt
     * &amp; braces" safety.
     *
     * @param s String to detabify 
     *
     * @see #index_column_overwrite
     */
    public final static char[] sptab ( String s){
	if ( null == s)
	    return null;
	else {
	    char[] scary = s.toCharArray();

	    int slen = scary.length;

	    boolean mod = false;

	    for ( int cc = 0; cc < slen; cc++){

		if ( '\t' == scary[cc]){
		    mod = true;

		    scary[cc] = ' ';
		}
	    }

	    return scary;
	}
    }

}
