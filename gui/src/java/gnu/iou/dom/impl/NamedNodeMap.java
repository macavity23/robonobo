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
public class NamedNodeMap
    extends gnu.iou.objmap
    implements gnu.iou.dom.NamedNodeMap
{

    private gnu.iou.dom.Node namednodemap_parent;

    public NamedNodeMap(gnu.iou.dom.Node user){
        super();
	this.namednodemap_parent = user;
    }

    public void destroy(){
	this.namednodemap_parent = null;
	super.clear();
    }
    public gnu.iou.dom.NamedNodeMap cloneNamedNodeMap(gnu.iou.dom.Node parent, boolean deep){
	NamedNodeMap clone = (NamedNodeMap)super.cloneObjmap();
	clone.namednodemap_parent = parent;
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
    public gnu.iou.dom.Node getNamedNodeMapParent(){
	return this.namednodemap_parent;
    }
    public org.w3c.dom.Node getNamedItem(String qn){
	return (org.w3c.dom.Node)this.get(Node.QNameSuffixLiberal(qn));
    }
    public gnu.iou.dom.Node getNamedItem(gnu.iou.dom.Name name){
	return (gnu.iou.dom.Node)this.get(name);
    }
    public org.w3c.dom.Node setNamedItem(org.w3c.dom.Node node)
	throws org.w3c.dom.DOMException
    {
	if (node instanceof gnu.iou.dom.Node){
	    gnu.iou.dom.Node attr = (gnu.iou.dom.Node)node;
	    if (null != this.namednodemap_parent){
		attr.setParentNode(this.namednodemap_parent);
		attr.setOwnerDocument(this.namednodemap_parent.getOwnerDocument());
	    }
	    gnu.iou.dom.Name name = attr.getNodeName2();
	    this.put(name,node);
	    return node;
	}
	else
	    return null;
    }
    public org.w3c.dom.Node removeNamedItem(String qn)
	throws org.w3c.dom.DOMException
    {
	Name name = new Name(qn);
	return (org.w3c.dom.Node)this.remove(name);
    }
    public org.w3c.dom.Node item(int idx){
	return (org.w3c.dom.Node)this.value(idx);
    }
    public int getLength(){
	return this.size();
    }
    public org.w3c.dom.Node getNamedItemNS(String ns, String qn){
	Name nn = new Name(ns,qn);
	return (org.w3c.dom.Node)this.get(nn);
    }
    public org.w3c.dom.Node setNamedItemNS(org.w3c.dom.Node node)
	throws org.w3c.dom.DOMException
    {
	return this.setNamedItem(node);
    }
    public org.w3c.dom.Node removeNamedItemNS(String ns, String qn)
	throws org.w3c.dom.DOMException
    {
	Name nn = new Name(ns,qn);
	return (org.w3c.dom.Node)this.remove(nn);
    }

}
