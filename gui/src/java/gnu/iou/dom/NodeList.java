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
public interface NodeList
    extends org.w3c.dom.NodeList,
	    java.lang.Cloneable
{
    public final static NodeList LIST_NIL = new gnu.iou.dom.impl.NodeList(Element.ELEMENT_NIL);

    /**
     * <p> Stack operations on node list. </p>
     */
    public interface Stack
	extends NodeList
    {
	/**
	 * <p> Insert node into stack head (position index zero). </p>
	 */
	public void push(gnu.iou.dom.Node node);

	/**
	 * <p> Remove node from stack head and return. </p>
	 */
	public gnu.iou.dom.Node pop();

	/**
	 * <p> Return stack head.  Identical to
	 * <code>item(0)</code>. </p>
	 */
	public gnu.iou.dom.Node peek();

	/**
	 * <p> Return node indexed from stack head.  Identical to
	 * <code>item(idx)</code>. </p>
	 */
	public gnu.iou.dom.Node peek(int idx);

	/**
	 * <p> Swap node in position zero with node in position
	 * one. </p>
	 */
	public void swap();

	/**
	 * <p> Multiply reference node at index zero (stack head).
	 * The node is not cloned, so that operations on one affects
	 * the other. </p>
	 */
	public void dup();
    }

    public void destroy();

    public NodeList cloneNodeList(Node clone, boolean deep);

    public Node getNodeListParent();

    /**
     * @return Node type, e.g., {@link org.w3c.dom.Node#ELEMENT_NODE}
     */
    public short type(int idx);

    public short typeFirst();

    public short typeLast();

    public org.w3c.dom.Node item(int idx);

    public Node item2(int idx);

    public int item(org.w3c.dom.Node node);

    public int item(gnu.iou.dom.Node node);

    public Node item(java.lang.String name);

    public Node item(Name name);

    public java.lang.Object remove(int idx);

    public int getLength();

    public org.w3c.dom.Node append(org.w3c.dom.Node child);

    public org.w3c.dom.Node remove(org.w3c.dom.Node child);

    public org.w3c.dom.Node replace(org.w3c.dom.Node newn, org.w3c.dom.Node oldn);

    public org.w3c.dom.Node insert(org.w3c.dom.Node newn, org.w3c.dom.Node oldn);

    /**
     * <p> Called from {@link Node#resetNodeName2(gnu.iou.dom.Name)},
     * and should not be used otherwise. </p>
     */
    public gnu.iou.dom.Node rename(gnu.iou.dom.Name newn, gnu.iou.dom.Name oldn, gnu.iou.dom.Node node);

    /**
     * <p> Reorder children by clearing, sorting, and then re-adding
     * nodes in sorted order.  <i>N.B.</i> requires that <i>all</i>
     * node children implement {@link java.lang.Comparable}. </p>
     */
    public void sort();
    /**
     * <p> List contents in user type, for example as follows.
     * <pre> 
     * usrNode[] list = (usrNode[])children.list(usrNode.class);
     * </pre> 
     * </p>
     * @param component Class of component type for returned array,
     * subclass of {@link Node} (not array of node).
     * @return An array in class "array of component" listing the
     * elements of this node list.  Null when the number of elements
     * of this list is zero.
     */
    public Object list(java.lang.Class component);
}
