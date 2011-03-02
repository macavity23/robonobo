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
 * <p> Array list for iteration. </p>
 * 
 * @author jdp
 */
public interface List {

    public final static Object[] NILSET = null;

    public final static List EMPTY = new gnu.iou.dom.impl.List(NILSET);

    /**
     * <p> List iterator. </p>
     * 
     * @author jdp
     */
    public interface Iterator 
	extends List 
    {
	public final static Iterator EMPTY = new gnu.iou.dom.impl.List.Iterator(null);

	public void reinit();

	public int pointer();

	public int terminal();

	public java.lang.Object next();

	public java.lang.Object current();

	public java.lang.Object previous();

	public boolean more();

	public boolean head();

	public boolean tail();

	public boolean first();

	public boolean last();

	/**
	 * @return Init value
	 */
	public int for_start();

	/**
	 * @return Init value
	 */
	public int for_many();

    }

    public void reinit();

    public List.Iterator iterator();

    public void add( java.lang.Object node);

    public void remove( java.lang.Object node);

    public java.lang.Object item(int idx);

    public int getLength();

    public void destroy();

}
