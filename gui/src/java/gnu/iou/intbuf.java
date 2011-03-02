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
 * <p> 
 * 
 * @author John Pritchard (jdp@syntelos)
 */
public class intbuf {

    protected int[] buf ;

    protected int p = 0;

    private int f ;

    private char[] output_line_sep = linebuf.default_line_separator;

    private char[] output_prefix = null;

    public intbuf ( int growthfactor){
	super();

	if ( 0 < growthfactor){

	    this.f = growthfactor;

	    this.buf = new int[f];
	}
	else
	    throw new IllegalArgumentException("Growth factor must be greater than zero.");
    }
    public intbuf(){
	this(10);
    }
    public intbuf ( Object obj){
	this(10);
	append(obj);
    }
    public intbuf ( int[] set){
	this((null != set && 0 < set.length)?(set.length):(10));
	append(set);
    }
    public intbuf ( int[] lineset, String line_sep){
	this(null,lineset,line_sep);
    }
    public intbuf ( String prefix, int[] set, String line_sep){
	this((null != set && 0 < set.length)?(set.length):(10));

	if ( null != set){
	    int ob;

	    int many = set.length;

	    for ( int lc = 0; lc < many; lc++){

		ob = set[lc];

		this.append(ob);
	    }
	}

	if ( null != line_sep)
	    this.output_line_sep = line_sep.toCharArray();

	if ( null != prefix)
	    this.output_prefix = prefix.toCharArray();
    }

    public int index(){
	if ( 0 < p)
	    return p-1;
	else
	    return 0;
    }
    public int index ( int idx){
	if ( idx >= 0 && idx < buf.length)
	    return this.buf[idx];
	else
	    throw new ArrayIndexOutOfBoundsException("Index `"+idx+"' is invalid.");
    }
    public void index ( int idx, int val){
	if ( idx >= 0 && idx < buf.length)
	    buf[idx] = val;
	else
	    throw new ArrayIndexOutOfBoundsException("Index `"+idx+"' is invalid.");
    }

    public intbuf append ( java.lang.Object val){
	if (val instanceof java.lang.Number){
	    java.lang.Number numeric = (java.lang.Number)val;
	    return this.append(numeric.intValue());
	}
	else if (null != val)
	    throw new java.lang.IllegalArgumentException(val.toString());
	else
	    throw new java.lang.IllegalArgumentException();
    }
    public intbuf append ( int val){

	if ( p >= buf.length){

	    int[] copier = new int[buf.length+f];

	    System.arraycopy(buf,0,copier,0,buf.length);

	    this.buf = copier;
	}
	this.buf[p++] = val;

	return this;
    }
    public int[] toIntArray(){
	int p = this.p;
	int[] buf = this.buf;
	if ( 0 < p){
	    int[] ret = new int[p];
	    for ( int cc = 0; cc < p; cc++)
		ret[cc] = buf[cc];
	    return ret;
	}
	else
	    return null;
    }
    public intbuf line_separator( String sep){
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
    public intbuf invert(){
	int p = this.p;
	int[] buf = this.buf;
	if ( 0 >= p) 
	    return this;
	else {
	    int len = p;

	    int[] copier = new int[len];

	    System.arraycopy(buf,0,copier,0,p);

	    int s;

	    for ( int top = (len-1), bot = 0; bot < len; bot++, top--){

		if ( bot >= top)
		    break;
		else {
		    s = copier[bot];

		    copier[bot] = copier[top];

		    copier[top] = s;
		}
	    }

	    this.buf = copier;

	    return this;
	}
    }
    public String toString(){
	int p = this.p;
	int[] buf = this.buf;
	if ( 0 < p){
	    chbuf strbuf = new chbuf(p*10);
	    if ( null != output_prefix)
		strbuf.append(output_prefix);
	    for ( int cc = 0; cc < p; cc++){
		if ( 0 < cc) 
		    strbuf.append(output_line_sep);
		strbuf.append(String.valueOf(buf[cc]));
	    }
	    return strbuf.toString();
	}
	else 
	    return "";
    }
    public intbuf reset(){
	int p = this.p;
	int[] buf = this.buf;
	for ( int cc = 0; cc < p; cc++)
	    buf[cc] = 0;
	this.p = 0;
	return this;
    }
    public int length(){
	return this.p;
    }
    public int pop (int defval){
	int p = this.p;
	int[] buf = this.buf;
	if ( 0 < p){
	    int re = buf[0];
	    System.arraycopy( buf, 1, buf, 0, p-1);
	    this.p -= 1;
	    return re;
	}
	else
	    return defval;
    }

}
