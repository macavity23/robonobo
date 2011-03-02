 /* 
  *  `gnu.iou' 
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

package gnu.iou.fil ;

/**
 * <p> Filter marking interface for filter ordering. </p>
 * 
 * <h3>Input order</h3>
 * 
 * <p> On input filter chains the consumer is at the user end,
 * opposite the source. </p>
 * 
 *
 * <h3>Output order</h3>
 * 
 * <p> On output filter chains the consumer is at the target end,
 * opposite the producer. </p>
 * 
 * 
 * <h3>Example</h3>
 * 
 * <p> The {@link oaep$dec} filter is an example of a consumer.  In
 * decoding it is useful only as the last transformer on the data, a
 * role necessarily symmetric to its use in the encoding
 * direction. </p>
 * 
 * <p> The {@link base64$enc} filter is an example of a consumer.  In
 * encoding it is useful only as the last transformer on the data.
 * The Base64 format has no other efficient and practical use. </p>
 * 
 * 
 * @see coder$format
 * @see coder$transform
 * @see producer
 * 
 * @author John Pritchard (jdp@syntelos.com)
 */
public interface consumer
{}
