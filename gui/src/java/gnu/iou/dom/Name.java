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
 * <p> Node name </p>
 * 
 * @author jdp
 */
public interface Name {

    public final static short TYPE_NIL = (short)0;

    public final static java.lang.String STR_NIL = null;

    public short getType();

    public java.lang.String getPrefix();

    public java.lang.String getLocalname();

    public java.lang.String getQname();

    public java.lang.String getNamespace();

    public int hashCode();

    public boolean equals(Object ano);

    /**
     * @return The namespace URI processed into a package name
     */
    public java.lang.String getPackage();

    /**
     * Concatenate the camel case of the local name with the prefix.
     * @see Builder$Binding
     */
    public java.lang.String getClassname(java.lang.String prefix);

    /**
     * @return Concatenate the camel case of the local name with the
     * namespace (as processed into package name) and dot.
     */
    public java.lang.String getClassname();

    /**
     * <p> This "notational" API method for the normalized name is
     * semantically identical to {@link #getClassname()}. </p>
     * 
     * @return Normalized name is identical to classname.
     */
    public java.lang.String getNormal();

    /**
     * Concatenate the camel case of the local name with this string
     * and dollar sign.
     */
    public java.lang.String getClassnameInner(java.lang.String inner);
    /**
     * Concatenate the camel case of the local name with this string
     * and dot.
     */
    public java.lang.String getClassnameUnder(java.lang.String inner);

}
