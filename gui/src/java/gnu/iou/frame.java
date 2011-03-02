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
package gnu.iou;

/**
 * <h3> Frame relations</h3>
 * 
 * <p> Values having {@link obfmap} relations need to implement this
 * interface. </p>
 * 
 * <h4>Cloneing</h4>
 * 
 * <p> A frame is not {@link java.lang.Cloneable} because it is not
 * cloned by its users.  Under cloning the frame relation holds, the
 * parent is known by an identical reference.  </p>
 * 
 * @author jdp
 */
public interface frame {
    /**
     * @return Target of frame relation
     */
    public frame frameParentGet();

    public boolean frameStale();
}
