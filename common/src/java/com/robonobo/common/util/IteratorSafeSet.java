package com.robonobo.common.util;
/*
 * Robonobo Common Utils
 * Copyright (C) 2008 Will Morton (macavity@well.com) & Ray Hilton (ray@wirestorm.net)

 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
*/


import java.util.*;;

/**
 * A set that allows add() and remove() to be called while the set is being iterated through.
 * setIterating(true) must be called before you start iterating through this set, and setIterating(false)
 * must be called when you have finished.
 * 
 * @author macavity
 */
public class IteratorSafeSet extends HashSet
{
	private static final long serialVersionUID = 2219978338396858696L;
	private boolean isIterating = false;
	private List toAdd = new ArrayList(); 
	private List toRemove = new ArrayList(); 
	
	public synchronized void setIterating(boolean isIterating)
	{
		this.isIterating = isIterating;
		if(isIterating == false)
		{
			Iterator i = toAdd.iterator();
			while(i.hasNext())
				add(i.next());
			toAdd.clear();
			
			i = toRemove.iterator();
			while(i.hasNext())
				remove(i.next());
			toRemove.clear();
		}
	}
	
	public synchronized boolean add(Object o)
	{
		if(isIterating)
		{
			boolean containedIt = contains(o);
			toAdd.add(o);
			return !containedIt;
		}
		
		return super.add(o);
	}
	
	
	public synchronized boolean addAll(Collection col)
	{
		if(isIterating)
		{
			boolean modified = false;
			Iterator i = col.iterator();
			while(i.hasNext())
			{
				if(add(i.next()))
					modified = true;
			}
			return modified;
		}

		return super.addAll(col);
	}
		
	public synchronized boolean remove(Object o)
	{
		if(isIterating)
		{
			boolean containedIt = contains(o);
			toRemove.add(o);
			return containedIt;
		}
		
		return super.remove(o);
	}
		
	public synchronized boolean removeAll(Collection col)
	{
		if(isIterating)
		{
			boolean modified = false;
			Iterator i = col.iterator();
			while(i.hasNext())
			{
				if(remove(i.next()))
					modified = true;
			}
			return modified;
		}
		
		return super.removeAll(col);
	}
	
	public synchronized boolean contains(Object o)
	{
		if(super.contains(o))
		{
			if(toRemove.contains(o))
				return false;
			else
				return true;
		}
		else
		{
			if(toAdd.contains(o))
				return true;
			else
				return false;
		}
	}
}
