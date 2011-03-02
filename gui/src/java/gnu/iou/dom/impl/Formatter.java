 /* 
  *  `gnu.iou.dom' 
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

package gnu.iou.dom.impl;

/**
 * <p> DOM writers for XML UTF-8 format. </p>
 * 
 * @see Formatter$Buffer
 * @see Formatter$Stream
 * @see Formatter$Null
 * 
 * @author jdp
 */
public abstract class Formatter
{
    /**
     * Default indent character is space.
     */
    public static char CHAR_INDENT = ' ';

    /**
     * Simple DOM writing stack used by DOM writers implements DOM
     * walking with {@link gnu.iou.dom.Formatter$WriteX} semantics.
     */
    public static class State
	extends java.lang.Object
	implements gnu.iou.dom.Formatter.State
    {
	private final static int DEPTH = 9;

	private final static java.lang.String PR_NIL = "";

	private int sp = 0;

	private gnu.iou.dom.Element[] stack = new gnu.iou.dom.Element[DEPTH];

	private boolean headline = true;

	public State(){
	    super();
	}

	public void reset(){
	    this.sp = 0;
	    this.stack = new gnu.iou.dom.Element[DEPTH];
	    this.headline = true;
	}
	public void destroy(){
	    this.sp = 0;
	    this.stack = null;
	    this.headline = true;
	}
	public boolean headline(){
	    if (0 == this.sp)
		return (this.headline = (!this.headline));
	    else
		throw new gnu.iou.dom.Error.State("State is not <init>, indent is not zero.");
	}
	public int indent(){
	    return this.sp;
	}
	protected void push(gnu.iou.dom.Element elem){
	    if (null == elem)
		throw new gnu.iou.dom.Error.State();
	    else {
		this.stack[this.sp++] = elem;
		if (this.sp >= this.stack.length){
		    gnu.iou.dom.Element[] copier = new gnu.iou.dom.Element[this.stack.length+DEPTH];
		    java.lang.System.arraycopy(this.stack,0,copier,0,this.stack.length);
		    this.stack = copier;
		}
	    }
	}
	protected gnu.iou.dom.Element pop(){
	    if (-1 < this.sp){
		this.sp -= 1;
		gnu.iou.dom.Element re = this.stack[this.sp];
		this.stack[this.sp] = null;
		return re;
	    }
	    else
		return null;
	}
	protected gnu.iou.dom.Element peek(){

	    return this.stack[this.sp];
	}
	protected void headline(gnu.iou.dom.Formatter out)
	    throws java.io.IOException
	{
	    if (this.headline){
		out.println("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
		this.headline = false;
	    }
	}
	/**
	 * @param map Optional XMLNS logic store used by this function
	 * to avoid duplication in output, managed by the caller to be
	 * specific to a scope.  Forms a part of the whole XMLNS
	 * process.
	 * @param prefix QName prefix for namespace 
	 * @param ns Namespace URI
	 * @param out Required target output
	 */
	protected void xmlns(gnu.iou.objmap map, String prefix, String ns, gnu.iou.dom.Formatter out)
	    throws java.io.IOException
	{
	    if (null != ns && 0 < ns.length() &&
		(null == map || null == map.get(prefix)))
	    {
		if (null == prefix || 1 > prefix.length())
		    prefix = PR_NIL;
		if (null != map){
		    if (null != map.get(prefix))
			return;
		    else
			map.put(prefix,ns);
		}
		out.print(' ');
		if (PR_NIL != prefix){
		    out.print("xmlns:");
		    out.print(prefix);
		    out.print('=');
		}
		else 
		    out.print("xmlns=");
		//
		out.printSafeQuoted(ns);
	    }
	}
	public boolean write(org.w3c.dom.Node node, gnu.iou.dom.Formatter out)
	    throws java.io.IOException
        {
	    if (node instanceof gnu.iou.dom.Node)
                return this.write( (gnu.iou.dom.Node)node, out);
	    else
		throw new gnu.iou.dom.Error.Argument();
        }
	public boolean write(org.w3c.dom.CDATASection node, gnu.iou.dom.Formatter out)
	    throws java.io.IOException
        {
	    return this.write( (gnu.iou.dom.CDATASection)node, out);
        }
	public boolean write(org.w3c.dom.Comment node, gnu.iou.dom.Formatter out)
	    throws java.io.IOException
        {
	    return this.write( (gnu.iou.dom.Comment)node, out);
        }
	public boolean write(org.w3c.dom.Document node, gnu.iou.dom.Formatter out)
	    throws java.io.IOException
        {
	    return this.write( (gnu.iou.dom.Document)node, out);
        }
	public boolean write(org.w3c.dom.Element node, gnu.iou.dom.Formatter out)
	    throws java.io.IOException
	{
	    return this.write( (gnu.iou.dom.Element)node, out);
	}
	public boolean write(org.w3c.dom.Entity node, gnu.iou.dom.Formatter out)
	    throws java.io.IOException
        {
	    throw new java.lang.UnsupportedOperationException("todo");
        }
	public boolean write(org.w3c.dom.EntityReference node, gnu.iou.dom.Formatter out)
	    throws java.io.IOException
        {
	    throw new java.lang.UnsupportedOperationException("todo");
        }
	public boolean write(org.w3c.dom.ProcessingInstruction node, gnu.iou.dom.Formatter out)
	    throws java.io.IOException
        {
	    throw new java.lang.UnsupportedOperationException("todo");
        }
	public boolean write(org.w3c.dom.Text node, gnu.iou.dom.Formatter out)
	    throws java.io.IOException
        {
	    return this.write( (gnu.iou.dom.Text)node, out);
        }
	public boolean write(gnu.iou.dom.Node node, gnu.iou.dom.Formatter out)
	    throws java.io.IOException
        {
	    if (null == node)
		return false;
	    else {
		switch (node.getNodeType()){
	        case org.w3c.dom.Node.CDATA_SECTION_NODE:
		    return this.write( (gnu.iou.dom.CDATASection)node, out);
		case org.w3c.dom.Node.COMMENT_NODE:
		    return this.write( (gnu.iou.dom.Comment)node, out);
		case org.w3c.dom.Node.DOCUMENT_NODE:
		    return this.write( (gnu.iou.dom.Document)node, out);
		case org.w3c.dom.Node.ELEMENT_NODE:
		    return this.write( (gnu.iou.dom.Element)node, out);
		case org.w3c.dom.Node.ENTITY_NODE:
		    return this.write( (gnu.iou.dom.Entity)node, out);
		case org.w3c.dom.Node.ENTITY_REFERENCE_NODE:
		    return this.write( (gnu.iou.dom.EntityReference)node, out);
		case org.w3c.dom.Node.PROCESSING_INSTRUCTION_NODE:
		    return this.write( (gnu.iou.dom.ProcessingInstruction)node, out);
		case org.w3c.dom.Node.TEXT_NODE:
		    return this.write( (gnu.iou.dom.Text)node, out);
		default:
		    throw new gnu.iou.dom.Error.Argument(node.getClass()+" type "+node.getNodeType());
		}
	    }
        }
        public boolean write(gnu.iou.dom.CDATASection node, gnu.iou.dom.Formatter out)
            throws java.io.IOException
        {
	    out.print("<![CDATA[");
	    out.print(node.getNodeValue());
	    out.print("]]>");
	    return false;
        }
        public boolean write(gnu.iou.dom.Comment node, gnu.iou.dom.Formatter out)
            throws java.io.IOException
        {
	    out.print("<!-- ");
	    out.print(node.getNodeValue());
	    out.print(" -->");
	    return false;
        }
        public boolean write(gnu.iou.dom.Document node, gnu.iou.dom.Formatter out)
            throws java.io.IOException
        {
	    if (node.hasChildNodes()){
		gnu.iou.dom.NodeList children = node.getChildNodes2();
		gnu.iou.dom.Node child;
		boolean re = false;
		for (int idx = 0, len = children.getLength(); idx < len; idx++){
		    child = children.item2(idx);
		    re = this.write(child,out);
		}
		return re;
	    }
	    else
		return false;
        }
	public boolean write(gnu.iou.dom.Element elem, gnu.iou.dom.Formatter out)
	    throws java.io.IOException
	{
	    int indent = this.sp;
	    if (0 == indent)
		this.headline(out);
	    //
	    gnu.iou.dom.Name name = elem.getNodeName2();
	    String qname = name.getQname();
	    //
	    gnu.iou.dom.Formatter.WriteX xelem = null;
	    /*
	     * Write head
	     */
	    if (elem instanceof gnu.iou.dom.Formatter.WriteX){
		xelem = (gnu.iou.dom.Formatter.WriteX)elem;

		if (!xelem.writeX(out))

		    return true;/*(heuristic assumption)
				 */
	    }
	    else {
		if (elem instanceof gnu.iou.dom.Formatter.Prewrite){
		    gnu.iou.dom.Formatter.Prewrite prewrite = (gnu.iou.dom.Formatter.Prewrite)elem;
		    prewrite.prewrite(this);
		}
		out.print(indent,'<');
		out.print(qname);
		//
		gnu.iou.objmap nstore = new gnu.iou.objmap();
		String ns = name.getNamespace();
		if ( null != ns && (!elem.isDeclaredNamespace(ns))){
		    //
		    String prefix = name.getPrefix();
		    this.xmlns(nstore,prefix,ns,out);
		}
		//
		if (elem.hasAttributes()){
		    org.w3c.dom.NamedNodeMap attributes = elem.getAttributes();
		    org.w3c.dom.Node attribute;
		    String qn, value, pr;
		    for (int cc = 0, len = attributes.getLength(); cc < len; cc++){
			attribute = attributes.item(cc);
			value = attribute.getNodeValue();
			qn = attribute.getNodeName();
			if (qn != attribute.getLocalName()){
			    pr = attribute.getPrefix();
			    if (null != pr){
				if ("xmlns".equals(pr)){
				    this.xmlns(nstore,attribute.getLocalName(),value,out);
				    continue;
				}
				else {
				    ns = attribute.getNamespaceURI();
				    if ( null != ns && (!elem.isDeclaredNamespace(ns)))
					this.xmlns(nstore,pr,ns,out);
				}
			    }
			}
			if ("xmlns".equals(qn))
			    continue;
			else {
			    out.print(' ');
			    out.print(qn);
			    out.print('=');
			    out.printSafeQuoted(value);
			}
		    }
		}
	    }
	    /*
	     * Write body
	     */
	    if (elem.hasChildNodes()){
		gnu.iou.dom.NodeList children = elem.getChildNodes2();
		boolean printline = (org.w3c.dom.Node.ELEMENT_NODE == children.typeFirst());
		if (null == xelem){
		    if (printline)
			out.println('>');
		    else
			out.print('>');
		}
		this.push(elem);
		org.w3c.dom.Node child;
		for (int cc = 0, len = children.getLength(); cc < len; cc++){
		    child = children.item(cc);
		    printline = this.write(child,out);
		}
		if (elem != this.pop())
		    throw new gnu.iou.dom.Error.State("bug");
		else {
		    /*
		     * Write tail with children
		     */
		    if (null != xelem){
			if (xelem instanceof gnu.iou.dom.Formatter.WriteX.Closing){
			    gnu.iou.dom.Formatter.WriteX.Closing closing = 
				(gnu.iou.dom.Formatter.WriteX.Closing)xelem;

			    return closing.writeX2(out);
			}
			else
			    return true;/*(heuristic assumption)
					 */
		    }
		    else {
			if (printline)
			    out.print(indent,"</");
			else
			    out.print("</");
			out.print(qname);
			out.println('>');

			if (elem instanceof gnu.iou.dom.Formatter.Postwrite){
			    gnu.iou.dom.Formatter.Postwrite postwrite = (gnu.iou.dom.Formatter.Postwrite)elem;
			    postwrite.postwrite(this);
			}
			return true;
		    }
		}
	    }
	    else {
		/*
		 * Write tail, no children
		 */
		if (null == xelem){
		    out.println("/>");

		    if (elem instanceof gnu.iou.dom.Formatter.Postwrite){
			gnu.iou.dom.Formatter.Postwrite postwrite = (gnu.iou.dom.Formatter.Postwrite)elem;
			postwrite.postwrite(this);
		    }
		    return true;
		}
		else
		    return true;/*(heuristic assumption)
				 */
	    }
	}
        public boolean write(gnu.iou.dom.Entity node, gnu.iou.dom.Formatter out)
            throws java.io.IOException
        {
	    throw new java.lang.UnsupportedOperationException("todo");
        }
        public boolean write(gnu.iou.dom.EntityReference node, gnu.iou.dom.Formatter out)
            throws java.io.IOException
        {
	    throw new java.lang.UnsupportedOperationException("todo");
        }
        public boolean write(gnu.iou.dom.ProcessingInstruction node, gnu.iou.dom.Formatter out)
            throws java.io.IOException
        {
	    throw new java.lang.UnsupportedOperationException("todo");
        }
        public boolean write(gnu.iou.dom.Text node, gnu.iou.dom.Formatter out)
            throws java.io.IOException
        {
	    java.lang.String string = node.getNodeValue();
	    if (null != string){
		int string_len = string.length();
		if (0 < string_len){
		    out.printSafe(string);
		    switch (string.charAt(string_len-1)){
		    case '\r':
		    case '\n':
			return true;
		    default:
			return false;
		    }
		}
	    }
	    return false;
        }
    }

    /**
     * Write DOM to XML in UTF-8 to buffer.
     */
    public static class Buffer
	extends gnu.iou.bbod
	implements gnu.iou.dom.Formatter
    {

	private char char_indent = CHAR_INDENT;

	private gnu.iou.dom.Formatter.State walker;


	public Buffer(int cap){
	    super(cap);
	    this.walker = this.ctorState();
	}
	public Buffer(){
	    super();
	    this.walker = this.ctorState();
	}
	public Buffer(gnu.iou.bbuf buf){
	    super(buf);
	    this.walker = this.ctorState();
	}

	protected gnu.iou.dom.Formatter.State ctorState(){
	    return new Formatter.State();
	}
	protected final gnu.iou.dom.Formatter.State getState(){
	    return this.walker;
	}
	public void reset(){
	    this.walker.reset();
	    super.reset();
	}
	public int indent(){
	    return this.walker.indent();
	}
	public char getCharIndent(){
	    return this.char_indent;
	}
	public void setCharIndent(char ch){
	    if (0 == this.indent())

		this.char_indent = ch; /*(liberal acceptance)
					*/
	    else
		throw new gnu.iou.dom.Error.State("State is not <init>, indent is not zero.");
	}
	public boolean headline(){
	    return this.walker.headline();
	}
	public void write(gnu.iou.dom.Element node)
	    throws java.io.IOException
	{
	    if (null != node)
		this.walker.write(node,this);
	}
	public void write(org.w3c.dom.Element node)
	    throws java.io.IOException
	{
	    if (null != node)
		this.walker.write(node,this);
	}
	public void write(gnu.iou.dom.Document node)
	    throws java.io.IOException
	{
	    if (null != node)
		this.walker.write(node,this);
	}
	public void write(org.w3c.dom.Document node)
	    throws java.io.IOException
	{
	    if (null != node)
		this.walker.write(node,this);
	}
	public void print(int indent, String string)
	    throws java.io.IOException
	{
	    this.nprint(this.char_indent,indent);
	    this.print(string);
	}  
	public void print(int indent, char ch)
	    throws java.io.IOException
	{
	    this.nprint(this.char_indent,indent);
	    this.print(ch);
	}
	public void println(int indent, String string)
	    throws java.io.IOException
	{
	    this.nprint(this.char_indent,indent);
	    this.println(string);
	}
	/**
	 * XML safe string encodes XML format special characters
	 * "&amp;, &lt;, &gt;" to their XML 1.0 standard character
	 * references.
	 */
	public void printSafe(java.lang.String string)
	    throws java.io.IOException
	{
	    if (null != string){
		char cary[] = string.toCharArray(), ch;
		for (int cc = 0, len = cary.length; cc < len; cc++){
		    ch = cary[cc];
		    switch(ch){
		    case '<':
			this.print("&lt;");
			break;
		    case '>':
			this.print("&gt;");
			break;
		    case '&':
			this.print("&amp;");
			break;
		    default:
			this.print(ch);
			break;
		    }
		}
	    }
	}
	/**
	 * XML safe string quoted in double quotes with quoted quotes
	 * using backslash.
	 */
	public void printSafeQuoted(java.lang.String string)
	    throws java.io.IOException
	{
	    if (null != string){
		this.print('"');
		char cary[] = string.toCharArray(), ch;
		for (int cc = 0, len = cary.length; cc < len; cc++){
		    ch = cary[cc];
		    switch(ch){
		    case '"':
			this.print('\\');
			this.print(ch);
			break;
		    case '<':
			this.print("&lt;");
			break;
		    case '>':
			this.print("&gt;");
			break;
		    case '&':
			this.print("&amp;");
			break;
		    default:
			this.print(ch);
			break;
		    }
		}
		this.print('"');
	    }
	    else
		this.print("\"\"");
	}
	/**
	 * Destroy walker ({@link Formatter$State}) and close buffer.
	 */
	public void close()
	    throws java.io.IOException
	{
	    this.walker.destroy();
	    super.close();
	}
    }

    /**
     * Write DOM to XML in UTF-8 to stream.
     */
    public static class Stream 
	extends java.io.FilterOutputStream
	implements gnu.iou.dom.Formatter
    {
	private final static char[] CRLF_CHAR = {
	    '\r',
	    '\n'
	};
	private final static byte[] CRLF_BYTE = {
	    (byte)'\r',
	    (byte)'\n'
	};

	private char char_indent = CHAR_INDENT;

	private gnu.iou.dom.Formatter.State walker;

	private boolean printline;

	public Stream(java.io.OutputStream out){
	    super(out);
	    this.walker = this.ctorState();
	}

	protected gnu.iou.dom.Formatter.State ctorState(){
	    return new Formatter.State();
	}
	protected final gnu.iou.dom.Formatter.State getState(){
	    return this.walker;
	}
	public int indent(){
	    return this.walker.indent();
	}
	public char getCharIndent(){
	    return this.char_indent;
	}
	public void setCharIndent(char ch){
	    if (0 == this.indent())

		this.char_indent = ch; /*(liberal acceptance)
					*/
	    else
		throw new gnu.iou.dom.Error.State("State is not <init>, indent is not zero.");
	}
	public boolean headline(){
	    return this.walker.headline();
	}
	public void write(gnu.iou.dom.Element node)
	    throws java.io.IOException
	{
	    if (null != node)
		this.walker.write(node,this);
	}
	public void write(org.w3c.dom.Element node)
	    throws java.io.IOException
	{
	    if (null != node)
		this.walker.write(node,this);
	}
	public void write(gnu.iou.dom.Document node)
	    throws java.io.IOException
	{
	    if (null != node)
		this.walker.write(node,this);
	}
	public void write(org.w3c.dom.Document node)
	    throws java.io.IOException
	{
	    if (null != node)
		this.walker.write(node,this);
	}
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
	public void nprint(char ch, int many)
	    throws java.io.IOException
	{
	    if (1 > many)
		return;
	    else if (ch < 0x80){
		for (int cc = 0; cc < many; cc++)
		    this.write(ch);
	    }
	    else {
		char[] cary = new char[]{ch};
		byte[] bary = gnu.iou.utf8.encode(cary);
		for (int cc = 0; cc < many; cc++)
		    this.write(bary,0,bary.length);
	    }
	}
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
	public void print(int indent, String string)
	    throws java.io.IOException
	{
	    this.nprint(this.char_indent,indent);
	    this.print(string);
	}
	public void print(int indent, char ch)
	    throws java.io.IOException
	{
	    this.nprint(this.char_indent,indent);
	    this.print(ch);
	}  
	public void println(char ch)
	    throws java.io.IOException
	{
	    char[] cary = gnu.iou.chbuf.cat(ch,CRLF_CHAR);
	    byte[] bary = gnu.iou.utf8.encode(cary);
	    this.write(bary,0,bary.length);
	}
	public void println()
	    throws java.io.IOException
	{
	    this.write(CRLF_BYTE,0,2);
	}
	public void println(String string)
	    throws java.io.IOException
	{
	    char[] cary = null;
	    if (null != string)
		cary = string.toCharArray();
	    cary = gnu.iou.chbuf.cat(cary,CRLF_CHAR);
	    byte[] bary = gnu.iou.utf8.encode(cary);
	    this.write(bary,0,bary.length);
	}    
	public void println(int indent, String string)
	    throws java.io.IOException
	{
	    this.nprint(this.char_indent,indent);
	    this.println(string);
	}
	/**
	 * XML safe string encodes XML format special characters
	 * "&amp;, &lt;, &gt;" to their XML 1.0 standard character
	 * references.
	 */
	public void printSafe(java.lang.String string)
	    throws java.io.IOException
	{
	    if (null != string){
		char cary[] = string.toCharArray(), ch;
		for (int cc = 0, len = cary.length; cc < len; cc++){
		    ch = cary[cc];
		    switch(ch){
		    case '<':
			this.print("&lt;");
			break;
		    case '>':
			this.print("&gt;");
			break;
		    case '&':
			this.print("&amp;");
			break;
		    default:
			this.print(ch);
			break;
		    }
		}
	    }
	}
	/**
	 * XML safe string quoted in double quotes with quoted quotes
	 * using backslash.
	 */
	public void printSafeQuoted(java.lang.String string)
	    throws java.io.IOException
	{
	    if (null != string){
		this.print('"');
		char cary[] = string.toCharArray(), ch;
		for (int cc = 0, len = cary.length; cc < len; cc++){
		    ch = cary[cc];
		    switch(ch){
		    case '"':
			this.print('\\');
			this.print(ch);
			break;
		    case '<':
			this.print("&lt;");
			break;
		    case '>':
			this.print("&gt;");
			break;
		    case '&':
			this.print("&amp;");
			break;
		    default:
			this.print(ch);
			break;
		    }
		}
		this.print('"');
	    }
	    else
		this.print("\"\"");
	}
	/**
	 * Destroy walker ({@link Formatter$State}) and close stream.
	 */
	public void close()
	    throws java.io.IOException
	{
	    this.walker.destroy();
	    super.close();
	}
    }

    /**
     * Null formatter evaluates output like others, but output goes
     * nowhere.  
     */
    public static class Null 
	extends gnu.iou.dom.impl.Formatter
	implements gnu.iou.dom.Formatter
    {

	private gnu.iou.dom.Formatter.State walker;

	private char char_indent = CHAR_INDENT;

	public Null(){
	    super();
	    this.walker = this.ctorState();
	}

	protected gnu.iou.dom.Formatter.State ctorState(){
	    return new Formatter.State();
	}
	protected final gnu.iou.dom.Formatter.State getState(){
	    return this.walker;
	}
	/**
	 * @return Current indent
	 */
	public int indent(){
	    return this.walker.indent();
	}
	public char getCharIndent(){
	    return this.char_indent;
	}
	public void setCharIndent(char ch){
	    if (0 == this.indent())

		this.char_indent = ch; /*(liberal acceptance)
					*/
	    else
		throw new gnu.iou.dom.Error.State("State is not <init>, indent is not zero.");
	}
	/**
	 * @return Toggle headline 
	 */
	public boolean headline(){
	    return this.walker.headline();
	}
	/**
	 * NOP
	 */
	public void write(int ch)
	    throws java.io.IOException
	{}
	/**
	 * NOP
	 */
	public void write(byte[] bary, int ofs, int len)
	    throws java.io.IOException
	{}
	/**
	 * Walk element
	 */
	public void write(gnu.iou.dom.Element node)
	    throws java.io.IOException
	{
	    if (null != node)
		this.walker.write(node,this);
	}
	/**
	 * Walk element
	 */
	public void write(org.w3c.dom.Element node)
	    throws java.io.IOException
	{
	    if (null != node)
		this.walker.write(node,this);
	}
	/**
	 * Walk document
	 */
	public void write(gnu.iou.dom.Document node)
	    throws java.io.IOException
	{
	    if (null != node)
		this.walker.write(node,this);
	}
	/**
	 * Walk document
	 */
	public void write(org.w3c.dom.Document node)
	    throws java.io.IOException
	{
	    if (null != node)
		this.walker.write(node,this);
	}
	/**
	 * NOP
	 */
	public void print(char ch)
	    throws java.io.IOException
	{}
	/**
	 * NOP
	 */
	public void nprint(char ch, int many)
	    throws java.io.IOException
	{}
	/**
	 * NOP
	 */
	public void print(String string)
	    throws java.io.IOException
	{}
	/**
	 * NOP
	 */
	public void print(int indent, String string)
	    throws java.io.IOException
	{}
	/**
	 * NOP
	 */
	public void print(int indent, char ch)
	    throws java.io.IOException
	{}  
	/**
	 * NOP
	 */
	public void println(char ch)
	    throws java.io.IOException
	{}
	/**
	 * NOP
	 */
	public void println()
	    throws java.io.IOException
	{}
	/**
	 * NOP
	 */
	public void println(String string)
	    throws java.io.IOException
	{}    
	/**
	 * NOP
	 */
	public void println(int indent, String string)
	    throws java.io.IOException
	{}
	/**
	 * NOP
	 */
	public void printSafe(java.lang.String string)
	    throws java.io.IOException
	{}
	/**
	 * NOP
	 */
	public void printSafeQuoted(java.lang.String string)
	    throws java.io.IOException
	{}
	/**
	 * NOP
	 */
	public void flush()
	    throws java.io.IOException
	{}
	/**
	 * Destroy walker ({@link Formatter$State}).
	 */
	public void close()
	    throws java.io.IOException
	{
	    this.walker.destroy();
	}
    }
}
