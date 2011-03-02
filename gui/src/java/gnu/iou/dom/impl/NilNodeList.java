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
public class NilNodeList
    extends java.lang.Object
    implements gnu.iou.dom.NodeList
{
    private gnu.iou.dom.Node nodelist_parent;

    public NilNodeList(gnu.iou.dom.Node user){
        super();
	this.nodelist_parent = user;
    }

    public void destroy(){
	this.nodelist_parent = null;
    }
    public gnu.iou.dom.NodeList cloneNodeList(gnu.iou.dom.Node parent, boolean deep){
	try {
	    NilNodeList clone = (NilNodeList)super.clone();
	    clone.nodelist_parent = parent;
	    //(if deep)
	    return clone;
	}
	catch (java.lang.CloneNotSupportedException cns){
	    throw new gnu.iou.dom.Error.State();
	}
    }
    public final gnu.iou.dom.Node getNodeListParent(){
	return this.nodelist_parent;
    }
    public short type(int idx){
	return (short)0;
    }
    public short typeFirst(){
	return this.type(0);
    }
    public short typeLast(){
	return this.type(this.getLength()-1);
    }
    public org.w3c.dom.Node item(int idx){
	return null;
    }
    public gnu.iou.dom.Node item2(int idx){
	return null;
    }
    public gnu.iou.dom.Node item(java.lang.String name){
	return null;
    }
    public int item(org.w3c.dom.Node node){
	return -1;
    }
    public int item(gnu.iou.dom.Node node){
	return -1;
    }
    public gnu.iou.dom.Node item(gnu.iou.dom.Name name){
	return null;
    }
    public int getLength(){
	return 0;
    }
    public java.lang.Object remove(int idx){
	throw new org.w3c.dom.DOMException(org.w3c.dom.DOMException.NOT_SUPPORTED_ERR,null);
    }
    public org.w3c.dom.Node append(org.w3c.dom.Node child){
	throw new org.w3c.dom.DOMException(org.w3c.dom.DOMException.NOT_SUPPORTED_ERR,null);
    }
    public org.w3c.dom.Node remove(org.w3c.dom.Node child){
	throw new org.w3c.dom.DOMException(org.w3c.dom.DOMException.NOT_SUPPORTED_ERR,null);
    }
    public org.w3c.dom.Node replace(org.w3c.dom.Node newn, org.w3c.dom.Node oldn){
	throw new org.w3c.dom.DOMException(org.w3c.dom.DOMException.NOT_SUPPORTED_ERR,null);
    }
    public org.w3c.dom.Node insert(org.w3c.dom.Node newn, org.w3c.dom.Node oldn){
	throw new org.w3c.dom.DOMException(org.w3c.dom.DOMException.NOT_SUPPORTED_ERR,null);
    }
    public gnu.iou.dom.Node rename(gnu.iou.dom.Name newn, gnu.iou.dom.Name oldn, gnu.iou.dom.Node node){
	throw new org.w3c.dom.DOMException(org.w3c.dom.DOMException.NOT_SUPPORTED_ERR,null);
    }
    public void sort(){
	throw new org.w3c.dom.DOMException(org.w3c.dom.DOMException.NOT_SUPPORTED_ERR,null);
    }
    public Object list(java.lang.Class component){
	return null;
    }
}
