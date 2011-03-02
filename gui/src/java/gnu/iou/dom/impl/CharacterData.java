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
 * 
 * @author jdp
 */
public abstract class CharacterData
    extends Node
    implements gnu.iou.dom.CharacterData
{
    public final static gnu.iou.dom.CharacterData.User NIL_USER = null;

    /** 
     * <p> One of the java lang data objects including {@link
     * java.util.Date} and others defined by the type user.  All must 
     * return reasonable string data from the object to-string
     * method.</p>
     */
    private java.lang.Object data;

    private gnu.iou.bbuf io_buffer;

    private gnu.iou.dom.io.Source.Char.Reader io_reader;

    private gnu.iou.dom.io.Target.Char.Writer io_writer;

    private int data_type = 0;

    protected CharacterData(org.w3c.dom.Document owner, gnu.iou.dom.Name name){
	super(owner,name);
    }
    /**
     * @see gnu.iou.dom.io.Source$Char
     */
    public final gnu.iou.dom.CharacterData asIONode(){
	return this;
    }
    /**
     * @see gnu.iou.dom.io.Source$Char
     */
    public gnu.iou.dom.io.Source.Char.Reader getIOReader(){
	if (null == this.io_reader)
	    this.io_reader = new gnu.iou.dom.io.Source.Char.Reader(this);
	return this.io_reader;
    }
    /**
     * @see gnu.iou.dom.io.Target$Char
     */
    public gnu.iou.dom.io.Target.Char.Writer getIOWriter(){
	if (null == this.io_writer)
	    this.io_writer = new gnu.iou.dom.io.Target.Char.Writer(this);
	return this.io_writer;
    }
    /**
     * @see gnu.iou.dom.io.Target$Char
     */
    public void flushIO(gnu.iou.dom.io.Target.Char.Writer writer){
	if (writer == this.io_writer){

	    this.data = writer.toString();
	}
	else if (writer.getTarget() == this)

	    throw new gnu.iou.dom.Error.State("BBBUGGGG: This node has another writer.");
	else
	    throw new gnu.iou.dom.Error.State("BBBUGGGG: Writer target is not this node.");
    }
    /**
     * @see gnu.iou.dom.io.Source$Char
     */
    public void closeIO(gnu.iou.dom.io.Source.Char.Reader reader){
	if (reader == this.io_reader){

	    this.io_reader = null;
	}
	else if (reader.getSource() == this)

	    throw new gnu.iou.dom.Error.State("BBBUGGGG: This node has another reader.");
	else
	    throw new gnu.iou.dom.Error.State("BBBUGGGG: Reader source is not this node.");
    }
    /**
     * @see gnu.iou.dom.io.Target$Char
     */
    public void closeIO(gnu.iou.dom.io.Target.Char.Writer writer){
	if (writer == this.io_writer){

	    this.io_writer = null;
	}
	else if (writer.getTarget() == this)

	    throw new gnu.iou.dom.Error.State("BBBUGGGG: This node has another writer.");
	else
	    throw new gnu.iou.dom.Error.State("BBBUGGGG: Writer target is not this node.");
    }
    /**
     * @see gnu.iou.dom.io.Source$Char
     */
    public final gnu.iou.bbuf getIOBuffer(){
	if (null == this.io_buffer){
	    this.io_buffer = new gnu.iou.bbuf();
	    this.io_buffer.append(this.getData());
	}
	return this.io_buffer;
    }
    public final int getDataType(){
	return this.data_type;
    }
    public final java.lang.Object getData(int type){
	return this.getData(type,NIL_USER);
    }
    public final java.lang.Object getData(int type, User user){
	if (type == this.data_type)
	    return this.data;
	else if (null == data)
	    return this.data;
	else if (null == user && 0 == type){
	    if (this.data instanceof java.lang.String)
		return (java.lang.String)this.data;
	    else
		return this.data.toString();
	}
	else if (null != user){
	    java.lang.Object test = user.getData(this,type,this.data);
	    if (null != test){
		this.data_type = type;
		return (this.data = test);
	    }
	    else
		return test;
	}
	else
	    throw new gnu.iou.dom.Error.State("missing-data-producer");
    }
    public final void setData(java.lang.Object value){
	this.setData(value,NIL_USER);
    }
    public final void setData(java.lang.Object value, User user){
	this.data = value;
	if (null != user)
	    this.data_type = user.typeOf(data);
    }
    public final java.lang.String getData()
	throws org.w3c.dom.DOMException
    {
	if (null == this.data)
	    return null;
	else if (this.data instanceof java.lang.String)
	    return (java.lang.String)this.data;
	else
	    return this.data.toString();
    }
    public final void setData(java.lang.String data)
	throws org.w3c.dom.DOMException
    {
	this.data = data;
	this.data_type = 0;
    }
    public final java.lang.String getNodeValue()
	throws org.w3c.dom.DOMException
    {
	return this.getData();
    }
    public void setNodeValue(java.lang.String value)
	throws org.w3c.dom.DOMException
    {
	this.setData(value);
    }
    public final int getLength(){
	java.lang.String data = this.getData();
	if (null == data)
	    return 0;
	else 
	    return data.length();
    }
    public final java.lang.String substringData(int ofs, int len)
	throws org.w3c.dom.DOMException
    {
	java.lang.String data = this.getData();
	if (null == data)
	    return null;
	else 
	    return data.substring(ofs,len);
    }
    public final void appendData(java.lang.String arg)
	throws org.w3c.dom.DOMException
    {
	java.lang.String data = this.getData();
	if (null == data)
	    this.setData(arg);
	else 
	    this.setData(data.concat(arg));
    }
    public /*(unimplemented)*/ void insertData(int ofs, java.lang.String arg)
	throws org.w3c.dom.DOMException
    {
	throw new org.w3c.dom.DOMException(org.w3c.dom.DOMException.NO_MODIFICATION_ALLOWED_ERR,STR_NIL);
    }
    public /*(unimplemented)*/ void deleteData(int ofs, int len)
	throws org.w3c.dom.DOMException
    {
	throw new org.w3c.dom.DOMException(org.w3c.dom.DOMException.NO_MODIFICATION_ALLOWED_ERR,STR_NIL);
    }
    public /*(unimplemented)*/ void replaceData(int ofs, int len, java.lang.String arg)
	throws org.w3c.dom.DOMException
    {
	throw new org.w3c.dom.DOMException(org.w3c.dom.DOMException.NO_MODIFICATION_ALLOWED_ERR,STR_NIL);
    }
    /**
     * @since DOM Level 3
     * @since Java 1.5
     */
    public boolean isElementContentWhitespace()
    {
	/*
	 * normalized DOM
	 */
	return false;
    }
    /**
     * @since DOM Level 3
     * @since Java 1.5
     */
    public String getWholeText()
    {
	return this.getData();
    }
    /**
     * @since DOM Level 3
     * @since Java 1.5
     */
    public org.w3c.dom.Text replaceWholeText(String content)
                                 throws org.w3c.dom.DOMException
    {
	throw new org.w3c.dom.DOMException(org.w3c.dom.DOMException.NOT_SUPPORTED_ERR,null);
    }

}
