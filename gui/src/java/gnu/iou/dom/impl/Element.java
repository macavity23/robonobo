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
public class Element
    extends Node
    implements gnu.iou.dom.Element
{

    private gnu.iou.dom.NodeList element_children;

    private gnu.iou.dom.NamedNodeMap element_attributes;

    private java.lang.String element_loc_sid, element_loc_pid;

    private int element_loc_lno, element_loc_cno;

    public Element(org.w3c.dom.Document owner, gnu.iou.dom.Name name){
	super(owner,name);
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
	this.element_children = null;
	if (this.hasAttributes()){
	    org.w3c.dom.NamedNodeMap attributes = this.getAttributes();
	    gnu.iou.dom.Node attr;
	    for (int cc = 0, len = attributes.getLength(); cc < len; cc++){
		attr = (gnu.iou.dom.Node)attributes.item(cc);
		if (this == attr.getParentNode())
		    attr.destroy();
	    }
	}
	this.element_attributes = null;
	super.destroy();
    }
    protected gnu.iou.dom.NodeList newChildNodes(){
	return new NodeList(this);
    }
    public final gnu.iou.dom.NodeList getChildNodes2(){
	if (null == this.element_children)
	    this.element_children = this.newChildNodes();
	return this.element_children;
    }
    public final void resetChildNodes(gnu.iou.dom.NodeList children){
	this.element_children = children;
    }
    public final boolean hasChildNodes(){
	return (null != this.element_children && 0 < this.element_children.getLength());
    }
    protected gnu.iou.dom.NamedNodeMap newAttributes(){
	return new NamedNodeMap(this);
    }
    public final gnu.iou.dom.NamedNodeMap getAttributes2(){
	if (null == this.element_attributes)
	    this.element_attributes = this.newAttributes();
	return this.element_attributes;
    }
    public final void resetAttributes(gnu.iou.dom.NamedNodeMap attributes){
	this.element_attributes = attributes;
    }
    public final boolean hasAttributes(){
	return (null != this.element_attributes && 0 < this.element_attributes.getLength());
    }
    public final java.lang.String getLocSystemId(){
	return this.element_loc_sid;
    }
    public final void setLocSystemId(java.lang.String sid){
	this.element_loc_sid = sid;
    }
    public final java.lang.String getLocPublicId(){
	return this.element_loc_pid;
    }
    public final void setLocPublicId(java.lang.String pid){
	this.element_loc_pid = pid;
    }
    public final int getLocLineNumber(){
	return this.element_loc_lno;
    }
    public final void setLocLineNumber(int lno){
	this.element_loc_lno = lno;
    }
    public final int getLocColumnNumber(){
	return this.element_loc_cno;
    }
    public final void setLocColumnNumber(int cno){
	this.element_loc_cno = cno;
    }

}
