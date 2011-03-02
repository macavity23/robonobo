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
 * 
 * @author jdp
 */
public interface NamedNodeMap
    extends org.w3c.dom.NamedNodeMap,
	    java.lang.Cloneable
{
    public final static NamedNodeMap MAP_NIL = new gnu.iou.dom.impl.NamedNodeMap(Element.ELEMENT_NIL);

    public void destroy();

    public NamedNodeMap cloneNamedNodeMap(Node clone, boolean deep);

    public Node getNamedNodeMapParent();

    public org.w3c.dom.Node getNamedItem(String qn);

    public Node getNamedItem(Name name);

    public org.w3c.dom.Node setNamedItem(org.w3c.dom.Node node)
	throws org.w3c.dom.DOMException;

    public org.w3c.dom.Node removeNamedItem(String qn)
	throws org.w3c.dom.DOMException;

    public org.w3c.dom.Node item(int idx);

    public int getLength();

    public org.w3c.dom.Node getNamedItemNS(String ns, String qn);

    public org.w3c.dom.Node setNamedItemNS(org.w3c.dom.Node node)
	throws org.w3c.dom.DOMException;

    public org.w3c.dom.Node removeNamedItemNS(String ns, String qn)
	throws org.w3c.dom.DOMException;

}
