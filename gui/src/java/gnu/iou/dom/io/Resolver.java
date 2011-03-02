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
package gnu.iou.dom.io;

/**
 * <p> Where this interface is implemented, the {@link Source} should
 * also implement this interface.  A number of architectures can be
 * described by this convention, but this interface simply provides
 * common access.  For example there's no requirement that the
 * implementor cache documents.  </p>
 * 
 * <p> This interface is not asychronous, these methods return when
 * the operation is complete.  Or they throw a DOM Error or I/O
 * exception on failure.
 * </p>
 * 
 * <p> The implementor would accept FILE and HTTP (or one of FILE or
 * HTTP) protocol references at a minimum.
 * </p>
 * 
 * 
 * @author jdp
 */
public interface Resolver {

    /**
     * <p> Store document to reference location. </p>
     */
    public interface Put extends Resolver {

	/**
	 * @param uri Reference to location (URL)
	 * @param doc Send document for storage (PUT request body XML)
	 */
	public void put(gnu.iou.uri uri, gnu.iou.dom.Document doc)
	    throws java.io.IOException;
    }
    /**
     * <p> Send message to reference location. </p>
     * 
     * <p> The implementor may choose to accept a null document
     * parameter in the case where the URL includes query parameters.
     * Whether the implementor converts query parameters to an HTTP
     * FORM body format is another option.  It should, but many
     * servers will accept either as equivalent.  </p>
     */
    public interface Post extends Resolver {

	/**
	 * @param uri Reference to location (URL)
	 * @param doc Send message (POST request body XML)
	 * @return Response XML document may be null
	 */
	public gnu.iou.dom.Document post(gnu.iou.uri uri, gnu.iou.dom.Document doc)
	    throws java.io.IOException;
    }

    /**
     * <p> Convenient "all" interface. </p>
     */
    public interface WS extends Put, Post {}

    public gnu.iou.dom.Document get(gnu.iou.uri uri)
	throws java.io.IOException;


}
