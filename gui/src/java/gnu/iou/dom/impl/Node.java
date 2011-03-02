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
 * <p> DOM node implementation. </p>
 * 
 * @author jdp
 */
public abstract class Node
    extends java.lang.Object
    implements gnu.iou.dom.Node
{

    public final static java.lang.Class NIL_CLASS = null;
    public final static java.lang.Class[] NIL_CLARY = null;
    public final static java.lang.Object NIL_OBJ = null;
    public final static java.lang.Object[] NIL_OARY = null;
    private final static java.lang.Object unknown = null;

    public final static gnu.iou.dom.Element ElementFor(org.w3c.dom.Node node){
	if (null == node)
	    throw new gnu.iou.dom.Error.Argument();
	else if (node instanceof gnu.iou.dom.Element)
	    return (gnu.iou.dom.Element)node;
	else {
	    org.w3c.dom.Node parent = node.getParentNode();
	    if (parent instanceof gnu.iou.dom.Element)
		return (gnu.iou.dom.Element)parent;
	    else
		throw new gnu.iou.dom.Error.Argument();
	}
    }
    /**
     * <p> Wrapper around {@link
     * java.lang.reflect.Constructor#newInstance(java.lang.Class,java.lang.Object[])}
     * to convert exceptions to runtime exceptions.  Supports
     * arguments in subclasses of constructor parameters.  Does not
     * support a constructor with one argument of object array type.
     * Supports constructors declared in the subject class
     * exclusively.  Supports null arguments with the exception that
     * one null argument is taken for a simple or no- parameter
     * constructor. </p>
     * 
     * <p> This is intended (used) for node instantiation which uses
     * only a fraction of the relatively narrow range of capabilities
     * presented here. </p>
     * 
     * <p> The design of this tool using runtime exceptions is
     * intended for usage that includes places where exceptions are
     * not probable: i.e., a closed (known and finite) use case set
     * where an exception is a development- time event.  In such
     * usage, the exceptions may be ignored in the immediate user code
     * and may or may not be be caught on a remote level.  </p>
     * 
     * @param clas Class to instantiate
     * @param args One argument to class declared constructor, or
     * object array of arguments to class declared constructor
     * 
     * @exception java.lang.IllegalArgumentException Null class argument to this function
     * @exception java.lang.IllegalStateException Constructor not found
     * @exception java.lang.RuntimeException Error instantiating class via found constructor
     */
    public final static java.lang.Object NewInstance( java.lang.Class clas, 
						      java.lang.Object args) 
	throws java.lang.IllegalArgumentException, 
	       java.lang.IllegalStateException, 
	       java.lang.RuntimeException
    {
	if (null == clas)
	    throw new gnu.iou.dom.Error.Argument("Missing class.");
	else 
	    try {
		java.lang.Object[] argv = null;
		if (args instanceof java.lang.Object[])
		    argv = (java.lang.Object[])args;
		else if (null != args)
		    argv = new java.lang.Object[]{args};
		//
		java.lang.reflect.Constructor ctor = FindConstructor(clas,argv);
		if (null != ctor)
		    return ctor.newInstance(argv);
		else
		    throw new gnu.iou.dom.Error.State("Constructor not found.");
	    }
	    catch (java.lang.InstantiationException x){
		java.lang.IllegalStateException rex = new gnu.iou.dom.Error.State("Error calling constructor.");
		rex.initCause(x);
		throw rex;
	    }		
	    catch (java.lang.IllegalAccessException x){
		java.lang.IllegalStateException rex = new gnu.iou.dom.Error.State("Error accessing constructor.");
		rex.initCause(x);
		throw rex;
	    }
	    catch (java.lang.reflect.InvocationTargetException inv){
		java.lang.IllegalStateException rex = new gnu.iou.dom.Error.State("Constructor threw exception.");
		java.lang.Throwable x = inv.getTargetException();
		if (null != x)
		    rex.initCause(x);
		else
		    rex.initCause(inv);
		throw rex;
	    }
    }
    private final static java.lang.reflect.Constructor FindConstructor( java.lang.Class jclass, 
									java.lang.Object[] args_user)
    {
	if ( null == args_user)

	    return FindConstructor( jclass.getDeclaredConstructors(), NIL_CLARY);

	else {
	    java.lang.Object[] aary = (java.lang.Object[])args_user;
	    int len = aary.length;
	    java.lang.Class[] params = new java.lang.Class[len];
	    java.lang.Object arg;
	    for (int cc = 0; cc < len; cc++){
		arg = aary[cc];
		if (unknown == arg)
		    params[cc] = NIL_CLASS;
		else
		    params[cc] = arg.getClass();
	    }
	    return FindConstructor( jclass.getDeclaredConstructors(), params);
	}
    }
    private final static java.lang.reflect.Constructor FindConstructor( java.lang.reflect.Constructor[] list, 
									java.lang.Class[] params_user)
    {
	if ( null == list)
	    return null;
	else {
	    java.lang.reflect.Constructor test;
	    java.lang.Class test_params[], class_user, class_test;
	    int search = 0, searchl = list.length, argc;
	    int params_user_len = (null == params_user)?(0):(params_user.length);
	    search:
	    for (; search < searchl; search++){
		test = list[search];
		test_params = test.getParameterTypes();
		if (null == test_params){
		    if (null == params_user)
			return test;
		    else
			continue search;
		}
		else if (params_user_len != test_params.length)
		    continue search;
		else {
		    for ( argc = 0; argc < params_user_len; argc++){
			class_user = params_user[argc];
			class_test = test_params[argc];
			if ( unknown == class_user ||
			     class_test.isAssignableFrom(class_user))
			    continue;
			else
			    continue search;
		    }
		    return test;
		}
	    }
	    return null;
	}
    }
    public final static java.lang.String QNameSuffixStrict( java.lang.String qn){
	if (null == qn)
	    return null;
	else {
	    int idx = qn.indexOf(':');
	    if ( 0 > idx)
		return null;
	    else
		return qn.substring(idx+1);
	}
    }
    public final static java.lang.String QNameSuffixLiberal( java.lang.String qn){
	if (null == qn)
	    return null;
	else {
	    int idx = qn.indexOf(':');
	    if ( 0 > idx)
		return qn;
	    else
		return qn.substring(idx+1);
	}
    }
    public final static java.lang.String QNamePrefixLiberal( java.lang.String qn){
	if (null == qn)
	    return null;
	else {
	    int idx = qn.indexOf(':');
	    if ( 0 > idx)
		return qn;
	    else
		return qn.substring(0,idx);
	}
    }
    public final static java.lang.String QNamePrefixStrict( java.lang.String qn){
	if (null == qn)
	    return null;
	else {
	    int idx = qn.indexOf(':');
	    if ( 0 > idx)
		return null;
	    else
		return qn.substring(0,idx);
	}
    }
    /**
     * <p> Faithfully preserve input for a valid QName output. </p>
     * @param name Input
     * @return Input, dropping illegal characters in prefix and suffix
     * positions, replacing illegal characters in infix positions with
     * hyphen
     */
    public final static java.lang.String QNameFilter( java.lang.String name){
	if (null == StrictString(name))
	    return null;
	else {
	    chbuf re = new chbuf();
	    char[] string = name.toCharArray();
	    int strlen = string.length, term = (strlen-1);
	    char ch;
	    boolean mark = true;
	    for (int cc = 0; cc < strlen; cc++, mark = ((mark)||(term == cc))){
		ch = string[cc];
		switch(ch){
		case '/':
		case '+':
		case ';':
		case '<':
		case '>':
		case '?':
		case '!':
		case '@':
		case '#':
		case '$':
		case '*':
		case '%':
		case '^':
		case '&':
		case '(':
		case ')':
		case '=':
		case '|':
		case '\\':
		case '~':
		case '`':
		case '\'':
		case '"':
		case ',':
		case '[':
		case ']':
		case ' ':
		case '\t':
		case '\r':
		case '\n':
		    if (mark){
			mark = false;
			continue;
		    }
		    else {
			re.append('-');
			mark = false;
			break;
		    }
		case ':':
		    re.append(ch);
		    mark = true;
		    break;
		default:
		    re.append(ch);
		    mark = false;
		    break;
		}
	    }
	    return re.toString();
	}
    }
    /**
     * <p> Transform node local name to class local name.  Primarily
     * truncate hyphen <code>'-'</code> with hyphen- capital case
     * letter sequence, and dot <code>'.'</code> to inner class name
     * delimiter.  The following table lists examples of the
     * transformation performed by this function.
     * 
     * <pre>
     * abcdef      Abcdef
     * abc-def     AbcDef
     * abc.def     Abc$Def
     * abc:def     Abc$Def
     * abc$def     Abc$Def
     * </pre>
     * 
     * The hyphen case is extended to a broad variety of (java
     * identifier illegal) punctuation characters for the sake of
     * completeness in the definition of this function.  The XML legal
     * character dot <code>'.'</code> is employed for the denotation
     * of inner class names.  For the sake of completeness, colon
     * <code>':'</code> and dollar sign <code>'$'</code> are employed
     * or maintained respectively as the inner class name
     * delimiter. </p>
     * 
     * <p> Note that the colon case is included here, again purely for
     * the safety or completeness in the role of this function, but
     * this character is not normal to the node local name.  However,
     * it is perhaps possible through a gap implicit between the
     * specification of XML and XML Namespaces and the implementation
     * of XML and XML Namespaces by its parser. </p>
     * 
     * @param name Node local name
     * @return Class local name
     */
    public final static java.lang.String CamelCase( java.lang.String name){
	if (null == StrictString(name))
	    return null;
	else {
	    chbuf re = new chbuf();
	    char[] string = name.toCharArray();
	    int strlen = string.length;
	    char ch;
	    boolean mark = true;
	    for (int cc = 0; cc < strlen; cc++){
		ch = string[cc];
		switch(ch){
		case '-':
		case '/':
		case '+':
		case ';':
		case '<':
		case '>':
		case '?':
		case '!':
		case '@':
		case '#':
		case '*':
		case '%':
		case '^':
		case '&':
		case '(':
		case ')':
		case '=':
		case '|':
		case '\\':
		case '~':
		case '`':
		case '\'':
		case '"':
		case ',':
		case '[':
		case ']':
		    mark = true;
		    break;
		case '.':
		case ':':
		case '$':
		    re.append('$');
		    mark = true;
		    break;
		default:
		    if (mark){
			re.append(java.lang.Character.toUpperCase(ch));
			mark = false;
		    }
		    else 
			re.append(ch);
		    break;
		}
	    }
	    return re.toString();
	}
    }
    /**
     * @param ns Namespace URI
     * @return Namespace URI converted to dotted package name for
     * dynamic node binding.  Uses host and path, reversing the host
     * name components ignoring the most specific host name component
     * (e.g., "www"), and using path literally replacing slash with
     * dot.
     */
    public final static java.lang.String Package( java.lang.String ns){
	if (null == ns || 1 > ns.length())
	    return null;
	else {
	    gnu.iou.uri uri = new gnu.iou.uri(ns);
	    gnu.iou.chbuf re = new gnu.iou.chbuf();
	    java.lang.String string;
	    string = uri.getHostName();
	    if (null != string){
		java.util.StringTokenizer strtok = new java.util.StringTokenizer(string,".");
		int count = strtok.countTokens();
		java.lang.String _0, _1, _2;
		switch (count){
		case 3:
		    _0 = strtok.nextToken();
		    _1 = strtok.nextToken();
		    _2 = strtok.nextToken();
		    re.append(_2);
		    re.append('.');
		    re.append(_1);
		    break;
		case 2:
		    _0 = strtok.nextToken();
		    _1 = strtok.nextToken();
		    re.append(_1);
		    re.append('.');
		    re.append(_0);
		    break;
		case 1:
		    re.append(strtok.nextToken());
		    break;
		default:
		    java.lang.String[] list = new java.lang.String[count];
		    for (int cc = 0; cc < count; cc++)
			list[cc] = strtok.nextToken();
		    for (int rc = (count-1); 0 < rc; rc--){
			if (0 < re.length())
			    re.append('.');
			re.append(list[rc]);
		    }
		}
	    }
	    string = uri.getPath();
	    if (null != string){
		java.util.StringTokenizer strtok = new java.util.StringTokenizer(string,"/");
		while (strtok.hasMoreTokens()){
		    if (0 < re.length())
			re.append('.');
		    re.append(strtok.nextToken());
		}
	    }
	    return re.toString();
	}
    }
    public final static java.lang.String StrictString( java.lang.String s){
	if (null != s && 0 < s.length())
	    return s;
	else 
	    return null;
    }

    /**
     * @param name In CamelCase 
     * @return Convert "CamelCase" to "camel-case"
     */
    public final static java.lang.String DeCamel(java.lang.String name){
	if (null == name || 1 > name.length())
	    return null;
	else {
	    gnu.iou.chbuf dec = new gnu.iou.chbuf();
	    char str[] = name.toCharArray(), ch;
	    for (int cc = 0, len = str.length; cc < len; cc++){
		ch = str[cc];
		if (java.lang.Character.isUpperCase(ch)){
		    if (0 < cc)
			dec.append('-');
		    dec.append(java.lang.Character.toLowerCase(ch));
		}
		else
		    dec.append(ch);
	    }
	    return dec.toString();
	}
    }

    /**
     * @return A fast pseudo random number
     */
    public final static long Rand64(){
	return gnu.iou.prng.RandLong();
    }
    /**
     * @return A random hexidecimal string 
     */
    public final static java.lang.String Rand64String(){
	return gnu.iou.prng.RandLongStringHex();
    }
    /**
     * @return Prefix a random hexidecimal string with "x"
     */
    public final static java.lang.String Rand64StringX(){
	return chbuf.cat("x",gnu.iou.prng.RandLongStringHex());
    }
    /**
     * @return Prefix a random hexidecimal string with "r"
     */
    public final static java.lang.String Rand64StringR(){
	return chbuf.cat("r",gnu.iou.prng.RandLongStringHex());
    }


    private gnu.iou.dom.Name node_name;

    private org.w3c.dom.Node node_parent;

    private org.w3c.dom.Document node_document;

    private final java.lang.Long node_unique;
    private java.lang.String node_unique_string;

    private boolean node_shared = false;;

    private gnu.iou.dom.Mutex node_mutex;
 
    private java.util.BitSet node_oobattr ;

    public Node(org.w3c.dom.Document owner, gnu.iou.dom.Name name){
        super();
	this.node_document = owner;
	this.node_name = name;
	/*
	 * (1) Mask prng state, (2) reliably unique number, with (3)
	 *     one time cryptographic value (system external).
	 */
	long identity = java.lang.System.identityHashCode(this);
	identity = ((identity<<32)|(identity));
	identity ^= Rand64();
	this.node_unique = new java.lang.Long(identity);
    }
    public void destroy(){
	this.node_parent = null;
	this.node_document = null;
    }
    public gnu.iou.dom.io.Source getSource(){
	gnu.iou.dom.Document doc = this.getOwnerDocument2();
	if (null != doc)
	    return doc.getSource();
	else
	    throw new gnu.iou.dom.Error.State("node:missing-document");
    }
    public final synchronized gnu.iou.dom.Mutex getMutex(){
	if (null == this.node_mutex)
	    this.node_mutex = new Mutex();
	return this.node_mutex;
    }
    public final boolean isShared(){
	return this.node_shared;
    }
    public final boolean isSharedNot(){
	return (!this.node_shared);
    }
    public final void setShared(boolean truf){
	this.node_shared = truf;
    }
    public final boolean isOobAttribute(int attr){
	java.util.BitSet node_oobattr = this.node_oobattr;
	if (null == node_oobattr)
	    return false;//(as "bitset" default (false))
	else
	    return node_oobattr.get(attr);
    }
    public final boolean isOobAttributeNot(int attr){
	return (!this.isOobAttribute(attr));
    }
    public final void setOobAttribute(int attr, boolean value){
	java.util.BitSet node_oobattr = this.node_oobattr;
	if (null == node_oobattr){
	    node_oobattr = new java.util.BitSet();
	    this.node_oobattr = node_oobattr;
	}
	if (value)
	    node_oobattr.set(attr);
	else
	    node_oobattr.clear(attr);
    }
    public final java.lang.Long unique(){
	return this.node_unique;
    }
    public final long uniqueValue(){
	return this.node_unique.longValue();
    }
    public final java.lang.String uniqueString(){
	if (null == this.node_unique_string)
	    this.node_unique_string = java.lang.Long.toHexString(this.node_unique.longValue());
	return this.node_unique_string;
    }
    public java.lang.String getAttributeIdName(){
	return "id";
    }
    public final java.lang.String getAttributeId(){
	return this.getAttribute(this.getAttributeIdName());
    }
    public final Integer getAttributeIdInt(gnu.iou.dom.CharacterData.User user){
	return this.getAttributeInt(this.getAttributeIdName(),user);
    }
    public final gnu.iou.dom.Name getNodeName2(){
	return this.node_name;
    }
    public final void resetNodeName2(gnu.iou.dom.Name name){
	if (null == name)
	    throw new gnu.iou.dom.Error.Argument();
	else {
	    gnu.iou.dom.Name old_name = this.node_name;
	    gnu.iou.dom.Node parent = (gnu.iou.dom.Node)this.node_parent;
	    if (null != parent && parent.hasChildNodes()){
		gnu.iou.dom.NodeList siblings = parent.getChildNodes2();
		siblings.rename(name,old_name,this);
	    }
	    this.node_name = name;
	}
    }
    public final short getNodeType(){
	if (null != this.node_name)
	    return this.node_name.getType();
	else
	    return Name.TYPE_NIL;
    }
    public final java.lang.String getNodeName(){
	if (null != this.node_name)
	    return this.node_name.getQname();
	else
	    return STR_NIL;
    }
    public final java.lang.String getNamespaceURI(){
	if (null != this.node_name)
	    return this.node_name.getNamespace();
	else
	    return STR_NIL;
    }
    public final java.lang.String getPrefix(){
	if (null != this.node_name)
	    return this.node_name.getPrefix();
	else
	    return STR_NIL;
    }
    public final java.lang.String getLocalName(){
	if (null != this.node_name)
	    return this.node_name.getLocalname();
	else
	    return STR_NIL;
    }
    public final org.w3c.dom.Node getParentNode(){
	return this.node_parent;
    }
    public final gnu.iou.dom.Node getParentNode2(){

	return (gnu.iou.dom.Node)this.node_parent;
    }
    /**
     * @param pp Set once latch, reset with null.
     */
    public void setParentNode(org.w3c.dom.Node pp){
	if (null == this.node_parent)
	    this.node_parent = pp;
	else if (null == pp)
	    this.node_parent = null;
    }
    /**
     * @param pp Set always
     */
    public final void resetParentNode(org.w3c.dom.Node pp){
	this.node_parent = pp;
    }
    public final gnu.iou.dom.Document getOwnerDocument2(){
	return (gnu.iou.dom.Document)this.getOwnerDocument();
    }
    public final org.w3c.dom.Document getOwnerDocument(){
	if (null == this.node_document){
	    if (null != this.node_parent)
		return this.node_parent.getOwnerDocument();
	    else {
		short this_type = this.getNodeType();
		if (DOCUMENT_NODE == this_type || DOCUMENT_FRAGMENT_NODE == this_type)
		    return (Document)this;
	    }
	}
	return this.node_document;
    }
    public final void setOwnerDocument(org.w3c.dom.Document dd){
	if (null == this.node_document)
	    this.node_document = dd;
	else if (null == dd)
	    this.node_document = null;
    }
    public final void resetOwnerDocument(org.w3c.dom.Document dd){
	this.node_document = dd;
    }
    public final org.w3c.dom.Element getDocumentElement(){
	return this.getFirstChildElement();
    }
    public final gnu.iou.dom.Element getDocumentElement2(){
	return this.getFirstChildElement();
    }
    public final gnu.iou.dom.Element getFirstChildElement(){
	org.w3c.dom.NodeList children = this.getChildNodes();
	org.w3c.dom.Node child;
	for (int cc = 0, len = children.getLength(); cc < len; cc++){
	    child = children.item(cc);
	    if (child instanceof Element)
		return (Element)child;
	}
	return null;
    }
    public final gnu.iou.dom.Element getLastChildElement(){
	org.w3c.dom.NodeList children = this.getChildNodes();
	org.w3c.dom.Node child;
	for (int idx = (children.getLength()-1); -1 < idx; idx--){
	    child = children.item(idx);
	    if (child instanceof Element)
		return (Element)child;
	}
	return null;
    }
    public final org.w3c.dom.Node getFirstChild(){
	org.w3c.dom.NodeList children = this.getChildNodes();
	if (NodeList.LIST_NIL != children)
	    return children.item(0);
	else
	    return null;
    }
    public final java.lang.String getChildText(){
	org.w3c.dom.NodeList children = this.getChildNodes();
	if (NodeList.LIST_NIL != children){
	    org.w3c.dom.Node child = children.item(0);
	    if (null != child)
		return child.getNodeValue();
	}
	return null;
    }
    public final void setChildText(java.lang.String value){
	this.setChildData(value,null);
    }
    public void setChildData(java.lang.Object data, gnu.iou.dom.CharacterData.User user){
	org.w3c.dom.NodeList children = this.getChildNodes();
	if (NodeList.LIST_NIL != children){
	    org.w3c.dom.Node child = children.item(0);
	    gnu.iou.dom.CharacterData cdata;
	    if (null == child){
		cdata = this.createData();
		this.appendChild(cdata);
	    }
	    else if (child instanceof gnu.iou.dom.CharacterData)
		cdata = (gnu.iou.dom.CharacterData)child;
	    else {
		cdata = this.createData();
		this.insertBefore(cdata,child);
	    }
	    cdata.setData(data,user);
	    return;
	}
    }
    public java.lang.Object getChildData(int type, gnu.iou.dom.CharacterData.User user){
	org.w3c.dom.NodeList children = this.getChildNodes();
	if (NodeList.LIST_NIL != children){
	    org.w3c.dom.Node child = children.item(0);
	    if (child instanceof gnu.iou.dom.CharacterData){
		gnu.iou.dom.CharacterData cdata = (gnu.iou.dom.CharacterData)child;
		return cdata.getData(type,user);
	    }
	}
	return null;
    }
    public final org.w3c.dom.Node getLastChild(){
	org.w3c.dom.NodeList children = this.getChildNodes();
	if (NodeList.LIST_NIL != children){
	    int idx = children.getLength()-1;
	    return children.item(idx);
	}
	else
	    return null;
    }
    public final gnu.iou.dom.Node getChildById(java.lang.String id){
	if (this.hasChildNodes()){
	    org.w3c.dom.NodeList children = this.getChildNodes();
	    Node child;
	    java.lang.String atv;
	    for (int cc = 0, len = children.getLength(); cc < len; cc++){
		child = (Node)children.item(cc);
		atv = child.getAttributeId();
		if (null != atv && atv.equals(id))
		    return child;
	    }
	}
	return null;
    }
    public final gnu.iou.dom.Node getChildById(Integer id, gnu.iou.dom.CharacterData.User user){
	if (this.hasChildNodes()){
	    org.w3c.dom.NodeList children = this.getChildNodes();
	    Node child;
	    Integer atv;
	    for (int cc = 0, len = children.getLength(); cc < len; cc++){
		child = (Node)children.item(cc);
		atv = child.getAttributeIdInt(user);
		if (null != atv && atv.equals(id))
		    return child;
	    }
	}
	return null;
    }
    public final gnu.iou.dom.Node getChildByField(java.lang.String name, 
						  java.lang.Object value, 
						  CharacterData.User user)
    {
	if (null == user){
	    if (null == value)
		return this.getChildByField(name,null);
	    else if (value instanceof java.lang.String)
		return this.getChildByField(name,(java.lang.String)value);
	    else
		return this.getChildByField(name,value.toString());
	}
	else if (this.hasChildNodes()){
	    int req_type = user.typeOf(value);
	    org.w3c.dom.NodeList children = this.getChildNodes();
	    Node child;
	    java.lang.Object test;
	    for (int cc = 0, len = children.getLength(); cc < len; cc++){
		child = (Node)children.item(cc);
		test = child.getDataField(name,req_type,user);
		if (null != test && test.equals(value))
		    return child;
	    }
	}
	return null;
    }
    public final gnu.iou.dom.Node getChildByField(java.lang.String name, java.lang.String value){
	if (this.hasChildNodes()){
	    org.w3c.dom.NodeList children = this.getChildNodes();
	    Node child;
	    java.lang.String test;
	    for (int cc = 0, len = children.getLength(); cc < len; cc++){
		child = (Node)children.item(cc);
		test = child.getDataField(name);
		if (null != test && test.equals(value))
		    return child;
	    }
	}
	return null;
    }
    public final gnu.iou.dom.Node getChildByName(java.lang.String name){
	if (this.hasChildNodes()){
	    NodeList children = (NodeList)this.getChildNodes();
	    return (Node)children.get(QNameSuffixLiberal(name));
	}
	return null;
    }
    public final gnu.iou.dom.Node getChildByName(gnu.iou.dom.Name name){
	if (this.hasChildNodes()){
	    NodeList children = (NodeList)this.getChildNodes();
	    return (Node)children.get(name);
	}
	return null;
    }
    public final gnu.iou.dom.Node getChildByIndex(int idx){
	if (this.hasChildNodes()){
	    NodeList children = (NodeList)this.getChildNodes();
	    return (Node)children.value(idx);
	}
	return null;
    }
    public boolean hasChildNodes(){
	org.w3c.dom.NodeList children = this.getChildNodes();
	return (NodeList.LIST_NIL != children && 0 < children.getLength());
    }
    public boolean hasAttributes(){
	org.w3c.dom.NamedNodeMap attributes = this.getAttributes();
	return (NamedNodeMap.MAP_NIL != attributes && 0 < attributes.getLength());
    }
    public final void setDataField(java.lang.String name, java.lang.Object data, gnu.iou.dom.CharacterData.User user){
	this.setDataField(name,data,false,user);
    }
    public final void pushDataField(java.lang.String name, java.lang.Object data, gnu.iou.dom.CharacterData.User user){
	this.setDataField(name,data,true,user);
    }
    public final void setDataField(java.lang.String name, java.lang.Object data, boolean push, gnu.iou.dom.CharacterData.User user){
	org.w3c.dom.Document ctor = this.getOwnerDocument();
	boolean rname = false;
	if (null == name){
	    if (push)
		name = Rand64StringR();
	    else
		throw new gnu.iou.dom.Error.Argument("Null node name without 'push'.");
	}
	/*
	 */
	gnu.iou.dom.Node field = this.getChildByName(name);
	if (null == field || push){
	    field = (gnu.iou.dom.Node)ctor.createElement(name);
	    if (push){
		org.w3c.dom.Node first = this.getFirstChild();
		if (null != first)
		    this.insertBefore(field,first);
		else
		    this.appendChild(field);
	    }
	    else
		this.appendChild(field);
	}
	else {
	    gnu.iou.dom.Node field_child = 
		(gnu.iou.dom.Node)field.getFirstChild();
	    if (field_child instanceof gnu.iou.dom.CharacterData){
		((gnu.iou.dom.CharacterData)field_child).setData(data,user);
		return;
	    }
	}
	gnu.iou.dom.CharacterData cdata;
	if (data instanceof gnu.iou.dom.CharacterData)
	    cdata = (gnu.iou.dom.CharacterData)data;
	else {
	    cdata = this.createData();
	    cdata.setData(data,user);
	}
	field.appendChild(cdata);
    }
    public final java.lang.String getDataField(java.lang.String name){
	return (java.lang.String)this.getDataField(name,0,false,null);
    }
    public final java.lang.Object getDataField(java.lang.String name, gnu.iou.dom.CharacterData.User user){
	return this.getDataField(name,0,false,user);
    }
    public final java.lang.Object getDataField(java.lang.String name, int type, gnu.iou.dom.CharacterData.User user){
	return this.getDataField(name,type,false,user);
    }
    public final java.lang.Object popDataField(java.lang.String name, int type, gnu.iou.dom.CharacterData.User user){
	return this.getDataField(name,type,true,user);
    }
    public final java.lang.Object getDataField(java.lang.String name, int type, boolean pop, gnu.iou.dom.CharacterData.User user){
	if (this.hasChildNodes()){
	    gnu.iou.dom.NodeList children = 
		(gnu.iou.dom.NodeList)this.getChildNodes();
	    gnu.iou.dom.Node node;
	    if (null == name){
		node = (gnu.iou.dom.Node)children.item(0);
		if (pop)
		    children.remove(0);
	    }
	    else
		node = (gnu.iou.dom.Node)children.item(name);
	    /*
	     */
	    if (null == node)
		return null;
	    else if (node.hasChildNodes()){
		gnu.iou.dom.Node data =
		    (gnu.iou.dom.Node)node.getFirstChild();
		if (data instanceof gnu.iou.dom.CharacterData){
		    gnu.iou.dom.CharacterData cdata = 
			(gnu.iou.dom.CharacterData)data;
		    return cdata.getData(type,user);
		}
	    }
	}
	return null;
    }
    public final void normalize(){
	/* 
	 * Default by construction
	 */
    }
    public org.w3c.dom.Node cloneNode(boolean deep){
	try {
	    Node re = (Node)this.clone();
	    re.node_mutex = null;
	    if (deep){
		if (this.hasAttributes()){
		    gnu.iou.dom.NamedNodeMap attributes = 
			(gnu.iou.dom.NamedNodeMap)this.getAttributes();
		    gnu.iou.dom.NamedNodeMap clone_attributes = 
			attributes.cloneNamedNodeMap(re,deep);
		    re.resetAttributes(clone_attributes);
		}
		//
		if (this.hasChildNodes()){
		    gnu.iou.dom.NodeList children = this.getChildNodes2();
		    gnu.iou.dom.NodeList clone_children = 
			children.cloneNodeList(re,deep);
		    re.resetChildNodes(clone_children);
		}
	    }
	    return re;
	}
	catch (java.lang.CloneNotSupportedException cnx){
	    throw new gnu.iou.dom.Error.State();
	}
    }
    public org.w3c.dom.Node cloneNode(gnu.iou.dom.Node nparent){
	Node clone = (Node)this.cloneNode(true);
	clone.resetParentNode(nparent);
	return clone;
    }
    public final java.lang.String getTagName(){
	return this.getNodeName();
    }
    public final boolean hasAttribute(java.lang.String name){
	org.w3c.dom.NamedNodeMap attributes = this.getAttributes();
	if (NamedNodeMap.MAP_NIL != attributes)
	    return (null != attributes.getNamedItem(name));
	else
	    return false;
    }
    public final boolean hasAttributeNS(java.lang.String ns, java.lang.String qn){
	org.w3c.dom.NamedNodeMap attributes = this.getAttributes();
	if (NamedNodeMap.MAP_NIL != attributes)
	    return (null != attributes.getNamedItemNS(ns,qn));
	else
	    return false;
    }
    public final java.lang.String getAttribute(java.lang.String name){
	org.w3c.dom.Attr attr = this.getAttributeNode(name);
	if (null != attr)
	    return attr.getValue();
	else
	    return null;
    }
    public final Integer getAttributeInt(java.lang.String name, gnu.iou.dom.CharacterData.User user){
	gnu.iou.dom.Attr attr = this.getAttributeNode2(name);
	if (null != attr)
	    return (Integer)attr.getData(user.typeInteger(),user);
	else
	    return null;
    }
    public final java.lang.String getAttributeNS(java.lang.String ns, java.lang.String qn){
	return this.getAttribute(qn);
    }
    public final void setAttribute(java.lang.String name, java.lang.String value)
	throws org.w3c.dom.DOMException
    {
	if (null != name){
	    org.w3c.dom.NamedNodeMap attributes = this.getAttributes();
	    if (NamedNodeMap.MAP_NIL != attributes){
		org.w3c.dom.Document doc = this.getOwnerDocument();
		if (null != doc){
		    org.w3c.dom.Attr attr = doc.createAttribute(name);
		    attr.setValue(value);
		    attributes.setNamedItem(attr);
		    return;
		}
	    }
	    throw new org.w3c.dom.DOMException(org.w3c.dom.DOMException.NO_MODIFICATION_ALLOWED_ERR,STR_NIL);
	}
    }
    public final void setAttributeNS(java.lang.String ns, java.lang.String qn, java.lang.String value)
	throws org.w3c.dom.DOMException
    {
	if (null != qn){
	    org.w3c.dom.NamedNodeMap attributes = this.getAttributes();
	    if (NamedNodeMap.MAP_NIL != attributes){
		org.w3c.dom.Document doc = this.getOwnerDocument();
		if (null != doc){
		    org.w3c.dom.Attr attr = doc.createAttributeNS(ns,qn);
		    attr.setValue(value);
		    attributes.setNamedItem(attr);
		    return;
		}
	    }
	    throw new org.w3c.dom.DOMException(org.w3c.dom.DOMException.NO_MODIFICATION_ALLOWED_ERR,STR_NIL);
	}
    }
    public final void removeAttribute(java.lang.String name)
	throws org.w3c.dom.DOMException
    {
	if (null != name){
	    org.w3c.dom.NamedNodeMap attributes = this.getAttributes();
	    if (NamedNodeMap.MAP_NIL != attributes){
		org.w3c.dom.Node attr = attributes.removeNamedItem(name);
		return;
	    }
	    throw new org.w3c.dom.DOMException(org.w3c.dom.DOMException.NO_MODIFICATION_ALLOWED_ERR,STR_NIL);
	}
    }
    public final void removeAttributeNS(java.lang.String ns, java.lang.String qn)
	throws org.w3c.dom.DOMException
    {
	if (null != qn){
	    org.w3c.dom.NamedNodeMap attributes = this.getAttributes();
	    if (NamedNodeMap.MAP_NIL != attributes){
		org.w3c.dom.Node attr = attributes.removeNamedItemNS(ns,qn);
		return;
	    }
	    throw new org.w3c.dom.DOMException(org.w3c.dom.DOMException.NO_MODIFICATION_ALLOWED_ERR,STR_NIL);
	}
    }
    public final org.w3c.dom.Attr getAttributeNode(java.lang.String name){
	if (null != name){
	    org.w3c.dom.NamedNodeMap attributes = this.getAttributes();
	    if (NamedNodeMap.MAP_NIL != attributes)
		return (org.w3c.dom.Attr)attributes.getNamedItem(QNameSuffixLiberal(name));
	}
	return null;
    }
    public final gnu.iou.dom.Attr getAttributeNode2(java.lang.String name){
	if (null != name){
	    org.w3c.dom.NamedNodeMap attributes = this.getAttributes();
	    if (NamedNodeMap.MAP_NIL != attributes)
		return (gnu.iou.dom.Attr)attributes.getNamedItem(QNameSuffixLiberal(name));
	}
	return null;
    }
    public final gnu.iou.dom.Attr getAttributeNode(gnu.iou.dom.Name name){
	if (null != name){
	    gnu.iou.dom.NamedNodeMap attributes = this.getAttributes2();
	    if (NamedNodeMap.MAP_NIL != attributes)
		return (gnu.iou.dom.Attr)attributes.getNamedItem(name);
	}
	return null;
    }
    public gnu.iou.dom.Attr getCreateAttributeNode(java.lang.String name){
	if (null != name){
	    gnu.iou.dom.NamedNodeMap attributes = this.getAttributes2();
	    if (NamedNodeMap.MAP_NIL != attributes){
		gnu.iou.dom.Attr node = (gnu.iou.dom.Attr)attributes.getNamedItem(name);
		if (null == node){
		    node = (gnu.iou.dom.Attr)this.getOwnerDocument().createAttribute(name);
		    attributes.setNamedItem(node);
		}
		return node;
	    }
	}
	return null;
    }
    public gnu.iou.dom.Attr getCreateAttributeNode(gnu.iou.dom.Name name){
	if (null != name){
	    gnu.iou.dom.NamedNodeMap attributes = this.getAttributes2();
	    if (NamedNodeMap.MAP_NIL != attributes){
		gnu.iou.dom.Attr node = (gnu.iou.dom.Attr)attributes.getNamedItem(name);
		if (null == node){
		    node = (gnu.iou.dom.Attr)this.getOwnerDocument().createAttributeNS(name.getNamespace(),name.getQname());
		    attributes.setNamedItem(node);
		}
		return node;
	    }
	}
	return null;
    }
    public final org.w3c.dom.Attr getAttributeNodeNS(java.lang.String ns, java.lang.String qn){
	if (null != qn){
	    org.w3c.dom.NamedNodeMap attributes = this.getAttributes();
	    if (NamedNodeMap.MAP_NIL != attributes)
		return (org.w3c.dom.Attr)attributes.getNamedItemNS(ns,qn);
	}
	return null;
    }
    public final org.w3c.dom.Attr setAttributeNode(org.w3c.dom.Attr node)
	throws org.w3c.dom.DOMException
    {
	if (null != node){
	    org.w3c.dom.NamedNodeMap attributes = this.getAttributes();
	    if (NamedNodeMap.MAP_NIL != attributes)
		return (org.w3c.dom.Attr)attributes.setNamedItem(node);
	}
	return null;
    }
    public final org.w3c.dom.Attr setAttributeNodeNS(org.w3c.dom.Attr node)
	throws org.w3c.dom.DOMException
    {
	if (null != node){
	    org.w3c.dom.NamedNodeMap attributes = this.getAttributes();
	    if (NamedNodeMap.MAP_NIL != attributes)
		return (org.w3c.dom.Attr)attributes.setNamedItemNS(node);
	}
	return null;
    }
    public final org.w3c.dom.Attr removeAttributeNode(org.w3c.dom.Attr node)
	throws org.w3c.dom.DOMException
    {
	if (null != node){
	    org.w3c.dom.NamedNodeMap attributes = this.getAttributes();
	    if (NamedNodeMap.MAP_NIL != attributes)
		return (org.w3c.dom.Attr)attributes.removeNamedItem(node.getNodeName());
	}
	return null;
    }
    public final org.w3c.dom.Node appendChild(org.w3c.dom.Node node)
	throws org.w3c.dom.DOMException
    {
	if (null != node){
	    org.w3c.dom.NodeList children = this.getChildNodes();
	    if (NodeList.LIST_NIL != children && children instanceof gnu.iou.dom.NodeList){
		((gnu.iou.dom.Node)node).setParentNode(this);
		return ((gnu.iou.dom.NodeList)children).append(node);
	    }
	    else
		throw new org.w3c.dom.DOMException(org.w3c.dom.DOMException.NO_MODIFICATION_ALLOWED_ERR,STR_NIL);
	}
	return null;
    }
    public final org.w3c.dom.Node removeChild(org.w3c.dom.Node node)
	throws org.w3c.dom.DOMException
    {
	if (null != node){
	    org.w3c.dom.NodeList children = this.getChildNodes();
	    if (NodeList.LIST_NIL != children && children instanceof gnu.iou.dom.NodeList)
		return ((gnu.iou.dom.NodeList)children).remove(node);
	    else
		throw new org.w3c.dom.DOMException(org.w3c.dom.DOMException.NO_MODIFICATION_ALLOWED_ERR,STR_NIL);
	}
	return null;
    }
    public final org.w3c.dom.Node replaceChild(org.w3c.dom.Node newn, 
					       org.w3c.dom.Node oldn)
	throws org.w3c.dom.DOMException
    {
	if (null != newn){
	    org.w3c.dom.NodeList children = this.getChildNodes();
	    if (NodeList.LIST_NIL != children && children instanceof gnu.iou.dom.NodeList){
		((gnu.iou.dom.Node)newn).setParentNode(this);
		if (null != oldn)
		    return ((gnu.iou.dom.NodeList)children).replace(newn,oldn);
		else
		    return ((gnu.iou.dom.NodeList)children).append(newn);
	    }
	    else
		throw new org.w3c.dom.DOMException(org.w3c.dom.DOMException.NO_MODIFICATION_ALLOWED_ERR,STR_NIL);
	}
	return null;
    }
    public final org.w3c.dom.Node insertBefore(org.w3c.dom.Node newn, 
					       org.w3c.dom.Node oldn)
	throws org.w3c.dom.DOMException
    {
	if (null != newn){
	    org.w3c.dom.NodeList children = this.getChildNodes();
	    if (NodeList.LIST_NIL != children && children instanceof gnu.iou.dom.NodeList){
		((gnu.iou.dom.Node)newn).setParentNode(this);
		if (null != oldn)
		    return ((gnu.iou.dom.NodeList)children).insert(newn,oldn);
		else
		    return ((gnu.iou.dom.NodeList)children).append(newn);
	    }
	    else
		throw new org.w3c.dom.DOMException(org.w3c.dom.DOMException.NO_MODIFICATION_ALLOWED_ERR,STR_NIL);
	}
	return null;
    }
    public /*(unimplemented)*/ boolean isSupported(java.lang.String feature,  java.lang.String version){
	////////////////////////
	////////////////////////
	////////////////////////
	////////////////////////
	return false;
	////////////////////////
	////////////////////////
	////////////////////////
	////////////////////////
    }
    public /*(unimplemented)*/ void setPrefix(java.lang.String prefix)
	throws org.w3c.dom.DOMException
    {
	throw new org.w3c.dom.DOMException(org.w3c.dom.DOMException.NO_MODIFICATION_ALLOWED_ERR,STR_NIL);
    }
    public /*(unimplemented)*/ org.w3c.dom.Node importNode(org.w3c.dom.Node node, boolean deep)
	throws org.w3c.dom.DOMException
    {
	if (node instanceof gnu.iou.dom.Node)

	    return node.cloneNode(true);
	else
	    throw new org.w3c.dom.DOMException(org.w3c.dom.DOMException.NOT_SUPPORTED_ERR,"Method not implemented.");
    }
    public /*(unimplemented)*/ org.w3c.dom.NodeList getElementsByTagName(java.lang.String name){
	/*
	 * depends on walker->nodelist
	 */
	throw new org.w3c.dom.DOMException(org.w3c.dom.DOMException.NOT_SUPPORTED_ERR,"Method not implemented.");
    }
    public /*(unimplemented)*/ org.w3c.dom.NodeList getElementsByTagNameNS(java.lang.String ns, java.lang.String qn){
	/*
	 * depends on walker->nodelist
	 */
	throw new org.w3c.dom.DOMException(org.w3c.dom.DOMException.NOT_SUPPORTED_ERR,"Method not implemented.");
    }
    public /*(unimplemented)*/ org.w3c.dom.Element getElementById(java.lang.String id){

	throw new org.w3c.dom.DOMException(org.w3c.dom.DOMException.NOT_SUPPORTED_ERR,"Method not implemented.");
    }
    public org.w3c.dom.Node getPreviousSibling(){
	gnu.iou.dom.Node parent = this.getParentNode2();
	if (null != parent && parent.hasChildNodes()){
	    gnu.iou.dom.NodeList list = parent.getChildNodes2();
	    int idx = list.item(this);
	    if (1 > idx){
		if (0 > idx)
		    throw new org.w3c.dom.DOMException(org.w3c.dom.DOMException.HIERARCHY_REQUEST_ERR,"This node not found in parent node list.");
		else
		    return null;
	    }
	    else 
		return list.item(idx - 1);
	}
	else
	    throw new org.w3c.dom.DOMException(org.w3c.dom.DOMException.HIERARCHY_REQUEST_ERR,"Node missing parent, or parent has no children.");
    }
    public org.w3c.dom.Node getNextSibling(){
	gnu.iou.dom.Node parent = this.getParentNode2();
	if (null != parent && parent.hasChildNodes()){
	    gnu.iou.dom.NodeList list = parent.getChildNodes2();
	    int idx = list.item(this);
	    if (0 > idx)
		throw new org.w3c.dom.DOMException(org.w3c.dom.DOMException.HIERARCHY_REQUEST_ERR,"This node not found in parent node list.");
	    else 
		return list.item(idx + 1);
	}
	else
	    throw new org.w3c.dom.DOMException(org.w3c.dom.DOMException.HIERARCHY_REQUEST_ERR,"Node missing parent, or parent has no children.");
    }
    public final org.w3c.dom.NodeList getChildNodes(){
	return this.getChildNodes2();
    }
    public /*(override)*/ gnu.iou.dom.NodeList getChildNodes2(){
	return NodeList.LIST_NIL;
    }
    public final int countChildNodes(){
	return this.getChildNodes2().getLength();
    }
    public /*(override)*/ void resetChildNodes(gnu.iou.dom.NodeList children){
	throw new org.w3c.dom.DOMException(org.w3c.dom.DOMException.NOT_SUPPORTED_ERR,"Method not implemented for this node type.");
    }
    public final org.w3c.dom.NamedNodeMap getAttributes(){
	return this.getAttributes2();
    }
    public /*(override)*/ gnu.iou.dom.NamedNodeMap getAttributes2(){
	return NamedNodeMap.MAP_NIL;
    }
    public /*(override)*/ void resetAttributes(gnu.iou.dom.NamedNodeMap attributes){
	throw new org.w3c.dom.DOMException(org.w3c.dom.DOMException.NOT_SUPPORTED_ERR,"Method not implemented for this node type.");
    }
    public /*(override)*/ java.lang.String getNodeValue()
	throws org.w3c.dom.DOMException
    {
	return null;
    }
    public /*(override)*/ void setNodeValue(java.lang.String value)
	throws org.w3c.dom.DOMException
    {
	throw new org.w3c.dom.DOMException(org.w3c.dom.DOMException.NO_MODIFICATION_ALLOWED_ERR,STR_NIL);
    }
    public java.lang.String getLocSystemId(){
	return null;
    }
    public void setLocSystemId(java.lang.String sid){
	throw new org.w3c.dom.DOMException(org.w3c.dom.DOMException.NOT_SUPPORTED_ERR,"Method not implemented for this node type.");
    }
    public java.lang.String getLocPublicId(){
	return null;
    }
    public void setLocPublicId(java.lang.String pid){
	throw new org.w3c.dom.DOMException(org.w3c.dom.DOMException.NOT_SUPPORTED_ERR,"Method not implemented for this node type.");
    }
    public int getLocLineNumber(){
	return -1;
    }
    public void setLocLineNumber(int lno){
	throw new org.w3c.dom.DOMException(org.w3c.dom.DOMException.NOT_SUPPORTED_ERR,"Method not implemented for this node type.");
    }
    public int getLocColumnNumber(){
	return -1;
    }
    public void setLocColumnNumber(int cno){
	throw new org.w3c.dom.DOMException(org.w3c.dom.DOMException.NOT_SUPPORTED_ERR,"Method not implemented for this node type.");
    }
    /**
     * @since DOM Level 3
     * @since Java 1.5
     */
    public final java.lang.String getBaseURI(){
	throw new org.w3c.dom.DOMException(org.w3c.dom.DOMException.NOT_SUPPORTED_ERR,"Method not implemented.");
    }
    /**
     * @since DOM Level 3
     * @since Java 1.5
     */
    public final short compareDocumentPosition(org.w3c.dom.Node other)
	throws org.w3c.dom.DOMException
    {
	throw new org.w3c.dom.DOMException(org.w3c.dom.DOMException.NOT_SUPPORTED_ERR,"Method not implemented.");
    }
    /**
     * @since DOM Level 3
     * @since Java 1.5
     */
    public final java.lang.String getTextContent()
	throws org.w3c.dom.DOMException
    {
	return this.getNodeValue();
    }
    /**
     * @since DOM Level 3
     * @since Java 1.5
     */
    public final void setTextContent(java.lang.String text)
	throws org.w3c.dom.DOMException
    {
	this.setNodeValue(text);
    }
    /**
     * @since DOM Level 3
     * @since Java 1.5
     */
    public final boolean isSameNode(org.w3c.dom.Node other){
	throw new org.w3c.dom.DOMException(org.w3c.dom.DOMException.NOT_SUPPORTED_ERR,"Method not implemented.");
    }
    /**
     * @since DOM Level 3
     * @since Java 1.5
     */
    public final java.lang.String lookupPrefix(java.lang.String ns){
	gnu.iou.dom.Name name = this.getNodeName2();
	if (null != name && ns.equals(name.getNamespace()))
	    return name.getPrefix();
	else {
	    org.w3c.dom.Node parent = this.getParentNode();
	    if (parent instanceof gnu.iou.dom.Node)//((dom-2)||(dom-3))
		return ((gnu.iou.dom.Node)parent).lookupPrefix(ns);
	    else
		return null;
	}
    }
    /**
     * @since DOM Level 3
     * @since Java 1.5
     */
    public final boolean isDefaultNamespace(java.lang.String ns){
	gnu.iou.dom.Name name = this.getNodeName2();
	if (null != name && null != name.getNamespace())
	    return (null == name.getPrefix() && ns.equals(name.getNamespace()));
	else {
	    org.w3c.dom.Node parent = this.getParentNode();
	    if (parent instanceof gnu.iou.dom.Node)//((dom-2)||(dom-3))
		return ((gnu.iou.dom.Node)parent).isDefaultNamespace(ns);
	    else
		return false;
	}
    }
    /**
     * @since DOM Level 3
     * @since Java 1.5
     * 
     */
    public java.lang.String lookupNamespaceURI(java.lang.String prefix){
	gnu.iou.dom.Name name = this.getNodeName2();
	if (null != name && prefix.equals(name.getPrefix()))
	    return name.getNamespace();
	else {
	    org.w3c.dom.Node parent = this.getParentNode();
	    if (parent instanceof gnu.iou.dom.Node)//((dom-2)||(dom-3))
		return ((gnu.iou.dom.Node)parent).lookupNamespaceURI(prefix);
	    else
		return null;
	}
    }
    /**
     * @since DOM Level 3
     * @since Java 1.5
     */
    public final boolean isEqualNode(org.w3c.dom.Node ano){
	throw new org.w3c.dom.DOMException(org.w3c.dom.DOMException.NOT_SUPPORTED_ERR,"Method not implemented.");
    }
    /**
     * @since DOM Level 3
     * @since Java 1.5
     */
    public final java.lang.Object getFeature(java.lang.String feature, java.lang.String version){
	throw new org.w3c.dom.DOMException(org.w3c.dom.DOMException.NOT_SUPPORTED_ERR,"Method not implemented.");
    }
    /**
     * @since DOM Level 3
     * @since Java 1.5
     */
    public final java.lang.Object setUserData(java.lang.String name, java.lang.Object value, org.w3c.dom.UserDataHandler handle){
	throw new org.w3c.dom.DOMException(org.w3c.dom.DOMException.NOT_SUPPORTED_ERR,"Method not implemented.");
    }
    /**
     * @since DOM Level 3
     * @since Java 1.5
     */
    public final java.lang.Object getUserData(java.lang.String name){
	throw new org.w3c.dom.DOMException(org.w3c.dom.DOMException.NOT_SUPPORTED_ERR,"Method not implemented.");
    }
    /**
     * @see org.w3c.dom.Attr
     * @see org.w3c.dom.Element
     * @since DOM Level 3
     * @since Java 1.5
     */
    public final org.w3c.dom.TypeInfo getSchemaTypeInfo()
    {
        throw new org.w3c.dom.DOMException(org.w3c.dom.DOMException.NOT_SUPPORTED_ERR,"Method not implemented.");
    }
    /**
     * @see org.w3c.dom.Element
     * @since DOM Level 3
     * @since Java 1.5
     */
    public final void setIdAttribute(java.lang.String name, boolean isid)
	throws org.w3c.dom.DOMException
    {
        throw new org.w3c.dom.DOMException(org.w3c.dom.DOMException.NOT_SUPPORTED_ERR,"Method not implemented.");
    }
    /**
     * @see org.w3c.dom.Element
     * @since DOM Level 3
     * @since Java 1.5
     */
    public final void setIdAttributeNS(java.lang.String ns, java.lang.String ln, boolean isid)
	throws org.w3c.dom.DOMException
    {
        throw new org.w3c.dom.DOMException(org.w3c.dom.DOMException.NOT_SUPPORTED_ERR,"Method not implemented.");
    }
    /**
     * @see org.w3c.dom.Element
     * @since DOM Level 3
     * @since Java 1.5
     */
    public final void setIdAttributeNode(org.w3c.dom.Attr idattr, boolean isid)
	throws org.w3c.dom.DOMException
    {
        throw new org.w3c.dom.DOMException(org.w3c.dom.DOMException.NOT_SUPPORTED_ERR,"Method not implemented.");
    }

    public final boolean isDeclaredNamespace(java.lang.String ns){
	if (null == ns)
	    throw new gnu.iou.dom.Error.Argument();
	else {
	    org.w3c.dom.Node parent = this.getParentNode();
	    if (null != parent){
		if (ns.equals(parent.getNamespaceURI()))
		    return true;
		else if (parent instanceof gnu.iou.dom.Node)
		    return ((gnu.iou.dom.Node)parent).isDeclaredNamespace(ns);
		else
		    return false;
	    }
	    else
		return false;
	}
    }
    /**
     * This method defined here returns a text node created by the
     * document.  It is intended for overriding this behavior as
     * desired to return CDATA or other character data nodes.
     */
    protected gnu.iou.dom.CharacterData createData(){
	org.w3c.dom.Document ctor = this.getOwnerDocument();
	return (gnu.iou.dom.CharacterData)ctor.createTextNode(STR_NIL);
    }
}
