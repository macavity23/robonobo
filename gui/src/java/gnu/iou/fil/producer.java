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
 * <p> On input filter chains the producer is at the source end of the
 * chain, opposite the user. </p>
 * 
 *
 * <h3>Output order</h3>
 * 
 * <p> On output filter chains the producer is at the user end,
 * opposite the target. </p>
 * 
 * 
 * <h3>Example</h3>
 * 
 * <p> The {@link oaep$enc} filter is an example of a producer.  In
 * encoding it is useful only as the first transformer on the data.
 * It transforms bytes from memory into the AONT container. </p>
 * 
 * <p> The {@link base64$dec} filter is an example of a producer.  In
 * decoding it is useful only as the first transformer on the
 * data. </p>
 * 
 * 
 * @see coder$format
 * @see coder$transform
 * @see consumer
 * 
 * @author John Pritchard (jdp@syntelos.com)
 */
public interface producer
{}
