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
public class DOMImplementation
    implements gnu.iou.dom.DOMImplementation
{

    public DOMImplementation(){
        super();
    }

    public /*(disused)*/ Object getFeature(String feature, String version){
	return null;
    }
    public /*(disused)*/ boolean hasFeature(String feature, String version){
	return false;
    }
    public /*(not-supported)*/ org.w3c.dom.DocumentType createDocumentType(String qn, String pid, String sid)
	throws org.w3c.dom.DOMException
    {
	throw new org.w3c.dom.DOMException(org.w3c.dom.DOMException.NOT_SUPPORTED_ERR,Node.STR_NIL);
    }
    public /*(not-supported)*/ org.w3c.dom.Document createDocument(String ns, String qn, org.w3c.dom.DocumentType doctype)
	throws org.w3c.dom.DOMException
    {
	throw new org.w3c.dom.DOMException(org.w3c.dom.DOMException.NOT_SUPPORTED_ERR,Node.STR_NIL);
    }

}
