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

package gnu.iou.dom;

/**
 * <p> DOM Attribute </p>
 * 
 * @author jdp
 */
public interface Attr
    extends CharacterData, /*(should be Node, but this is typed data)
			    */
	    org.w3c.dom.Attr
{

    /**
     * @see org.w3c.dom.Attr
     */
    public java.lang.String getName();

    /**
     * <p> Disused </p>
     * @see org.w3c.dom.Attr
     */
    public boolean getSpecified();

    /**
     * @see org.w3c.dom.Attr
     */
    public java.lang.String getValue();

    /**
     * @see org.w3c.dom.Attr
     */
    public void setValue(java.lang.String value)
	throws org.w3c.dom.DOMException;

    /**
     * @see org.w3c.dom.Attr
     */
    public org.w3c.dom.Element getOwnerElement();

    /**
     * @see org.xml.sax.AttributeList
     */
    public java.lang.String getType();

    public void setType(java.lang.String type);

}
