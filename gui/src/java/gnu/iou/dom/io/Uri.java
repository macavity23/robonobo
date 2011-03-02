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
 * <p> A System Id URI is expected to be an absolute URI with a
 * scheme and path.  It is implemented by {@link gnu.iou.uri}.
 * </p>
 * 
 * @see Source
 * @see Target
 * 
 * @author jdp
 */
public interface Uri {

    public boolean isAbsolute();

    public java.lang.String getScheme();

    public java.lang.String getHost();

    public java.lang.String getHostUser();

    public java.lang.String getHostPass();

    public java.lang.String getHostName();

    public java.lang.String getHostPort();

    public java.lang.String getPath();

    public java.lang.String getPathTail();

    public java.lang.String getIntern();

    public java.lang.String getInternTail();

    public java.lang.String getFragment();

    public java.lang.String getFragmentTail();

    public java.lang.String getQuery();

    public java.lang.String getTerminal();
}
