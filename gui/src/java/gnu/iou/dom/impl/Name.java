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
 * <p> Immutable node name </p>
 * 
 * @author jdp
 */
public class Name 
    implements gnu.iou.dom.Name
{
    private final java.lang.String ns;
    private final java.lang.String pr;
    private final java.lang.String ln;
    private final java.lang.String qn;
    private final int hash;
    private final short type;
    private java.lang.String classname;

    /**
     * Convenience for lookup keys: construct a new node name with
     * neither namespace nor type.
     * 
     * @param qn Node qualified- name (prefix optional) is optional
     */
    public Name(java.lang.String qn){
	this(STR_NIL,qn);
    }
    /**
     * Convenience for lookup keys: construct a new node name with no
     * type.
     * 
     * @param ns Node namespace URI is optional
     * @param qn Node qualified- name (prefix optional) is optional
     */
    public Name(java.lang.String ns, java.lang.String qn){
	this(TYPE_NIL,ns,qn);
    }
    /**
     * Construct a proper node name with type, namespace and name.
     * 
     * @param type Node type is important: when a qname is missing the
     * default is provided based on the type.
     * @param ns Node namespace URI is optional
     * @param qn Node qualified- name (prefix optional) is optional
     * 
     * @exception java.lang.IllegalArgumentException For input
     * parameters missing both QName string and a recognized type
     * value.
     */
    public Name(short type, java.lang.String ns, java.lang.String qn)
	throws java.lang.IllegalArgumentException
    {
	super();
	if (null == qn)
	    switch(type){
	    case org.w3c.dom.Node.ATTRIBUTE_NODE:
		qn = "Attr";
		break;
	    case org.w3c.dom.Node.CDATA_SECTION_NODE:
		qn = "CDATASection";
		break;
	    case org.w3c.dom.Node.COMMENT_NODE:
		qn = "Comment";
		break;
	    case org.w3c.dom.Node.DOCUMENT_NODE:
		qn = "Document";
		break;
	    case org.w3c.dom.Node.DOCUMENT_FRAGMENT_NODE:
		qn = "DocumentFragment";
		break;
	    case org.w3c.dom.Node.DOCUMENT_TYPE_NODE:
		qn = "DocumentType";
		break;
	    case org.w3c.dom.Node.ELEMENT_NODE:
		qn = "Element";
		break;
	    case org.w3c.dom.Node.ENTITY_NODE:
		qn = "Entity";
		break;
	    case org.w3c.dom.Node.ENTITY_REFERENCE_NODE:
		qn = "EntityReference";
		break;
	    case org.w3c.dom.Node.NOTATION_NODE:
		qn = "Notation";
		break;
	    case org.w3c.dom.Node.PROCESSING_INSTRUCTION_NODE:
		qn = "ProcessingInstruction";
		break;
	    case org.w3c.dom.Node.TEXT_NODE:
		qn = "Text";
		break;
	    default:
		throw new gnu.iou.dom.Error.State(java.lang.String.valueOf(type));
	    }
	this.type = type;
	this.ns = Node.StrictString(ns);
	this.qn = Node.StrictString(qn);
	this.pr = Node.QNamePrefixStrict(this.qn);
	this.ln = Node.QNameSuffixLiberal(this.qn);
	if (null != this.ln)
	    this.hash = this.ln.hashCode();
	else if (null != ns)
	    this.hash = this.ns.hashCode();
	else
	    this.hash = 0;
    }
    /**
     * <p> The copy constructor makes its hash code from both
     * namespace and local name (as available).  It is a distinctly
     * different hash map key from a normal name.  The input parameter
     * must have non null namespace and name values.  </p>
     * 
     * @param copy Required name object must have both namespace and
     * name values
     * 
     * @exception java.lang.IllegalArgumentException For "Null input
     * parameter.", or "Missing 'namespace'." or "Missing
     * 'local-name'.".
     */
    public Name( gnu.iou.dom.Name copy)
	throws java.lang.IllegalArgumentException
    {
	super();
	if (null != copy){
	    if (null != copy.getNamespace()){
		if (null != copy.getLocalname()){
		    this.type = copy.getType();
		    this.ns = copy.getNamespace();
		    this.qn = copy.getQname();
		    this.pr = copy.getPrefix();
		    this.ln = copy.getLocalname();
		    this.hash = (this.ns.hashCode() ^ this.ln.hashCode());
		}
		else
		    throw new gnu.iou.dom.Error.Argument("Missing 'local-name'.");
	    }
	    else if (null != copy.getLocalname()){
		this.type = copy.getType();
		this.ns = copy.getNamespace();
		this.qn = copy.getQname();
		this.pr = copy.getPrefix();
		this.ln = copy.getLocalname();
		this.hash = (this.ln.hashCode());
	    }
	    else
		throw new gnu.iou.dom.Error.Argument("Missing 'local-name'.");
	}
	else
	    throw new gnu.iou.dom.Error.Argument("Null input parameter.");
    }
    public short getType(){
	return this.type;
    }
    public java.lang.String getPrefix(){
	return this.pr;
    }
    public java.lang.String getLocalname(){
	return this.ln;
    }
    public java.lang.String getQname(){
	return this.qn;
    }
    public java.lang.String getNamespace(){
	return this.ns;
    }
    public int hashCode(){
	return this.hash;
    }
    public boolean equals(Object ano){
	if (this == ano)
	    return true;
	else if (ano instanceof gnu.iou.dom.Name){
	    gnu.iou.dom.Name anon = (gnu.iou.dom.Name)ano;
	    if (null != this.ns && null != anon.getNamespace()){
		java.lang.String this_cn = this.getClassname();
		java.lang.String anon_cn = anon.getClassname();
		if (null == this_cn || null == anon_cn)
		    return (this_cn == anon_cn);
		else
		    return (this_cn.equals(anon_cn));
	    }
	    //
	    if (null != this.ln){
		java.lang.String anon_ln = anon.getLocalname();
		if (null != anon_ln)
		    return this.ln.equals(anon_ln);
		else
		    return false;
	    }
	    else
		return (null == anon.getLocalname());
	}
	else if (ano instanceof java.lang.String){
	    if (null == this.ln){
		if (null != this.ns)/*(matches ctor hashing)
				     */
		    return this.ns.equals((java.lang.String)ano);
		else
		    return false;
	    }
	    else if (this.ln.equals(ano))
		return true;
	    else if (this.qn.equals(ano))
		return true;
	    else
		return false;
	}
	else
	    return false;
    }
    public java.lang.String getPackage(){
	return Node.Package(this.ns);
    }
    public java.lang.String getClassname(java.lang.String prefix){
	    
	return chbuf.cat(prefix,Node.CamelCase(this.ln));
    }
    public java.lang.String getNormal(){
	return this.getClassname();
    }
    public java.lang.String getClassname(){
	if (null == this.classname){
	    if (null != this.ln){
		java.lang.String pkg = this.getPackage();
		java.lang.String cla = Node.CamelCase(this.ln);
		if (null != pkg)
		    this.classname = chbuf.cat(pkg,".",cla);
		else
		    this.classname = cla;
	    }
	    else
		return null;
	}
	return this.classname;
    }
    public java.lang.String getClassnameInner(java.lang.String inner){
	if (null != inner){
	    java.lang.String cla = this.getClassname();
	    return chbuf.cat(cla,"$",Node.CamelCase(inner));
	}
	else
	    return null;
    }
    public java.lang.String getClassnameUnder(java.lang.String inner){
	if (null != inner){
	    java.lang.String cla = this.getClassname();
	    return chbuf.cat(cla,".",Node.CamelCase(inner));
	}
	else
	    return null;
    }
}
