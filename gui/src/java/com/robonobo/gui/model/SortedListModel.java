package com.robonobo.gui.model;

import java.util.*;

import javax.swing.AbstractListModel;

@SuppressWarnings("serial")
public class SortedListModel<T extends Comparable<T>> extends AbstractListModel implements Iterable<T> {
	protected List<T> list = new ArrayList<T>();

	public SortedListModel() {
	}

	public void insertSorted(T element) {
		// If we're empty, just whack it in
		if (list.size() == 0) {
			list.add(element);
			fireIntervalAdded(this, list.size()-1, list.size()-1);
			return;
		}
		// If it's before the first one or after the last one, put at start/end
		if (element.compareTo(list.get(0)) < 0) {
			list.add(0, element);
			fireIntervalAdded(this, 0, 0);
			return;
		}
		if (element.compareTo(list.get(list.size() - 1)) >= 0) {
			list.add(element);
			fireIntervalAdded(this, list.size()-1, list.size()-1);
			return;
		}
		// Pick the index by binary search and plonk it in
		int idx = insertIndex(element, 0, getSize() - 1);
		list.add(idx, element);
		fireIntervalAdded(this, idx, idx);
	}

	public void remove(T element) {
		int idx = list.indexOf(element);
		if(idx >= 0) {
			list.remove(idx);
			fireIntervalRemoved(this, idx, idx);
		}
	}
	
	@Override
	public int getSize() {
		return list.size();
	}

	public void clear() {
		int oldSz = list.size();
		list.clear();
		if(oldSz > 0)
			fireIntervalRemoved(this, 0, oldSz-1);
	}
	
	/**
	 * Returns the object that will be displayed in the list (might not be the same as get(index), e.g.
	 * get(index).someProperty)
	 */
	@Override
	public Object getElementAt(int index) {
		return get(index);
	}

	/**
	 * Returns the object in our sorted list
	 */
	protected T get(int index) {
		return list.get(index);
	}
	
	private int insertIndex(T element, int low, int high) {
		if (low == high || high == (low + 1))
			return high;
		int pivot = (high + low) / 2;
		T pEl = list.get(pivot);
		int cmp = element.compareTo(pEl);
		if (cmp == 0)
			return pivot;
		else if (cmp < 0)
			return insertIndex(element, low, pivot);
		else
			return insertIndex(element, pivot, high);
	}

	@Override
	public Iterator<T> iterator() {
		return list.iterator();
	}
}
