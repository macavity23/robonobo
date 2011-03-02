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

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;

/**
 * Print stream <tt>`tee'</tt>.
 *
 * @author John Pritchard (john@syntelos.org)
 * 
 * @see tee
 */
public class pte extends PrintStream {



    protected volatile PrintStream fir ;

    protected volatile PrintStream sec ;

    protected volatile PrintStream thi ;

    protected boolean debugCopyTrace = false;

    public pte ( PrintStream first, PrintStream second ){

	super(first);// not used as `super.out'

	if ( null == first) 
	    throw new IllegalArgumentException("Constructing `pte(2)' with one stream?");
	else
	    this.fir = first;

	if ( null == second) 
	    throw new IllegalArgumentException("Constructing `pte(2)' with one stream?");
	else
	    this.sec = second;
    }
    /**
     * Debug mode, copy everything to a file.
     *
     * @param first Debugged stream
     * 
     * @param filename Copy file
     * 
     * @param trace If true, print a "newline- newline- stack trace- newline" into the copy file at the start of each invocation of each method.
     */
    public pte ( PrintStream first, String filename, boolean trace) throws IOException {

	this(first,new PrintStream(new FileOutputStream(filename)));

	this.debugCopyTrace = trace;
    }
    public pte ( pte first, PrintStream third ){
	this(first.fir,first.sec);

	if ( null == third) 
	    throw new IllegalArgumentException("Constructing `pte(3)' with two streams?");
	else
	    this.thi = third;
    }



    public PrintStream pteOrig(){ 

	if ( this.fir instanceof pte)

	    return ((pte)this.fir).pteOrig();
	else
	    return fir;
    }

    public boolean pteFull(){ return (null != this.thi);}

    public boolean pteEmpty(){ return (null == this.sec);}

    protected void pteAdd( PrintStream ps){

	if ( null != ps){

	    if ( null == this.sec)

		this.sec = ps;

	    else if ( null == this.thi)

		this.thi = ps;

	    else if ( this.thi instanceof pte)

		((pte)this.thi).pteAdd(ps);

	    else {
		pte t = new pte( this.thi, ps);

		this.thi = t;
	    }
	}
    }
    /**
     * @exception IllegalStateException Attempting to remove first
     * stream from pte (ie, when pte is empty -- only one stream in
     * it).  */
    protected void pteRemove( PrintStream ps){

	if ( null != ps){

	    if ( ps == this.thi)

		this.thi = null;

	    else if ( ps == this.sec){

		if ( null != this.thi){

		    PrintStream t = this.thi;

		    this.thi = null;

		    this.sec = t;
		}
		else 
		    this.sec = null;
	    }
	    else if ( ps == this.fir){

		if ( null != this.thi){

		    PrintStream t = this.thi;

		    this.thi = null;

		    PrintStream s = this.sec;

		    this.sec = t;

		    this.fir = s;
		}
		else if ( null != this.sec){

		    PrintStream s = this.sec;

		    this.sec = null;

		    this.fir = this.sec;
		}
		else
		    throw new IllegalStateException("Removing root from pte.");
	    }
	    else if ( this.thi instanceof pte){

		try {
		    ((pte)this.thi).pteRemove(ps);
		}
		catch ( IllegalStateException mmt){

		    this.thi = null; // is empty
		}
	    }
	}
    }

    protected void trace(){
	sec.println();
	sec.println();
	sec.println(bpo.stackTrace());
    }

