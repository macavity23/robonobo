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
 * <p> DOM builder.</p>
 * 
 * @author jdp
 */
public abstract class Builder 
    extends gnu.iou.queue
    implements gnu.iou.dom.Builder
{

    /**
     * <p> SAX parse events for builder </p>
     * 
     * @author jdp
     */
    public final static class Sax 
	extends Builder
	implements gnu.iou.dom.Builder.Sax
    {
	private final static int NST_INIT    = Integer.MIN_VALUE;
	private final static int NST_ELEMENT = org.w3c.dom.Node.ELEMENT_NODE;
	private final static int NST_TEXT    = org.w3c.dom.Node.TEXT_NODE;
	private final static int NST_DTD     = Integer.MAX_VALUE;
	private final static int NST_ENTITY  = org.w3c.dom.Node.ENTITY_NODE;
	private final static int NST_CDATA   = org.w3c.dom.Node.CDATA_SECTION_NODE;
	private final static int NST_COMMENT = org.w3c.dom.Node.COMMENT_NODE;


	protected org.xml.sax.Locator locator;

	private int state = NST_INIT;

	public Sax(Document doc){
	    super(doc);
	}

	public void destroy(){
	    this.locator = null;
	    super.destroy();
	}
	public org.xml.sax.Locator locator(){
	    if (null == this.locator)
		throw new gnu.iou.dom.Error.State("destroyed");
	    else
		return this.locator;
	}
	public org.xml.sax.Locator locator(boolean exc){
	    if (null == this.locator){
		if (exc)
		    throw new gnu.iou.dom.Error.State("destroyed");
		else
		    return null;
	    }
	    else
		return this.locator;
	}
	/* @see org.xml.sax.ContentHandler 
	 */
	public final void setDocumentLocator(org.xml.sax.Locator locator){
	    this.locator = locator;
	}
	/* @see org.xml.sax.ContentHandler 
	 */
	public final void startDocument() throws org.xml.sax.SAXException {
	}
	/* @see org.xml.sax.ContentHandler 
	 */
	public final void endDocument() throws org.xml.sax.SAXException {

	    this.document().builderExit();
	}
	/* @see org.xml.sax.ContentHandler 
	 */
	public final void startPrefixMapping (String prefix, String uri)
	    throws org.xml.sax.SAXException
	{
	}
	/* @see org.xml.sax.ContentHandler 
	 */
	public final void endPrefixMapping (String prefix) throws org.xml.sax.SAXException {
	}
	/* @see org.xml.sax.ContentHandler 
	 */
	public final void startElement (String ns, String ln, String qn, org.xml.sax.Attributes atts)
	    throws org.xml.sax.SAXException
	{
	    this.state = NST_ELEMENT;
	    ns = Node.StrictString(ns);
	    ln = Node.StrictString(ln);
	    qn = Node.StrictString(qn);

	    gnu.iou.dom.Document ctor = this.document();
	    org.xml.sax.Locator loc = this.locator(false);
	    //
	    gnu.iou.dom.Node parent = this.buildParent();
	    //
	    gnu.iou.dom.Element child = (gnu.iou.dom.Element)ctor.createElementNS(ns,qn);
	    //
	    if (null != loc){
		child.setLocSystemId(loc.getSystemId());
		child.setLocPublicId(loc.getPublicId());
		child.setLocLineNumber(loc.getLineNumber());
		child.setLocColumnNumber(loc.getColumnNumber());
	    }
	    //
	    this.push(child);
	    //
	    if (null != atts){
		int len = atts.getLength();
		if (0 < len){
		    org.w3c.dom.NamedNodeMap attributes = child.getAttributes();

		    String at_ns, at_qn;
		    gnu.iou.dom.Attr dat;
		    attlist:
		    for (int cc = 0; cc < len; cc++){
			at_ns = atts.getURI(cc);
			at_qn = atts.getQName(cc);
			if ( null == at_qn || 1 > at_qn.length()){
			    at_qn = atts.getLocalName(cc);
			    if ( null == at_qn || 1 > at_qn.length())
				continue attlist;
			}
			dat = (gnu.iou.dom.Attr)ctor.createAttributeNS(at_ns,at_qn);
			dat.setParentNode(child);
			dat.setNodeValue(atts.getValue(cc));
			dat.setType(atts.getType(cc));
			attributes.setNamedItem(dat);
		    }
		}
	    }
	    parent.appendChild(child);
	}
	/* @see org.xml.sax.ContentHandler 
	 */
	public final void endElement (String ns, String ln, String qn)
	    throws org.xml.sax.SAXException
	{
	    this.state = NST_INIT;

	    gnu.iou.dom.Element elem = this.pop( ns, qn);
	    if (elem instanceof gnu.iou.dom.Builder.Post){
		gnu.iou.dom.Builder.Post post = (gnu.iou.dom.Builder.Post)elem;
		post.buildpost();
	    }
	}
	/* @see org.xml.sax.ContentHandler 
	 */
	public final void characters (char src[], int ofs, int len)
	    throws org.xml.sax.SAXException
	{
	    gnu.iou.dom.Node last = this.buildLast();
	    if (last instanceof gnu.iou.dom.CharacterData && 
		this.state == last.getNodeType())
	    {
		gnu.iou.dom.CharacterData node = (gnu.iou.dom.CharacterData)last;
		if (null != src && 0 < len)
		    node.appendData(new String(src,ofs,len));
	    }
	    else {
		String string = StringNotWS(src,ofs,len);
		if (null != string){
		    gnu.iou.dom.Node parent = this.buildParent();
		    gnu.iou.dom.Node node;
		    String data = new String(src,ofs,len);
		    switch (this.state){
		    case NST_CDATA:
			node = (gnu.iou.dom.Node)this.document().createCDATASection(data);
			break;
		    case NST_COMMENT:
			node = (gnu.iou.dom.Node)this.document().createComment(data);
			break;
		    default:
		    case NST_INIT:
			this.state = NST_TEXT;
			//(fall-through)
		    case NST_TEXT:
			node = (gnu.iou.dom.Node)this.document().createTextNode(data);
			break;
		    }
		    parent.appendChild(node);
		}
	    }
	}
	/* @see org.xml.sax.ContentHandler 
	 */
	public final void ignorableWhitespace (char src[], int ofs, int len)
	    throws org.xml.sax.SAXException
	{
	    String string = new String(src,ofs,len);
	}
	/* @see org.xml.sax.ContentHandler 
	 */
	public final void processingInstruction (String target, String data)
	    throws org.xml.sax.SAXException
	{
	    gnu.iou.dom.Node parent = this.buildParent();
	    org.w3c.dom.ProcessingInstruction node =
		this.document().createProcessingInstruction(target,data);
	    parent.appendChild(node);
	}
	/* @see org.xml.sax.ContentHandler 
	 */
	public final void skippedEntity (String name) 
	    throws org.xml.sax.SAXException
	{
	    gnu.iou.dom.Node parent = this.buildParent();
	    org.w3c.dom.EntityReference node =
		this.document().createEntityReference(name);
	    parent.appendChild(node);
	}
	/* @see org.xml.sax.ext.LexicalHandler
	 */
	public final void startDTD( String name, String pid, String sid)
	    throws org.xml.sax.SAXException 
	{
	    // 	    this.state = NST_DTD;
	}
	/* @see org.xml.sax.ext.LexicalHandler
	 */
	public final void endDTD() throws org.xml.sax.SAXException {
	    // 	    this.state = NST_INIT;
	}
	/* @see org.xml.sax.ext.LexicalHandler
	 */
	public final void startEntity( String name) throws org.xml.sax.SAXException {
	    // 	    this.state = NST_ENTITY;
	}
	/* @see org.xml.sax.ext.LexicalHandler
	 */
	public final void endEntity( String name) throws org.xml.sax.SAXException {
	    // 	    this.state = NST_INIT;
	}
	/* @see org.xml.sax.ext.LexicalHandler
	 */
	public final void startCDATA() throws org.xml.sax.SAXException {
	    this.state = NST_CDATA;
	}
	/* @see org.xml.sax.ext.LexicalHandler
	 */
	public final void endCDATA() throws org.xml.sax.SAXException {
	    this.state = NST_INIT;
	}
	/* @see org.xml.sax.ext.LexicalHandler
	 */
	public final void comment (char src[], int ofs, int len) throws org.xml.sax.SAXException {
	    gnu.iou.dom.Node last = this.buildLast();
	    if (last instanceof gnu.iou.dom.Comment){
		gnu.iou.dom.Comment node = (gnu.iou.dom.Comment)last;
		if (null != src && 0 < len)
		    node.appendData(new String(src,ofs,len));
	    }
	    else {
		gnu.iou.dom.Node parent = this.buildParent();
		org.w3c.dom.Node node;
		if (null != src && 0 < len)
		    node = this.document().createComment(new String(src,ofs,len));
		else
		    throw new gnu.iou.dom.Error.State("xmlp-bug");

		parent.appendChild(node);
	    }
	}

    }

    private final static boolean USE_STACK = true;

    protected gnu.iou.dom.Document doc;

    private gnu.iou.queue dom;

    protected Builder(gnu.iou.dom.Document doc){
	super(USE_STACK);

	if (null == doc)
	    throw new gnu.iou.dom.Error.Argument();
	else {
	    this.doc = doc;

	    if (doc instanceof gnu.iou.dom.Builder.Binding){
		this.dom = new gnu.iou.queue(USE_STACK);
		this.dom.push(this,doc);
	    }
	}
    }

    public gnu.iou.dom.Builder cloneBuilder(gnu.iou.dom.Document newp){
	try {
	    Builder clone = (Builder)this.clone();
	    clone.doc = newp;
	    return clone;
	}
	catch (java.lang.CloneNotSupportedException cns){
	    throw new gnu.iou.dom.Error.State();
	}
    }
    public final gnu.iou.dom.Document document(){
	if (null == this.doc)
	    throw new gnu.iou.dom.Error.State("document:destroyed");
	else
	    return this.doc;
    }
    public void destroy(){
	this.doc =  null;
	super.clear();
	if (null != this.dom)
	    try {
		this.dom.clear();
	    }
	    finally {
		this.dom = null;
	    }
    }
    public gnu.iou.dom.Element buildCurrent(boolean exc){
	gnu.iou.dom.Element elem = 
	    (gnu.iou.dom.Element)this.peek(this.doc);
	if (exc){
	    if (null == elem)
		throw new gnu.iou.dom.Error.State("Empty build stack");
	    else
		return elem;
	}
	else
	    return elem;
    }
    public gnu.iou.dom.Node buildLast(){
	gnu.iou.dom.Node current = this.buildParent();
	if (current.hasChildNodes()){
	    org.w3c.dom.NodeList children = current.getChildNodes();
	    return (gnu.iou.dom.Node)children.item(children.getLength()-1);
	}
	else
	    return current;
    }
    public gnu.iou.dom.Node buildParent(){
	gnu.iou.dom.Node parent = this.buildCurrent(false);
	if (null == parent)
	    return this.document();
	else
	    return parent;
    }
    public gnu.iou.dom.Element push(gnu.iou.dom.Element elem){
	this.push(this.doc,elem);
	//
	if (elem instanceof gnu.iou.dom.Builder.Binding){
	    if (null == this.dom)
		this.dom = new gnu.iou.queue(USE_STACK);
	    this.dom.push(this,elem);
	}
	//
	return elem;
    }
    public gnu.iou.dom.Element pop(String ns, String qn){
	gnu.iou.dom.Element re = 
	    (gnu.iou.dom.Element)this.pop(this.doc);
	//
	if (null != this.dom && re == this.dom.peek(this))
	    this.dom.pop(this);
	//
	if (re.getNodeName2().equals(qn))
	    return re;
	else
	    throw new gnu.iou.dom.Error.State("basic-document:build-stack-order");
    }
    public final gnu.iou.dom.Builder.Binding lastBinding(){
	if (null == this.dom)
	    return null;
	else
	    return (gnu.iou.dom.Builder.Binding)this.dom.peek(this);
    }
    public final gnu.iou.dom.Builder.Binding prevBinding(){
	if (null == this.dom)
	    return null;
	else
	    return (gnu.iou.dom.Builder.Binding)this.dom.peek(this,1);
    }
    public final gnu.iou.dom.Builder.Binding popBinding(){
	if (null != this.dom)
	    return (gnu.iou.dom.Builder.Binding)this.dom.pop(this.doc);
	else
	    return null;
    }
    /**
     * <p> Unicode whitespace is defined as any of the following characters.
     * 
     * <pre>
     * 0009- 000D Control-0009, ..., Control-000D
     * 0020       SPACE
     * 0085       Control-0085
     * 00A0       NON-BREAKING SPACE
     * 1680       OGHAM SPACE MARK
     * 180E       MONGOLIAN VOWEL SEPARATOR
     * 2000- 200A EN QUAD, ..., HAIR SPACE
     * 2028       LINE SEPARATOR
     * 2029       PARAGRAPH SEPARATOR
     * 202F       NARROW NON-BREAKING SPACE
     * 205F       MEDIUM MATHEMATICAL SPACE
     * 3000       IDEOGRAPHIC SPACE
     * </pre>
     * </p>
     * 
     * @return NULL if src is only whitespace (SP, TB, CR, LF,
     * plus Unicode whitespace), otherwise original string.
     */
    public final static String StringNotWS( char[] src, int ofs, int len){
	for (int cc = ofs, clen = (ofs+len); cc < clen; cc++){
	    switch(src[cc]){
	    case 0x0009:
	    case 0x000A:
	    case 0x000B:
	    case 0x000C:
	    case 0x000D:
	    case 0x0020:
	    case 0x0085:
	    case 0x1680:
	    case 0x180E:
	    case 0x2000:
	    case 0x2001:
	    case 0x2002:
	    case 0x2003:
	    case 0x2004:
	    case 0x2005:
	    case 0x2006:
	    case 0x2007:
	    case 0x2008:
	    case 0x2009:
	    case 0x200A:
	    case 0x2028:
	    case 0x2029:
	    case 0x202F:
	    case 0x205F:
	    case 0x3000:
		break;
	    default:
		/*
		 * At the first non-whitespace character, return
		 * the string verbatum.
		 */
		return new String(src,ofs,len);
	    }
	}
	/*
	 * Just whitespace, return null.
	 */
	return null;
    }
}
