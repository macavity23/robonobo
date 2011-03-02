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

import gnu.iou.chbuf;

/**
 * 
 * @author jdp
 */
public class Document
    extends Node
    implements gnu.iou.dom.Document
{
    private gnu.iou.dom.DocumentType      document_doctype;
    private gnu.iou.dom.DOMImplementation document_domimplementation;
    private gnu.iou.dom.Builder           document_builder;
    private gnu.iou.dom.NodeList          document_children;
    private gnu.iou.dom.io.Source         document_source;
    private java.lang.String              document_uri;

    public Document(){
        super(DOC_NIL,new Name(org.w3c.dom.Node.DOCUMENT_NODE,STR_NIL,"Document"));
    }
    public Document(org.w3c.dom.Document owner, gnu.iou.dom.Name nname){
        super(DOC_NIL,nname);
    }

    public void destroy(){
	if (this.hasChildNodes()){
	    org.w3c.dom.NodeList children = this.getChildNodes();
	    gnu.iou.dom.Node child;
	    for (int cc = 0, len = children.getLength(); cc < len; cc++){
		child = (gnu.iou.dom.Node)children.item(cc);
		if (this == child.getParentNode())
		    child.destroy();
	    }
	}
	this.document_children = null;
	if (null != this.document_source){
	    this.document_source.destroy();
	    this.document_source = null;
	}
	super.destroy();
    }
    public final gnu.iou.dom.io.Source getSource(){
	return this.document_source;
    }
    public final void setSource(gnu.iou.dom.io.Source src){
	this.document_source = src;;
    }
    protected gnu.iou.dom.NodeList newChildNodes(){
	return new NodeList(this);
    }
    public final gnu.iou.dom.NodeList getChildNodes2(){
	if (null == this.document_children)
	    this.document_children = this.newChildNodes();
	return this.document_children;
    }
    public final boolean hasChildNodes(){
	return (null != this.document_children && 0 < this.document_children.getLength());
    }
    public final void builderExit(){
	if (null != this.document_builder)
	    try {
		this.document_builder.destroy();
	    }
	    finally {
		this.document_builder = null;
	    }
    }
    public final gnu.iou.dom.Builder.Sax builderEnterSax(){
	if (null != this.document_builder)
	    throw new gnu.iou.dom.Error.State();
	else 
	    return (gnu.iou.dom.Builder.Sax)(this.document_builder = new Builder.Sax(this));
    }
    public final gnu.iou.dom.Builder.Sax builderSax(boolean exc){
	gnu.iou.dom.Builder re = this.builder(exc);
	if (re instanceof gnu.iou.dom.Builder.Sax)
	    return (gnu.iou.dom.Builder.Sax)re;
	else if (exc)
	    throw new gnu.iou.dom.Error.State();
	else
	    return null;
    }
    public final gnu.iou.dom.Builder builder(boolean exc){
	if (null != this.document_builder)
	    return this.document_builder;
	else if (exc)
	    throw new gnu.iou.dom.Error.State();
	else
	    return null;
    }
    public final org.w3c.dom.DocumentType getDoctype(){
	return this.document_doctype;
    }
    public final void setDoctype(gnu.iou.dom.DocumentType doctype){
	this.document_doctype = doctype;
    }
    public final org.w3c.dom.DOMImplementation getImplementation(){
	return this.document_domimplementation;
    }
    public final void setImplementation(gnu.iou.dom.DOMImplementation impl){
	this.document_domimplementation = impl;
    }
    public final org.w3c.dom.Element createElement(String qn)
	throws org.w3c.dom.DOMException
    {
	gnu.iou.dom.Element elem = (gnu.iou.dom.Element)this.create(org.w3c.dom.Node.ELEMENT_NODE,STR_NIL,qn);
	return elem;
    }
    public final gnu.iou.dom.Element createElement(gnu.iou.dom.Name name){
	gnu.iou.dom.Element elem = (gnu.iou.dom.Element)this.create(org.w3c.dom.Node.ELEMENT_NODE,name.getNamespace(),name.getQname());
	return elem;
    }
    public final org.w3c.dom.Element createElementNS(String ns, String qn)
	throws org.w3c.dom.DOMException
    {
	gnu.iou.dom.Element elem = (gnu.iou.dom.Element)this.create(org.w3c.dom.Node.ELEMENT_NODE,ns,qn);
	return elem;
    }
    public final org.w3c.dom.DocumentFragment createDocumentFragment(){
	return (org.w3c.dom.DocumentFragment)this.create(org.w3c.dom.Node.DOCUMENT_FRAGMENT_NODE,STR_NIL,STR_NIL);
    }
    public final org.w3c.dom.Text createTextNode(String text){
	gnu.iou.dom.Text node = 
	    (gnu.iou.dom.Text)this.create(org.w3c.dom.Node.TEXT_NODE,STR_NIL,STR_NIL);
	if (null != text)
	    node.setData(text);
	return node;
    }
    public final org.w3c.dom.Comment createComment(String text){
	gnu.iou.dom.Comment node = 
	    (gnu.iou.dom.Comment)this.create(org.w3c.dom.Node.COMMENT_NODE,STR_NIL,STR_NIL);
	if (null != text)
	    node.setData(text);
	return node;
    }
    public final org.w3c.dom.CDATASection createCDATASection(String text)
	throws org.w3c.dom.DOMException
    {
	gnu.iou.dom.CDATASection node = 
	    (gnu.iou.dom.CDATASection)this.create(org.w3c.dom.Node.CDATA_SECTION_NODE,STR_NIL,STR_NIL);
	if (null != text)
	    node.setData(text);
	return node;
    }
    public final org.w3c.dom.ProcessingInstruction createProcessingInstruction(String target, String data)
	throws org.w3c.dom.DOMException
    {
	gnu.iou.dom.ProcessingInstruction pi = 
	    (gnu.iou.dom.ProcessingInstruction)this.create(org.w3c.dom.Node.PROCESSING_INSTRUCTION_NODE,STR_NIL,STR_NIL);
	pi.setTarget(target);
	pi.setData(data);
	return pi;
    }
    public final org.w3c.dom.Attr createAttribute(String name)
	throws org.w3c.dom.DOMException
    {
	gnu.iou.dom.Attr attr = 
	    (gnu.iou.dom.Attr)this.create(org.w3c.dom.Node.ATTRIBUTE_NODE,STR_NIL,name);
	return attr;
    }
    public final org.w3c.dom.Attr createAttributeNS(String ns, String qn)
	throws org.w3c.dom.DOMException
    {
	gnu.iou.dom.Attr attr = 
	    (gnu.iou.dom.Attr)this.create(org.w3c.dom.Node.ATTRIBUTE_NODE,ns,qn);
	return attr;
    }
    public final org.w3c.dom.EntityReference createEntityReference(String name)
	throws org.w3c.dom.DOMException
    {
	gnu.iou.dom.EntityReference entref = 
	    (gnu.iou.dom.EntityReference)this.create(org.w3c.dom.Node.ENTITY_REFERENCE_NODE,STR_NIL,name);
	return entref;
    }
    public final gnu.iou.dom.Node create(short type, String ns, String qn){
	gnu.iou.dom.Name nname = new gnu.iou.dom.impl.Name(type,ns,qn);
	gnu.iou.dom.Builder builder = this.builder(false);
	gnu.iou.dom.Builder.Binding builder_bind = null;
	if (null != builder)
	    builder_bind = builder.lastBinding();
	return this.create(nname,builder_bind);
    }
    public final gnu.iou.dom.Node create(gnu.iou.dom.Name nname, gnu.iou.dom.Builder.Binding bind){
	if (null == nname || 1 > nname.getType())
	    throw new gnu.iou.dom.Error.Argument("Bad name");
	//
	if (bind instanceof gnu.iou.dom.Builder.Binding.Map){
	    String clan = ((gnu.iou.dom.Builder.Binding.Map)bind).map(nname);
	    if (null != clan){
		try {
		    return this.instantiateNode(clan,nname);
		}
		catch (java.lang.ClassNotFoundException cnf){
		    java.lang.IllegalStateException ilst = new gnu.iou.dom.Error.State(clan);
		    ilst.initCause(cnf);
		    throw ilst;
		}
	    }
	}
	//
	if (bind instanceof gnu.iou.dom.Builder.Binding.Override){
	    /*
	     * Override node binding
	     */
	    String pkgn = ((gnu.iou.dom.Builder.Binding.Override)bind).overridePrefix();
	    String clan = nname.getClassname(pkgn);
	    try {
		return this.instantiateNode(clan,nname);
	    }
	    catch (java.lang.ClassNotFoundException cnf){
		try {
		    switch (nname.getType()){
		    case ELEMENT_NODE:
			clan = chbuf.cat(pkgn,"Element");
			return this.instantiateNode(clan,nname);
		    case ATTRIBUTE_NODE:
			clan = chbuf.cat(pkgn,"Attr");
			return this.instantiateNode(clan,nname);
		    default:
			break;
		    }
		}
		catch (java.lang.ClassNotFoundException cnf2){
		}
		catch (java.lang.IllegalStateException ilst){
		}
	    }
	    catch (java.lang.IllegalStateException ilst){
	    }
	}
	try {
	    /*
	     * Normal node binding
	     */
	    String clan = nname.getClassname();
	    return this.instantiateNode(clan,nname);
	}
	catch (java.lang.ClassNotFoundException cnf){
	}
	catch (java.lang.IllegalStateException ilst){
	}
	catch (java.lang.IllegalArgumentException clan){
	}
	if (bind instanceof gnu.iou.dom.Builder.Binding.Special){
	    String pkgn = ((gnu.iou.dom.Builder.Binding.Special)bind).specialPrefix();
	    String clan = nname.getClassname(pkgn);
	    try {
		/*
		 * Special node binding
		 */
		return this.instantiateNode(clan,nname);
	    }
	    catch (java.lang.ClassNotFoundException cnf){
		try {
		    switch (nname.getType()){
		    case ELEMENT_NODE:
			clan = chbuf.cat(pkgn,"Element");
			return this.instantiateNode(clan,nname);

		    case ATTRIBUTE_NODE:
			clan = chbuf.cat(pkgn,"Attr");
			return this.instantiateNode(clan,nname);

		    default:
			break;
		    }
		}
		catch (java.lang.ClassNotFoundException cnf2){
		}
		catch (java.lang.IllegalStateException ilst){
		}
	    }
	    catch (java.lang.IllegalStateException ilst){
	    }
	}
	/*
	 * Default node binding
	 */
	switch(nname.getType()){
	case ATTRIBUTE_NODE:
	    return new Attr(this,nname);
	case CDATA_SECTION_NODE:
	    return new CDATASection(this,nname);
	case COMMENT_NODE:
	    return new Comment(this,nname);
	case DOCUMENT_NODE:
	    return new Document(this,nname);
	case DOCUMENT_FRAGMENT_NODE:
	    return new DocumentFragment(this,nname);
	case DOCUMENT_TYPE_NODE:
	    return new DocumentType(this,nname);
	case ELEMENT_NODE:
	    return new Element(this,nname);
	case ENTITY_NODE:
	    return new Entity(this,nname);
	case ENTITY_REFERENCE_NODE:
	    return new EntityReference(this,nname);
	case NOTATION_NODE:
	    return new Notation(this,nname);
	case PROCESSING_INSTRUCTION_NODE:
	    return new ProcessingInstruction(this,nname);
	case TEXT_NODE:
	    return new Text(this,nname);
	default:
	    throw new gnu.iou.dom.Error.State(String.valueOf(nname.getType()));
	}
    }
    public final gnu.iou.dom.Node instantiateNode(String classname,
						  gnu.iou.dom.Name nname)
	throws java.lang.ClassNotFoundException
    {
	if (null == classname || 1 > classname.length())
	    throw new gnu.iou.dom.Error.Argument("Bad class name");
	else if (null == nname || 1 > nname.getType())
	    throw new gnu.iou.dom.Error.Argument("Bad node name");
	else {
	    Object[] argv = {
		this,
		nname
	    };
	    java.lang.Class clas = java.lang.Class.forName(classname);
	    switch(nname.getType()){
	    case ATTRIBUTE_NODE:
		if (AttrClass.isAssignableFrom(clas))
		    return (Node)NewInstance(clas,argv);
		else
		    break;
	    case CDATA_SECTION_NODE:
		if (CDATASectionClass.isAssignableFrom(clas))
		    return (Node)NewInstance(clas,argv);
		else
		    break;
	    case COMMENT_NODE:
		if (CommentClass.isAssignableFrom(clas))
		    return (Node)NewInstance(clas,argv);
		else
		    break;
	    case DOCUMENT_NODE:
		if (DocumentClass.isAssignableFrom(clas))
		    return (Node)NewInstance(clas,argv);
		else
		    break;
	    case DOCUMENT_FRAGMENT_NODE:
		if (DocumentFragmentClass.isAssignableFrom(clas))
		    return (Node)NewInstance(clas,argv);
		else
		    break;
	    case DOCUMENT_TYPE_NODE:
		if (DocumentTypeClass.isAssignableFrom(clas))
		    return (Node)NewInstance(clas,argv);
		else
		    break;
	    case ELEMENT_NODE:
		if (ElementClass.isAssignableFrom(clas))
		    return (Node)NewInstance(clas,argv);
		else
		    break;
	    case ENTITY_NODE:
		if (EntityClass.isAssignableFrom(clas))
		    return (Node)NewInstance(clas,argv);
		else
		    break;
	    case ENTITY_REFERENCE_NODE:
		if (EntityReferenceClass.isAssignableFrom(clas))
		    return (Node)NewInstance(clas,argv);
		else
		    break;
	    case NOTATION_NODE:
		if (NotationClass.isAssignableFrom(clas))
		    return (Node)NewInstance(clas,argv);
		else
		    break;
	    case PROCESSING_INSTRUCTION_NODE:
		if (ProcessingInstructionClass.isAssignableFrom(clas))
		    return (Node)NewInstance(clas,argv);
		else
		    break;
	    case TEXT_NODE:
		if (TextClass.isAssignableFrom(clas))
		    return (Node)NewInstance(clas,argv);
		else
		    break;
	    default:
		throw new gnu.iou.dom.Error.State(String.valueOf(nname.getType()));
	    }
	    throw new gnu.iou.dom.Error.State(classname);
	}
    }

    /**
     * @since DOM Level 3
     * @since Java 1.5
     */
    public final String getInputEncoding()
    {
        throw new org.w3c.dom.DOMException(org.w3c.dom.DOMException.NOT_SUPPORTED_ERR,null);
    }
    /**
     * @since DOM Level 3
     * @since Java 1.5
     */
    public final String getXmlEncoding()
    {
        throw new org.w3c.dom.DOMException(org.w3c.dom.DOMException.NOT_SUPPORTED_ERR,null);
    }
    /**
     * @since DOM Level 3
     * @since Java 1.5
     */
    public final boolean getXmlStandalone()
    {
        throw new org.w3c.dom.DOMException(org.w3c.dom.DOMException.NOT_SUPPORTED_ERR,null);
    }
    /**
     * @since DOM Level 3
     * @since Java 1.5
     */
    public final void setXmlStandalone(boolean xmlStandalone)
                                  throws org.w3c.dom.DOMException
    {
        throw new org.w3c.dom.DOMException(org.w3c.dom.DOMException.NOT_SUPPORTED_ERR,null);
    }
    /**
     * @since DOM Level 3
     * @since Java 1.5
     */
    public final String getXmlVersion()
    {
        throw new org.w3c.dom.DOMException(org.w3c.dom.DOMException.NOT_SUPPORTED_ERR,null);
    }
    /**
     * @since DOM Level 3
     * @since Java 1.5
     */
    public final void setXmlVersion(String xmlVersion)
                                  throws org.w3c.dom.DOMException
    {
        throw new org.w3c.dom.DOMException(org.w3c.dom.DOMException.NOT_SUPPORTED_ERR,null);
    }
    /**
     * @since DOM Level 3
     * @since Java 1.5
     */
    public final boolean getStrictErrorChecking()
    {
        throw new org.w3c.dom.DOMException(org.w3c.dom.DOMException.NOT_SUPPORTED_ERR,null);
    }
    /**
     * @since DOM Level 3
     * @since Java 1.5
     */
    public final void setStrictErrorChecking(boolean strictErrorChecking)
    {
        throw new org.w3c.dom.DOMException(org.w3c.dom.DOMException.NOT_SUPPORTED_ERR,null);
    }
    /**
     * @since DOM Level 3
     * @since Java 1.5
     */
    public final String getDocumentURI()
    {
	return this.document_uri;
    }
    /**
     * @since DOM Level 3
     * @since Java 1.5
     */
    public final void setDocumentURI(String documentURI)
    {
	this.document_uri = documentURI;
    }
    /**
     * @since DOM Level 3
     * @since Java 1.5
     */
    public final org.w3c.dom.Node adoptNode(org.w3c.dom.Node source)
	throws org.w3c.dom.DOMException
    {
        throw new org.w3c.dom.DOMException(org.w3c.dom.DOMException.NOT_SUPPORTED_ERR,null);
    }
    /**
     * @since DOM Level 3
     * @since Java 1.5
     */
    public final org.w3c.dom.DOMConfiguration getDomConfig()
    {
        throw new org.w3c.dom.DOMException(org.w3c.dom.DOMException.NOT_SUPPORTED_ERR,null);
    }
    /**
     * @since DOM Level 3
     * @since Java 1.5
     */
    public final void normalizeDocument()
    {
    }
    /**
     * @since DOM Level 3
     * @since Java 1.5
     */
    public final org.w3c.dom.Node renameNode(org.w3c.dom.Node node, 
					     String ns, 
					     String qn)
	throws org.w3c.dom.DOMException
    {
// 	if (node instanceof gnu.iou.dom.Node){
// 	    gnu.iou.dom.Node gnode = (gnu.iou.dom.Node)node;
// 	    gnu.iou.dom.Name oname = gnode.getNodeName2();
// 	    gnu.iou.dom.Name nname = new gnu.iou.dom.impl.Name(node.getNodeType(),ns,qn);
// 	    gnu.iou.dom.Node parent = gnode.getParentNode2();
// 	    parent.renameChild2(nname,oname,gnode);
// 	    return node;
// 	}
// 	else
// 	    throw new org.w3c.dom.DOMException(org.w3c.dom.DOMException.WRONG_DOCUMENT_ERR,node.getClass().getName());
        throw new org.w3c.dom.DOMException(org.w3c.dom.DOMException.NOT_SUPPORTED_ERR,null);
    }

}