    public void write ( int b) {
	fir.write(b);
	if ( null != sec){
	    if (this.debugCopyTrace)
		trace();
	    sec.write(b);
	}
	if ( null != thi)
	    thi.write(b);
    }
    public void write ( byte[] b) {
	this.write(b,0,b.length);
    }
    public void write ( byte[] b, int ofs, int len) {
	fir.write(b, ofs, len);
	if ( null != sec){
	    if (this.debugCopyTrace)
		trace();
	    sec.write(b, ofs, len);
	}
	if ( null != thi)
	    thi.write(b, ofs, len);
    }
    public void flush() {
	fir.flush();
	if ( null != sec){
	    if (this.debugCopyTrace)
		trace();
	    sec.flush();
	}
	if ( null != thi)
	    thi.flush();
    }
    public void close() {
	if (null != fir)
	    fir.close();
	if (null != sec)
	    sec.close();
	if (null != thi)
	    thi.close();
    }
    public boolean checkError() {

	boolean err1 = fir.checkError();

	if ( null != sec){
	    boolean err2 = sec.checkError();

	    if ( null != thi)
		return (thi.checkError()||err2||err1);
	    else
		return (err2||err1);
	}
	else
	    return err1;
    }
    public void print(boolean o) {
	fir.print(o);
	if ( null != sec){
	    if (this.debugCopyTrace)
		trace();
	    sec.print(o);
	}
	if ( null != thi)
	    thi.print(o);
    }
    public void print(char o) {
	fir.print(o);
	if ( null != sec){
	    if (this.debugCopyTrace)
		trace();
	    sec.print(o);
	}
	if ( null != thi)
	    thi.print(o);
    }
    public void print(int o) {
	fir.print(o);
	if ( null != sec){
	    if (this.debugCopyTrace)
		trace();
	    sec.print(o);
	}
	if ( null != thi)
	    thi.print(o);
    }
    public void print(long o) {
	fir.print(o);
	if ( null != sec){
	    if (this.debugCopyTrace)
		trace();
	    sec.print(o);
	}
	if ( null != thi)
	    thi.print(o);
    }
    public void print(float o) {
	fir.print(o);
	if ( null != sec){
	    if (this.debugCopyTrace)
		trace();
	    sec.print(o);
	}
	if ( null != thi)
	    thi.print(o);
    }
    public void print(double o) {
	fir.print(o);
	if ( null != sec){
	    if (this.debugCopyTrace)
		trace();
	    sec.print(o);
	}
	if ( null != thi)
	    thi.print(o);
    }
    public void print(char o[]) {
	fir.print(o);
	if ( null != sec){
	    if (this.debugCopyTrace)
		trace();
	    sec.print(o);
	}
	if ( null != thi)
	    thi.print(o);
    }
    public void print(String o) {
	fir.print(o);
	if ( null != sec){
	    if (this.debugCopyTrace)
		trace();
	    sec.print(o);
	}
	if ( null != thi)
	    thi.print(o);
    }
    public void print(Object o) {
	fir.print(o);
	if ( null != sec){
	    if (this.debugCopyTrace)
		trace();
	    sec.print(o);
	}
	if ( null != thi)
	    thi.print(o);
    }
    public void println() {
	fir.println();
	if ( null != sec){
	    if (this.debugCopyTrace)
		trace();
	    sec.println();
	}
	if ( null != thi)
	    thi.println();
    }
    public void println(boolean o) {
	fir.println(o);
	if ( null != sec){
	    if (this.debugCopyTrace)
		trace();
	    sec.println(o);
	}
	if ( null != thi)
	    thi.println(o);
    }
    public void println(char o) {
	fir.println(o);
	if ( null != sec){
	    if (this.debugCopyTrace)
		trace();
	    sec.println(o);
	}
	if ( null != thi)
	    thi.println(o);
    }
    public void println(int o) {
	fir.println(o);
	if ( null != sec){
	    if (this.debugCopyTrace)
		trace();
	    sec.println(o);
	}
	if ( null != thi)
	    thi.println(o);
    }
    public void println(long o) {
	fir.println(o);
	if ( null != sec){
	    if (this.debugCopyTrace)
		trace();
	    sec.println(o);
	}
	if ( null != thi)
	    thi.println(o);
    }
    public void println(float o) {
	fir.println(o);
	if ( null != sec){
	    if (this.debugCopyTrace)
		trace();
	    sec.println(o);
	}
	if ( null != thi)
	    thi.println(o);
    }
    public void println(double o) {
	fir.println(o);
	if ( null != sec){
	    if (this.debugCopyTrace)
		trace();
	    sec.println(o);
	}
	if ( null != thi)
	    thi.println(o);
    }
    public void println(char[] o) {
	fir.println(o);
	if ( null != sec){
	    if (this.debugCopyTrace)
		trace();
	    sec.println(o);
	}
	if ( null != thi)
	    thi.println(o);
    }
    public void println(String o) {
	fir.println(o);
	if ( null != sec){
	    if (this.debugCopyTrace)
		trace();
	    sec.println(o);
	}
	if ( null != thi)
	    thi.println(o);
    }
    public void println(Object o) {
	fir.println(o);
	if ( null != sec){
	    if (this.debugCopyTrace)
		trace();
	    sec.println(o);
	}
	if ( null != thi)
	    thi.println(o);
    }
}
