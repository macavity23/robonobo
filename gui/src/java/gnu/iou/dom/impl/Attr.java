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
 * <p> DOM Attribute </p>
 * 
 * @author jdp
 */
public class Attr
    extends CharacterData /*(should be Node, but this is typed data)
			   */
    implements gnu.iou.dom.Attr
{
    private java.lang.String attr_type;

    public Attr(org.w3c.dom.Document owner, gnu.iou.dom.Name name){
        super(owner,name);
    }
    public final java.lang.String getName(){
	return this.getLocalName();
    }
    public final /*(disused)*/ boolean getSpecified(){
	return true;
    }
    public final java.lang.String getValue(){
	return this.getData();
    }
    public final void setValue(java.lang.String value)
	throws org.w3c.dom.DOMException
    {
	this.setData(value);
    }
    public final org.w3c.dom.Element getOwnerElement(){
	return (org.w3c.dom.Element)this.getParentNode();
    }
    public final java.lang.String getType(){
	return this.attr_type;
    }
    public final void setType(java.lang.String type){
	this.attr_type = type;
    }
    public final boolean isId(){
	return ("id".equals(this.getLocalName()));
    }
}
