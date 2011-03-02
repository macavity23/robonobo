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


import java.util.Vector;

public class Queue extends Vector {
	private static final long serialVersionUID = 4197875755603533994L;

	/**
	 * Gets an item from the front of the queue.
	 * 
	 * @exception EmptyQueueException
	 *                If the queue is empty.
	 */
	public synchronized Object dequeue() {
		Object obj;
		obj = peek();
		removeElementAt(0);
		return obj;
	}

	/**
	 * Returns true if the queue is empty.
	 */
	public boolean empty() {
		return size() == 0;
	}

	/**
	 * Puts an item into the queue.
	 * 
	 * @param item
	 *            the item to be put into the queue.
	 */
	public synchronized Object enqueue(Object item) {
		addElement(item);
		notifyAll();
		return item;
	}

	/**
	 * Peeks at the front of the queue.
	 * 
	 * @exception EmptyQueueException
	 *                If the queue is empty.
	 */
	public Object peek() {
		int len = size();
		if(len == 0)
			return null;
		return elementAt(0);
	}

	/**
	 * Sees if an object is in the queue.
	 * 
	 * @param o
	 *            the desired object
	 * @return the distance from the front, or -1 if it is not found.
	 */
	public int search(Object o) {
		int i = indexOf(o, 0);
		if(i >= 0) {
			return i;
		}
		return -1;
	}
}
