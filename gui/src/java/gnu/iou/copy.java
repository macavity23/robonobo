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
package gnu.iou ;

/**
 * A clone helper function for objects that would not be cloned when a
 * container is cloneing itself, but necessarily need to be cloned in
 * every case in order to preserve essential semantics for multiple
 * users.
 * 
 * <p> For example, "file" does not implement "copy" as the streams it
 * returns from its API are never shared -- so "to clone or not" is
 * left to the discretion of the clone implementor, while "urlc" which
 * maintains stateful connection streams necessarily does need to be
 * copied in every case.
 * 
 * @author John Pritchard (john@syntelos.org)
 */
public interface copy extends Cloneable {

    public Object copy();
}
