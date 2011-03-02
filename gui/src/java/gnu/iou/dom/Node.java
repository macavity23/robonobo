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
public interface Node
    extends org.w3c.dom.Node,
	    java.lang.Cloneable
{
    public final static java.lang.String STR_NIL = null;

    public final static java.lang.Class CLA_NIL = null;

    public final static Name NAME_NIL = null;

    public final static org.w3c.dom.Document DOC_NIL = null;

    public final static java.lang.Class ClassDate = java.util.Date.class;

    public void destroy();

    /**
     * <p> The document node has an I/O Source, but all nodes can
     * reach it for convenience. </p>
     * @return Document I/O Source maybe null
     */
    public gnu.iou.dom.io.Source getSource();

    public Mutex getMutex();

    /**
     * @return Is not cloned under parent when parent is cloned
     */
    public boolean isShared();
    /**
     * @return Is cloned under parent when parent is cloned
     */
    public boolean isSharedNot();

    /**
     * @param truf Cloned under parent when parent is cloned
     */
    public void setShared(boolean truf);

    /**
     * <p> User space "out of band" boolean attributes. </p>
     * @param attr Index identifier with value greater than negative one 
     * @return Value of indexed attribute, default FALSE for not found
     */
    public boolean isOobAttribute(int attr);

    /**
     * <p> User space "out of band" boolean attributes. </p>
     * @param attr Index identifier with value greater than negative one 
     * @return Inverse value of indexed attribute
     */
    public boolean isOobAttributeNot(int attr);

    /**
     * <p> User space "out of band" boolean attributes. </p>
     * @param attr Index identifier with value greater than negative one 
     * @param value Value of indexed attribute
     */
    public void setOobAttribute(int attr, boolean value);

    public java.lang.Long unique();

    public long uniqueValue();

    public java.lang.String uniqueString();

    public java.lang.String getAttributeIdName();

    public java.lang.String getAttributeId();

    public java.lang.Integer getAttributeIdInt(CharacterData.User user);

    public Name getNodeName2();

    public void resetNodeName2(Name name);

    public Document getOwnerDocument2();

    public Node getParentNode2();

    /**
     * @param pp Set once latch, reset with null.
     */
    public void setParentNode(org.w3c.dom.Node pp);

    /**
     * @param pp Set always
     */
    public void resetParentNode(org.w3c.dom.Node pp);

    /**
     * @param attributes Set always
     */
    public void resetAttributes(NamedNodeMap attributes);

    /**
     * @param children Set always
     */
    public void resetChildNodes(NodeList children);

    public NodeList getChildNodes2();

    public int countChildNodes();

    public void setOwnerDocument(org.w3c.dom.Document dd);

    public void resetOwnerDocument(org.w3c.dom.Document dd);

    public org.w3c.dom.Element getDocumentElement();

    public Element getFirstChildElement();

    public Element getLastChildElement();

    public java.lang.String getChildText();

    public void setChildText(java.lang.String data);

    public void setChildData(java.lang.Object data, CharacterData.User user);

    public java.lang.Object getChildData(int type, CharacterData.User user);

    public Node getChildById(java.lang.String id);

    public Node getChildById(java.lang.Integer id, CharacterData.User user);

    public Node getChildByField(java.lang.String name, java.lang.String value);

    public Node getChildByField(java.lang.String name, java.lang.Object value, CharacterData.User user);

    public Node getChildByName(java.lang.String name);

    public Node getChildByName(Name name);

    public Node getChildByIndex(int idx);

    public void setDataField(java.lang.String name, java.lang.Object data, CharacterData.User user);

    public void pushDataField(java.lang.String name, java.lang.Object data, CharacterData.User user);

    public void setDataField(java.lang.String name, java.lang.Object data, boolean push, CharacterData.User user);

    public java.lang.String getDataField(java.lang.String name);

    public java.lang.Object getDataField(java.lang.String name, CharacterData.User user);

    public java.lang.Object getDataField(java.lang.String name, int type, CharacterData.User user);

    public java.lang.Object popDataField(java.lang.String name, int type, CharacterData.User user);

    public java.lang.Object getDataField(java.lang.String name, int type, boolean pop, CharacterData.User user);

    public org.w3c.dom.Node cloneNode(Node nparent);

    /** @see org.w3c.dom.Element
     */
    public java.lang.String getTagName();

    public NamedNodeMap getAttributes2();

    /** @see org.w3c.dom.Element
     */
    public java.lang.String getAttribute(java.lang.String name);

    public java.lang.Integer getAttributeInt(java.lang.String name, CharacterData.User user);

    /** @see org.w3c.dom.Element
     */
    public java.lang.String getAttributeNS(java.lang.String ns, java.lang.String qn);

    /** @see org.w3c.dom.Element
     */
    public void setAttribute(java.lang.String name, java.lang.String value)
	throws org.w3c.dom.DOMException;

    /** @see org.w3c.dom.Element
     */
    public void setAttributeNS(java.lang.String ns, java.lang.String qn, java.lang.String value)
	throws org.w3c.dom.DOMException;

    /** @see org.w3c.dom.Element
     */
    public void removeAttribute(java.lang.String name)
	throws org.w3c.dom.DOMException;

    /** @see org.w3c.dom.Element
     */
    public void removeAttributeNS(java.lang.String ns, java.lang.String qn)
	throws org.w3c.dom.DOMException;

    /** @see org.w3c.dom.Element
     */
    public org.w3c.dom.Attr getAttributeNode(java.lang.String name);

    public Attr getAttributeNode2(java.lang.String name);

    public Attr getAttributeNode(Name name);

    public Attr getCreateAttributeNode(java.lang.String name);

    public Attr getCreateAttributeNode(Name name);

    /** @see org.w3c.dom.Element
     */
    public org.w3c.dom.Attr getAttributeNodeNS(java.lang.String ns, java.lang.String qn);

    /** @see org.w3c.dom.Element
     */
    public org.w3c.dom.Attr setAttributeNode(org.w3c.dom.Attr node)
	throws org.w3c.dom.DOMException;

    public org.w3c.dom.Attr setAttributeNodeNS(org.w3c.dom.Attr node)
	throws org.w3c.dom.DOMException;

    public org.w3c.dom.Attr removeAttributeNode(org.w3c.dom.Attr node)
	throws org.w3c.dom.DOMException;

    public org.w3c.dom.Node appendChild(org.w3c.dom.Node node)
	throws org.w3c.dom.DOMException;

    public org.w3c.dom.Node removeChild(org.w3c.dom.Node node)
	throws org.w3c.dom.DOMException;

    public org.w3c.dom.Node replaceChild(org.w3c.dom.Node newn, 
					 org.w3c.dom.Node oldn)
	throws org.w3c.dom.DOMException;

    public org.w3c.dom.Node insertBefore(org.w3c.dom.Node newn, 
					 org.w3c.dom.Node oldn)
	throws org.w3c.dom.DOMException;
    /** 
     * <p> Disused </p>
     */
    public boolean isSupported(java.lang.String feature,  java.lang.String version);

    /** 
     * <p> Unimplemented </p>
     */
    public void setPrefix(java.lang.String prefix)
	throws org.w3c.dom.DOMException;
    
    /** @see org.w3c.dom.Document
     */
    public org.w3c.dom.Node importNode(org.w3c.dom.Node node, boolean deep)
	throws org.w3c.dom.DOMException;

    public org.w3c.dom.NodeList getElementsByTagName(java.lang.String name);

    public org.w3c.dom.NodeList getElementsByTagNameNS(java.lang.String ns, java.lang.String qn);

    public org.w3c.dom.Element getElementById(java.lang.String id);

    /**
     * @since DOM Level 3
     * @since Java 1.5
     */
    public String lookupPrefix(String ns);

    /**
     * @since DOM Level 3
     * @since Java 1.5
     */
    public boolean isDefaultNamespace(String ns);

    /**
     * @since DOM Level 3
     * @since Java 1.5
     * 
     */
    public String lookupNamespaceURI(String prefix);

    /**
     * @return Namespace is found among the ancestors (parents) of this node
     */
    public boolean isDeclaredNamespace(String ns);
}
