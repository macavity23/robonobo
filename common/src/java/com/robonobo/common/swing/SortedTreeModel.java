package com.robonobo.common.swing;

import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.*;

public class SortedTreeModel extends DefaultTreeModel {

	public SortedTreeModel(TreeNode root) {
		super(root);
	}

	public synchronized void replaceNodeSorted(SortableTreeNode parent, SortableTreeNode changedNode) {
		removeNodeFromParent(changedNode);
		insertNodeSorted(parent, changedNode);
	}

	public synchronized void insertNodeSorted(SortableTreeNode parent, SortableTreeNode newNode) {
		// If the parent has no children, just insert it
		if (parent.getChildCount() == 0) {
			insertNodeInto(newNode, parent, 0);
			return;
		}
		// If it's before the first one or after the last one, just put it in
		SortableTreeNode firstChild = (SortableTreeNode) parent.getFirstChild();
		if (newNode.compareTo(firstChild) < 0) {
			insertNodeInto(newNode, parent, 0);
			return;
		}
		SortableTreeNode lastChild = (SortableTreeNode) parent.getLastChild();
		if (newNode.compareTo(lastChild) >= 0) {
			insertNodeInto(newNode, parent, parent.getChildCount());
			return;
		}
		// Otherwise, use binary search to find the place
		insertNodeInto(newNode, parent, getInsertIndex(newNode, parent, 0, parent.getChildCount() - 1));
	}

	private int getInsertIndex(SortableTreeNode newNode, SortableTreeNode parent, int low, int high) {
		if (low == high || high == (low + 1))
			return high;
		int pivot = (high + low) / 2;
		SortableTreeNode pNode = (SortableTreeNode) parent.getChildAt(pivot);
		int cmp = newNode.compareTo(pNode);
		if (cmp == 0)
			return pivot;
		if (cmp < 0)
			return getInsertIndex(newNode, parent, low, pivot);
		else
			return getInsertIndex(newNode, parent, pivot, high);
	}

	public void firePathToRootChanged(TreeNode n) {
		// Why isn't this in DefaultTreeModel?
		TreeModelEvent e = new TreeModelEvent(this, getPathToRoot(n));
		for (TreeModelListener l : getTreeModelListeners()) {
			l.treeNodesChanged(e);
		}
	}
}
