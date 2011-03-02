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
public interface Element
    extends Node,
	    org.w3c.dom.Element
{
    public final static Element ELEMENT_NIL = 
	new gnu.iou.dom.impl.Element(DOC_NIL,new gnu.iou.dom.impl.Name(ELEMENT_NODE,STR_NIL,"Nil"));

    public void destroy();

    public org.w3c.dom.NodeList getChildNodes();

    public boolean hasChildNodes();

    public org.w3c.dom.NamedNodeMap getAttributes();

    public boolean hasAttributes();

    public String getLocSystemId();

    public void setLocSystemId(String sid);

    public String getLocPublicId();

    public void setLocPublicId(String pid);

    public int getLocLineNumber();

    public void setLocLineNumber(int lno);

    public int getLocColumnNumber();

    public void setLocColumnNumber(int cno);

}
