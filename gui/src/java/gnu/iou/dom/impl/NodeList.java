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
public class NodeList
    extends gnu.iou.objmap
    implements gnu.iou.dom.NodeList.Stack
{
    private gnu.iou.dom.Node nodelist_parent;

    public NodeList(gnu.iou.dom.Node user){
        super();
	this.nodelist_parent = user;
    }

    public void destroy(){
	this.nodelist_parent = null;
	super.clear();
    }
    public gnu.iou.dom.NodeList cloneNodeList(gnu.iou.dom.Node parent, boolean deep){
	NodeList clone = (NodeList)super.cloneObjmap();
	clone.nodelist_parent = parent;
	if (deep){
	    gnu.iou.dom.Node item;
	    org.w3c.dom.Node item_clone;
	    for (int idx = 0, len = clone.getLength(); idx < len; idx++){
		item = (gnu.iou.dom.Node)clone.item(idx);
		if (item.isShared())
		    continue;
		else {
		    item_clone = item.cloneNode(parent);
		    clone.value(idx,item_clone);
		}
	    }
	}
	return clone;
    }
    public final gnu.iou.dom.Node getNodeListParent(){
	return this.nodelist_parent;
    }
    public short type(int idx){
	org.w3c.dom.Node node = this.item(idx);
	if (null != node)
	    return node.getNodeType();
	else
	    return (short)0;
    }
    public short typeFirst(){
	return this.type(0);
    }
    public short typeLast(){
	return this.type(this.size()-1);
    }
    public org.w3c.dom.Node item(int idx){
	return (org.w3c.dom.Node)this.value(idx);
    }
    public gnu.iou.dom.Node item2(int idx){
	return (gnu.iou.dom.Node)this.value(idx);
    }
    public gnu.iou.dom.Node item(java.lang.String name){
	return (gnu.iou.dom.Node)this.get(name);
    }
    public gnu.iou.dom.Node item(gnu.iou.dom.Name name){
	return (gnu.iou.dom.Node)this.get(name);
    }
    public int item(org.w3c.dom.Node node){
	if (node instanceof gnu.iou.dom.Node)
	    return this.item((gnu.iou.dom.Node)node);
	else
	    return -1;
    }
    public int item(gnu.iou.dom.Node node){
	gnu.iou.dom.Name name = node.getNodeName2();
	int[] list = this.indexOfList(name);
	if (null == list)
	    return -1;
	else {
	    int len = list.length;
	    if (1 == len)
		return list[0];
	    else {
		for (int idx, lidx = 0; lidx < len; lidx++){
		    idx = list[lidx];
		    if (node == this.value(idx))
			return idx;
		}
		return -1;
	    }
	}
    }
    public int getLength(){
	return this.size();
    }
    public org.w3c.dom.Node append(org.w3c.dom.Node child){
	if (child instanceof gnu.iou.dom.Node){
	    gnu.iou.dom.Node node = (gnu.iou.dom.Node)child;
	    if (null != this.nodelist_parent){
		node.resetParentNode(this.nodelist_parent);
		node.setOwnerDocument(this.nodelist_parent.getOwnerDocument());
	    }
	    gnu.iou.dom.Name name = node.getNodeName2();
	    this.append(name,child);
	    return child;
	}
	else
	    throw new org.w3c.dom.DOMException(org.w3c.dom.DOMException.WRONG_DOCUMENT_ERR,Node.STR_NIL);
    }
    public org.w3c.dom.Node remove(org.w3c.dom.Node child){
	if (child instanceof gnu.iou.dom.Node){
	    int idx = this.item(child);
	    if (-1 < idx){
		this.remove(idx);
		return child;
	    }
	    else
		throw new org.w3c.dom.DOMException(org.w3c.dom.DOMException.WRONG_DOCUMENT_ERR,Node.STR_NIL);
	}
	else
	    throw new org.w3c.dom.DOMException(org.w3c.dom.DOMException.WRONG_DOCUMENT_ERR,Node.STR_NIL);
    }
    public org.w3c.dom.Node replace(org.w3c.dom.Node newn, org.w3c.dom.Node oldn){
	if (newn instanceof gnu.iou.dom.Node && oldn instanceof gnu.iou.dom.Node){
	    if (null != this.nodelist_parent){
		gnu.iou.dom.Node node = (gnu.iou.dom.Node)newn;
		node.resetParentNode(this.nodelist_parent);
		node.setOwnerDocument(this.nodelist_parent.getOwnerDocument());
	    }
	    int idx = this.item(oldn);
	    this.replace(idx,((gnu.iou.dom.Node)newn).getNodeName2(),newn);
	    return newn;
	}
	else
	    throw new org.w3c.dom.DOMException(org.w3c.dom.DOMException.WRONG_DOCUMENT_ERR,Node.STR_NIL);
    }
    public gnu.iou.dom.Node rename(gnu.iou.dom.Name newname, gnu.iou.dom.Name oldname, gnu.iou.dom.Node node){
	if (null != newname && null != oldname && null != node){
	    if (null == this.nodelist_parent)
		throw new gnu.iou.dom.Error.State();
	    else if (this.nodelist_parent != node.getParentNode())
		throw new org.w3c.dom.DOMException(org.w3c.dom.DOMException.WRONG_DOCUMENT_ERR,Node.STR_NIL);
	    else if (oldname != node.getNodeName2())
		throw new gnu.iou.dom.Error.Bug("Node rename protocol.");
	    else {
		int idx = this.item(node);
		this.replace(idx,newname,node);
		return node;
	    }
	}
	else
	    throw new gnu.iou.dom.Error.Argument();
    }
    public org.w3c.dom.Node insert(org.w3c.dom.Node newn, org.w3c.dom.Node oldn){
	if (newn instanceof gnu.iou.dom.Node && oldn instanceof gnu.iou.dom.Node){
	    if (null != this.nodelist_parent){
		gnu.iou.dom.Node node = (gnu.iou.dom.Node)newn;
		node.resetParentNode(this.nodelist_parent);
		node.setOwnerDocument(this.nodelist_parent.getOwnerDocument());
	    }
	    int index = this.item(oldn);
	    if (0 > index){
		this.insert(0,((gnu.iou.dom.Node)newn).getNodeName2(),newn);
		return newn;
	    }
	    else {
		this.insert(index,((gnu.iou.dom.Node)newn).getNodeName2(),newn);
		return newn;
	    }
	}
	else
	    throw new org.w3c.dom.DOMException(org.w3c.dom.DOMException.WRONG_DOCUMENT_ERR,Node.STR_NIL);
    }
    public void sort(){
	int count = this.getLength();
	if (0 < count){
	    org.w3c.dom.Node[] list = (org.w3c.dom.Node[])this.valary(org.w3c.dom.Node.class);
	    this.clear();
	    java.util.Arrays.sort(list);
	    for (int cc = 0; cc < count; cc++){
		this.append(list[cc]);
	    }
	}
    }
    public Object list(java.lang.Class component){
	return this.valary(component);
    }
    public void push(gnu.iou.dom.Node node){
	if (null != node)
	    this.insert(0,node.getNodeName2(),node);
    }
    public gnu.iou.dom.Node pop(){
	gnu.iou.dom.Node head = this.item2(0);
	if (null != head)
	    this.remove(head);
	return head;
    }
    public gnu.iou.dom.Node peek(){
	return this.item2(0);
    }
    public gnu.iou.dom.Node peek(int idx){
	return this.item2(idx);
    }
    public void swap(){
	gnu.iou.dom.Node _0 = this.item2(0);
	if (null != _0){
	    gnu.iou.dom.Node _1 = this.item2(1);
	    if (null != _1){
		this.remove(0);
		this.remove(0);
		this.push(_1);
		this.push(_0);
	    }
	}
    }
    public void dup(){
	gnu.iou.dom.Node head = this.item2(0);
	if (null != head)
	    this.insert(0,head.getNodeName2(),head);
    }
}
