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
 * <p> A data type user may be employed to maintain data objects as
 * equivalent to source (and target) XML source string values.  The
 * data object classes employed by the data type user must produce
 * desireable data value input or source strings from their object to
 * string methods.  </p>
 * 
 * @author jdp
 */
public interface CharacterData
    extends Node,
	    org.w3c.dom.CharacterData,
	    gnu.iou.dom.io.Source.Char,
	    gnu.iou.dom.io.Target.Char
{
    /**
     * <p> Data type user is expected to tolerate the default behavior
     * of the {@link CharacterData} class in using the data type
     * descriptor value of "zero" for the "source string value" or
     * "any value" from this node. </p>
     * 
     * @author jdp
     */
    public interface User {
	/**
	 * <p> Any type system defined by the implementor is required
	 * by the nature of XML and the DOM to employ type descriptor
	 * value zero for data value type string. </p>
	 */
	public final static int TYPE_STRING = 0x0;

	/**
	 * <p> User data type manager. 
	 * </p>
	 * @param source The node holding value
	 * @param type The requested data type descriptor for the data type user
	 * @param value The current data value, or null for no change
	 * to the node's internal data value and type.
	 * @return Correct type, null, or exception for a conversion
	 * not supported by the type user.
	 * @see gnu.iou.dom.CharacterData#getData(int,gnu.iou.dom.CharacterData$User)
	 */
	public java.lang.Object getData(CharacterData source, int type, 
					java.lang.Object value);
	/**
	 * <p> User data type set identifier.
	 * </p>
	 */
	public int typeOf(java.lang.Object value);

	/**
	 * @return Data type descriptor value for integer represented
	 * by {@link java.lang.Integer} (or subclass at user's
	 * choice).  The implementor may choose to throw a runtime
	 * exception if it has no such data type, at the expense of
	 * {@link
	 * Node#getAttributeInt(java.lang.String,gnu.iou.dom.CharacterData$User)},
	 * {@link
	 * Node#getAttributeIdInt(gnu.iou.dom.CharacterData$User)},
	 * and {@link
	 * Node#getChildById(java.lang.Integer,gnu.iou.dom.CharacterData$User)}.
	 */
	public int typeInteger();
    }

    /**
     * @return User defined type descriptor value, default value
     * "zero" implies "source string"
     */
    public int getDataType();

    /**
     * <p> Calls {@link #getData(int,gnu.iou.dom.CharacterData$User}
     * with a null user, which will not throw an exception if the data
     * type descriptor values match.  Note that the default internal
     * data type is string (type descriptor value zero), and that the
     * internal data type descriptor value is updated when the type
     * user returns a non null data object value. </p>
     * @param type User defined data type descriptor, or zero for "any"
     * @return User defined type object
     */
    public java.lang.Object getData(int type);

    /**
     * <p> The implementor returns the internal data value if null or
     * the internal type descriptor matches the method call operand
     * (requested) type descriptor value. </p>
     * 
     * <p> The {@link gnu.iou.dom.CharacterData$User} will manage the
     * returned object directly as the object it returns.  If it
     * returns a non- null object, the internal data object is
     * updated, and the internal type descriptor value is updated by
     * calling {@link
     * gnu.iou.dom.CharacterData$User#typeOf(java.lang.Object)}.  If
     * it returns null, then null is returned but the node internal
     * data object and type are not changed.  </p>
     * 
     * @param type User defined type descriptor
     * @param user Data type manager
     * @return User defined type object
     */
     public java.lang.Object getData(int type, User user);

    /**
     * <p> Sets the internal data value and type, using the operand
     * data value and the type descriptor value returned by {@link
     * gnu.iou.dom.CharacterData$User#typeOf(java.lang.Object)}.
     * </p>
     * @param data Accept this value for the user defined data
     */
    public void setData(java.lang.Object data);

    /**
     * <p> Employs the user operand to type the data operand. </p>
     * @param data Accept this value for the user defined data
     * @param user Data type user
     */
    public void setData(java.lang.Object data, User user);

    public java.lang.String getData()
	throws org.w3c.dom.DOMException;

    public void setData(java.lang.String data)
	throws org.w3c.dom.DOMException;

    public java.lang.String getNodeValue()
	throws org.w3c.dom.DOMException;

    public void setNodeValue(java.lang.String value)
	throws org.w3c.dom.DOMException;

    public int getLength();

    public java.lang.String substringData(int ofs, int len)
	throws org.w3c.dom.DOMException;

    public void appendData(java.lang.String arg)
	throws org.w3c.dom.DOMException;

    /**
     * <p> Unimplemented </p>
     */
    public void insertData(int ofs, java.lang.String arg)
	throws org.w3c.dom.DOMException;

    /**
     * <p> Unimplemented </p>
     */
    public void deleteData(int ofs, int len)
	throws org.w3c.dom.DOMException;

    /**
     * <p> Unimplemented </p>
     */
    public void replaceData(int ofs, int len, java.lang.String arg)
	throws org.w3c.dom.DOMException;

}
