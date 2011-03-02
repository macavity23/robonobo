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

package gnu.iou.fil ;

/**
 * Filter output stream with {@link gnu.iou.bbo BBO} extensions
 * available for BBO output streams only.  The print (to UTF8)
 * extensions are available in any case with or without BBO.
 * 
 * @author John Pritchard (jdp@syntelos.com)
 * 
 * @see gnu.iou.bbo
 * @see fii
 */
public class foo
    extends java.io.FilterOutputStream 
{
    /**
     * Indent character is SPACE
     */
    protected final static char INDENT = ' ';
    /**
     * Newline terminal is CRLF
     */
    protected final static char[] NEWLINE = {
	'\r',
	'\n'
    };

    private final boolean have_bbo;

    private gnu.iou.bbo bbo;

    private char indent_char = INDENT;

    private char[] newline_cary = NEWLINE;

    public foo( java.io.OutputStream out){
	super(out);
	if (out instanceof gnu.iou.bbo){
	    this.have_bbo = true;
	    this.bbo = (gnu.iou.bbo)out;
	}
	else if (out instanceof foo){
	    foo out_foo = (foo)out;
	    this.have_bbo = out_foo.haveBBO();
	    if (this.have_bbo)
		this.bbo = out_foo.getBBO();
	}
	else {
	    this.have_bbo = false;
	}
    }
    public foo( gnu.iou.bbuf buf){
	this(new gnu.iou.bbo(buf));
    }

    public char getCharIndent(){
	return this.indent_char;
    }
    public void setCharIndent(char ch){
	this.indent_char = ch;
    }
    public char[] getCaryNewline(){

	return gnu.iou.chbuf.copy( this.newline_cary);
    }
    public void setCaryNewline(char[] newline){
	this.newline_cary = newline;
    }

    /**
     * @return Whether the BBO dependent methods will work without
     * throwing exceptions, whether this object has a non null BBO.
     */
    public final boolean haveBBO(){
	return this.have_bbo;
    }
    /**
     * @return A non null BBO, or exception
     * @exception java.lang.IllegalStateException For no BBO
     * @see #haveBBO()
     */
    public final gnu.iou.bbo getBBO(){
	if (this.have_bbo)
	    return this.bbo;
	else
	    throw new java.lang.IllegalStateException();
    }
    /**
     * @return The filter output stream underlying target stream
     */
    public final java.io.OutputStream getOut(){
	return super.out;
    }

    /**
     * @return BBO offset of the last byte read, or negative one before
     * the first byte has been read
     * @exception java.lang.IllegalStateException For no BBO
     * @see #haveBBO()
     */
    public int offset(){
	if (this.have_bbo)
	    return this.bbo.offset();
	else
	    throw new java.lang.IllegalStateException();
    }
    /**
     * @return BBO Buffer
     * @exception java.lang.IllegalStateException For no BBO
     * @see #haveBBO()
     */
    public gnu.iou.bbuf getByteBuffer(){ 
	if (this.have_bbo)
	    return this.bbo.getByteBuffer();
	else
	    throw new java.lang.IllegalStateException();
    }
    /**
     * @return BBO buffer, null with no BBO
     * @see #haveBBO()
     */
    public byte[] toByteArray(){ 
	if (this.have_bbo)
	    return this.bbo.toByteArray();
	else
	    return null;
    }
    /**
     * @return BBO buffer decoded from UTF8, null with no BBO
     * @see #haveBBO()
     */
    public String toString(){ 
	if (this.have_bbo)
	    return this.bbo.toString();
	else
	    return null;
    }
    /**
     * @return BBO buffer length, or zero with no BBO
     * @see #haveBBO()
     */
    public int length(){ 
	if (this.have_bbo)
	    return this.bbo.length();
	else
	    return 0;
    }
    /**
     * BBO buffer reset, no op without BBO
     * @see #haveBBO()
     */
    public void reset() 
	throws java.io.IOException 
    {
	if (this.have_bbo)
	    this.bbo.reset();
    }
    /**
     * @return BBO marked buffer, null with no BBO
     * @see #haveBBO()
     * @see #markSupported()
     */
    public byte[] marked(){
	if (this.have_bbo)
	    return this.bbo.marked();
	else
	    return null;
    }
    /**
     * @see #markSupported()
     */
    public void mark(){
	if (this.have_bbo)
	    this.bbo.mark();
    }
    /**
     * @see #haveBBO()
     * @return True for BBO, otherwise false.
     */
    public boolean markSupported(){
	if (this.have_bbo)
	    return this.bbo.markSupported();
	else
	    return false;
    }  
    /**
     * Write to format UTF-8
     * @param ch Output character to format encoding
     */
    public void print(char ch)
	throws java.io.IOException
    {
	if (ch < 0x80)
	    this.write(ch);
	else {
	    char[] cary = new char[]{ch};
	    byte[] bary = gnu.iou.utf8.encode(cary);
	    this.write(bary,0,bary.length);
	}
    }
    /**
     * @param ch Output character to format encoding
     * @param many Repeat output this many times
     */
    public void nprint(char ch, int many)
	throws java.io.IOException
    {
	byte[] bary;
	if (ch < 0x80){
	    byte bb = (byte)ch;
	    bary = new byte[many];
	    for (int cc = 0; cc < many; cc++)
		bary[cc] = bb;
	}
	else {
	    gnu.iou.chbuf buf = new gnu.iou.chbuf(many);
	    buf.appendMany(ch,many);
	    bary = buf.toByteArray();//(enc to utf8)
	}
	this.write(bary,0,bary.length);
    }
    /**
     * Write to format UTF-8
     * @param string Output data to format encoding
     */
    public void print(String string)
	throws java.io.IOException
    {
	if (null != string){
	    char[] cary = string.toCharArray();
	    if (0 < cary.length){
		byte[] bary = gnu.iou.utf8.encode(cary);
		this.write(bary,0,bary.length);
	    }
	}
    }
    /**
     * Write to format UTF-8
     * @param indent Indentation level
     * @param string Output data to format encoding
     */
    public void print(int indent, String string)
	throws java.io.IOException
    {
	this.nprint(this.indent_char,indent);
	this.print(string);
    }
    /**
     * Write to format UTF-8
     * @param ch Output character to format encoding 
     */
    public void println(char ch)
	throws java.io.IOException
    {
	char[] cary = gnu.iou.chbuf.cat(ch,this.newline_cary);
	byte[] bary = gnu.iou.utf8.encode(cary);
	this.write(bary,0,bary.length);
    }
    /**
     * Write to format UTF-8
     * @param string Output data to format encoding with newline
     */
    public void println(String string)
	throws java.io.IOException
    {
	char[] cary = null;
	if (null != string)
	    cary = string.toCharArray();
	cary = gnu.iou.chbuf.cat(cary,this.newline_cary);
	byte[] bary = gnu.iou.utf8.encode(cary);
	this.write(bary,0,bary.length);
    }
    /**
     * Write to format UTF-8
     * @param indent Indentation level
     * @param string Output data to format encoding
     */
    public void println(int indent, String string)
	throws java.io.IOException
    {
	this.nprint(this.indent_char,indent);
	this.println(string);
    }

}
