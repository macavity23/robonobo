package com.robonobo.common.swing;

import javax.swing.DefaultListSelectionModel;
import javax.swing.ListModel;

/**
 * Only permits selection of items that match specified criteria
 * Hacked from example posted by JPmGuru to http://forums.sun.com/thread.jspa?threadID=336115&tstart=9765
 * @author macavity
 *
 */
public abstract class SelectiveListSelectionModel extends DefaultListSelectionModel {
	protected ListModel src = null;

	public SelectiveListSelectionModel(ListModel model) {
		if (model == null)
			throw new IllegalArgumentException("model cannot be null.");
		src = model;
	}

	protected abstract boolean isSelectable(Object obj);

	/**
	 * Ensure that the selection is maintained for the given selection. If the
	 * selection is simply one index instead of a real range, then the selection
	 * is checked and then added is applicable. If it is a real range, then the
	 * whole range of selection is performed and then individually checked to
	 * unselect selections that were not valid email addresses.
	 * 
	 * @TODO implement a range breakup around invalid entries and then set those
	 * smaller ranges containing the valid selections. Instead of setting each
	 * index in the range and generating an event for each index addition.
	 * @param index0 One end of the selection.
	 * @param index1 The other end of selection.
	 * @param add Is the selection being added (true) or set (false)?
	 */
	protected void ensureSelectionMaintained(int index0, int index1, boolean add) {
		boolean oneSelection = (index0 == index1 && index0 >= 0);
		if (!oneSelection) {
			if (add)
				super.addSelectionInterval(index0, index1);
			else
				super.setSelectionInterval(index0, index1);

			int start = index0;
			int end = index1;

			// if they're in opposite order, switch them.
			if (index0 > index1) {
				start = index1;
				end = index0;
			}

			// ensure that the ends are within bounds
			if (start >= 0 && end <= src.getSize() - 1) {
				// check through each one and ensure that the unselectable objs
				// are deselected.
				for (int n = start; n <= end; n++) {
					if (!isSelectable(src.getElementAt(n))) 
						super.removeSelectionInterval(n, n);
				}
			}
		} else {
			// check the single selection change
			if (isSelectable(src.getElementAt(index0))) {
				if (add)
					super.addSelectionInterval(index0, index1);
				else
					super.setSelectionInterval(index0, index1);
			}
		}
	}

	/*
	 * Ensure that any added selections are processed and the data underlying
	 * the selection is a valid email address.
	 * 
	 * @see javax.swing.ListSelectionModel#addSelectionInterval(int, int)
	 */
	public void addSelectionInterval(int index0, int index1) {
		boolean add = true;
		ensureSelectionMaintained(index0, index1, add);
	}

	/*
	 * Ensure that any set selections are processed and the data underlying the
	 * selection is a valid email address.
	 * 
	 * @see javax.swing.ListSelectionModel#setSelectionInterval(int, int)
	 */
	public void setSelectionInterval(int index0, int index1) {
		boolean add = false;
		ensureSelectionMaintained(index0, index1, add);
	}
}
