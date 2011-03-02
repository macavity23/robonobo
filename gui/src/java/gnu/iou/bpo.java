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

import java.io.PrintStream;

/**
 * Print stream over <tt>`linebuf'</tt>.  This uses an intermediate
 * "line" buffer for <tt>"print"</tt> that can be flushed to the
 * output (final) buffer using <tt>"flush()"</tt>.  This scheme
 * (without extra flushing) works in normal Print Stream usage for
 * <tt>"print"</tt> followed by <tt>"println"</tt>, but breaks when,
 * for example, <tt>"print"</tt> contains a newline.
 *
 * @author John Pritchard (john@syntelos.org)
 * 
 * @see linebuf */
public class bpo extends PrintStream {



    private linebuf lb ;

    private chbuf line = new chbuf();

    public bpo ( linebuf lb ){
	super(new bbo()/*not-used*/);
	if ( null != lb)
	    this.lb = lb;
	else
	    this.lb = new linebuf();
    }

    public bpo (){
	this(new linebuf());
    }



    public final String[] toStringArray(){ 
	this.flush();

	return lb.toStringArray();
    }
    public final char[][] toCharCharArray(){
	this.flush();

	return lb.toCharCharArray();
    }
    public final String toString(){ 
	this.flush();

	return lb.toString();
    }
    public final char[] toCharArray(){
	this.flush();

	return lb.toCharArray();
    }
    public final byte[] toByteArray(){
	this.flush();

	return utf8.encode(lb.toCharArray());
    }

    /**
     * Delete the first line in the buffer (ignores state of line
     * buffer, which should have been flushed).
     */
    public final void pop(){
	lb.pop();
    }

    /**
     * Reset (intermediate) line, and output buffers.
     */
    public final void reset(){
	line.reset();
	lb.reset();
    }

    /**
     * Number of lines
     */
    public final int length(){
	return lb.length();
    }

    public final linebuf linebuf(){ return lb;}

    public final chbuf chbuf(){ return line;}


    /**
     * Produce a string from the stack trace print from the argument.
     * 
     * @param t Throwable to print.
     */
    public final static String toString ( Throwable t){

	bpo buf = new bpo();

	t.printStackTrace(buf);

	return buf.toString();
    }

    public final static String atStack( Throwable t){
	return atStack(t,0);
    }
    /**
     * @param t Exception to trace
     * @param pop Number of lines to pop from the stack trace.  This
     * is for an artificial trace of a throwable constructed just to
     * know where the code is.  Pop one (argument value 1) to skip the
     * exception itself, another (2) to drop the caller.  A value less
     * than one preserves the entire trace.
     * @return Condensed stack trace for logging.
     */
    public final static String atStack( Throwable t, int pop){

	bpo ps = new bpo();

	t.printStackTrace(ps);

	for (int cc = 0; cc < pop; cc++)
	    ps.pop();

	linebuf buf = ps.linebuf();

	String strary[] = buf.toStringArray(), lin;

	chbuf cb = ps.chbuf(); //reuse

	cb.reset();

	int len = strary.length;

	if ( 8 < len) len = 8;

	for (int idx, idx2, cc = 0; cc < len; cc++){

	    lin = strary[cc];

	    idx = lin.lastIndexOf('(');
	    idx2 = lin.lastIndexOf(".java");

	    if (0 < idx && 0 < idx2)
		lin = chbuf.cat(lin.substring(0,idx+1),lin.substring(idx2+5));

	    idx = lin.lastIndexOf(' ');
	    if (-1 < idx)
		lin = lin.substring(idx+1);

	    cb.append('@'); 

	    cb.append(lin);
	}

	return cb.toString();
    }

    /**
     * Produce a stack trace of the current position, deleting the
     * <tt>`Throwable.toString()'</tt> and <tt>`bbo.stackTrace()'</tt>
     * lines produced by <tt>`new Exception()'</tt>.
     */
    public final static String stackTrace(){

	Throwable stack = new Exception();

	bpo buf = new bpo();

	stack.printStackTrace(buf);

	buf.pop();

	buf.pop();

	return buf.toString();
    }



    public final void write ( int b) {

	line.append( (char)b);
    }
    public final void write ( byte[] b) {

	this.write(b,0,b.length);
    }
    public final void write ( byte[] b, int ofs, int len) {

	char[] cary = utf8.decode(b,ofs,len);
	
	line.append(cary);
    }
    public final void flush() {
	String str = line.toString();
	if ( null != str)
	    lb.append(str);
	line.reset();
    }
    public final void close() {
	this.flush();
    }
    public final boolean checkError() {
	return false;
    }
    public final void print(boolean o) {
	line.append(o);
    }
    public final void print(char o) {
	line.append(o);
    }
    public final void print(int o) {
	line.append(o);
    }
    public final void print(long o) {
	line.append(o);
    }
    public final void print(float o) {
	line.append(o);
    }
    public final void print(double o) {
	line.append(o);
    }
    public final void print(char o[]) {
	line.append(o);
    }
    public final void print(String o) {
	line.append(o);
    }
    public final void print(Object o) {
	line.append(o);
    }
    public final void println() {
	lb.append(line.toString());
	line.reset();
    }
    public final void println(boolean o) {
	line.append(o);
	lb.append(line.toString());
	line.reset();
    }
    public final void println(char o) {
	line.append(o);
	lb.append(line.toString());
	line.reset();
    }
    public final void println(int o) {
	line.append(o);
	lb.append(line.toString());
	line.reset();
    }
    public final void println(long o) {
	line.append(o);
	lb.append(line.toString());
	line.reset();
    }
    public final void println(float o) {
	line.append(o);
	lb.append(line.toString());
	line.reset();
    }
    public final void println(double o) {
	line.append(o);
	lb.append(line.toString());
	line.reset();
    }
    public final void println(char[] o) {
	line.append(o);
	lb.append(line.toString());
	line.reset();
    }
    public final void println(String o) {
	line.append(o);
	lb.append(line.toString());
	line.reset();
    }
    public final void println(Object o) {
	line.append(o);
	lb.append(line.toString());
	line.reset();
    }
}
