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
public interface Document
    extends Node,
	    org.w3c.dom.Document
{

    public final static java.lang.Class AttrClass = Attr.class;
    public final static java.lang.Class CDATASectionClass = CDATASection.class;
    public final static java.lang.Class CommentClass = Comment.class;
    public final static java.lang.Class DocumentClass = Document.class;
    public final static java.lang.Class DocumentFragmentClass = DocumentFragment.class;
    public final static java.lang.Class DocumentTypeClass = DocumentType.class;
    public final static java.lang.Class ElementClass = Element.class;
    public final static java.lang.Class EntityClass = Entity.class;
    public final static java.lang.Class EntityReferenceClass = EntityReference.class;
    public final static java.lang.Class NotationClass = Notation.class;
    public final static java.lang.Class ProcessingInstructionClass = ProcessingInstruction.class;
    public final static java.lang.Class TextClass = Text.class;

    public void destroy();

    public void setSource(gnu.iou.dom.io.Source src);

    public void builderExit();

    public Element getDocumentElement2();

    public Builder.Sax builderEnterSax();

    public Builder.Sax builderSax(boolean exc);

    public Builder builder(boolean exc);

    public org.w3c.dom.DocumentType getDoctype();

    public void setDoctype(DocumentType doctype);

    public org.w3c.dom.DOMImplementation getImplementation();

    public void setImplementation(DOMImplementation impl);

    public org.w3c.dom.Element createElement(String qn)
	throws org.w3c.dom.DOMException;

    public Element createElement(Name name);

    public org.w3c.dom.Element createElementNS(String ns, String qn)
	throws org.w3c.dom.DOMException;

    public org.w3c.dom.DocumentFragment createDocumentFragment();

    public org.w3c.dom.Text createTextNode(String text);

    public org.w3c.dom.Comment createComment(String text);

    public org.w3c.dom.CDATASection createCDATASection(String text)
	throws org.w3c.dom.DOMException;

    public org.w3c.dom.ProcessingInstruction createProcessingInstruction(String target, String data)
	throws org.w3c.dom.DOMException;

    public org.w3c.dom.Attr createAttribute(String name)
	throws org.w3c.dom.DOMException;

    public org.w3c.dom.Attr createAttributeNS(String ns, String qn)
	throws org.w3c.dom.DOMException;

    public org.w3c.dom.EntityReference createEntityReference(String name)
	throws org.w3c.dom.DOMException;

    /**
     * <p> Nodes' instantiation called from the create methods
     * implements node binding from names to node classes, including
     * any extant builder context.  Calls {@link
     * #create(gnu.iou.dom.Name,gnu.iou.dom.Builder$Binding)}. </p>
     * 
     * @param type Node type as defined by {@link org.w3c.dom.Node}
     * @param ns Node namespace URI
     * @param qn Node name, prefix optional
     * 
     * @return Bound or default nodes
     * 
     * @see gnu.iou.dom.Builder$Binding
     */
    public Node create(short type, String ns, String qn);

    /**
     * <p> Stateful nodes' instantiation with special or override
     * binding support.  Implements binding convention, and calls
     * {@link
     * #instantiateNode(java.lang.String,gnu.iou.dom.Name)}. </p>
     * 
     * @param name Required node name with required type must be
     * complete
     * @param bind Optional binding context (containing node)
     * @return New node for name and type may be default
     * implementation, or throws exception for bad arguments or
     * unrecognized type
     * @exception java.lang.IllegalArgumentException For a missing or
     * incomplete (type) node name argument.
     * @exception java.lang.IllegalStateException For an unrecognized
     * node 'type'.
     */
    public Node create(Name nname, Builder.Binding bind)
	throws java.lang.IllegalArgumentException,
	       java.lang.IllegalStateException;

    /**
     * <p> Node instantiation tool called from the {@link
     * #create(short,java.lang.String,java.lang.String)} method
     * implements typed node class lookup and construction for a node
     * class having an accessable constructor taking operands
     * <code>(org.w3c.dom.Document, gnu.iou.dom.Name)</code>.</p>
     * 
     * @param classname Canonical (dot) name as produced by the {@link
     * #create(short,java.lang.String,java.lang.String)} method
     * @param nname Required complete node name and type, typically
     * produced from the verbatim input to the {@link
     * #create(short,java.lang.String,java.lang.String)} method
     * 
     * @return New node
     * 
     * @exception java.lang.ClassNotFoundException For a class named
     * 'classname' not found.
     * @exception java.lang.IllegalStateException For a class named
     * 'classname' not having the required node 'type'.  Or, for
     * development time errors in instantiating and constructing the
     * node class (eg, ctor not accessable, or throws exceptions) in
     * which case cause exceptions will be attached.
     * @exception java.lang.IllegalArgumentException For a class named
     * 'classname' not having the required constructor method.
     */
    public Node instantiateNode(String classname, Name nname)
	throws java.lang.ClassNotFoundException,
	       java.lang.IllegalStateException,
	       java.lang.IllegalArgumentException;

}
