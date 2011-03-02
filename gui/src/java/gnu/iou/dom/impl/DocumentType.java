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
public class DocumentType
    extends Node
    implements gnu.iou.dom.DocumentType
{
    private java.lang.String doctype_name, doctype_sid, doctype_pid, doctype_subset;
    private gnu.iou.dom.NamedNodeMap doctype_entities, doctype_notations;

    public DocumentType(){
        super(DOC_NIL,new Name(DOCUMENT_TYPE_NODE,STR_NIL,"DocumentType"));
    }
    public DocumentType(org.w3c.dom.Document owner, gnu.iou.dom.Name nname){
        super(owner,nname);
    }

    public final java.lang.String getName(){
	return this.doctype_name;
    }
    public final void setName(java.lang.String name){
	this.doctype_name = name;
    }
    public final org.w3c.dom.NamedNodeMap getEntities(){
	if (null == this.doctype_entities)
	    return NamedNodeMap.MAP_NIL;
	else
	    return this.doctype_entities;
    }
    public final void setEntities(gnu.iou.dom.NamedNodeMap entities){
	this.doctype_entities = entities;
    }
    public final org.w3c.dom.NamedNodeMap getNotations(){
	if (null == this.doctype_notations)
	    return NamedNodeMap.MAP_NIL;
	else
	    return this.doctype_notations;
    }
    public final void setNotations(gnu.iou.dom.NamedNodeMap notations){
	this.doctype_notations = notations;
    }
    public final java.lang.String getPublicId(){
	return this.doctype_pid;
    }
    public final void setPublicId(java.lang.String pid){
	this.doctype_pid = pid;
    }
    public final java.lang.String getSystemId(){
	return this.doctype_sid;
    }
    public final void setSystemId(java.lang.String sid){
	this.doctype_sid = sid;
    }
    public final java.lang.String getInternalSubset(){
	return this.doctype_subset;
    }
    public final void setInternalSubset(java.lang.String subset){
	this.doctype_subset = subset;
    }

}
