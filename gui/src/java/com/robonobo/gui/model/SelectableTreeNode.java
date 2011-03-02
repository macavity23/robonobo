package com.robonobo.gui.model;

import java.awt.datatransfer.Transferable;

import javax.swing.tree.TreeNode;

import com.robonobo.common.swing.SortableTreeNode;

@SuppressWarnings("serial")
public class SelectableTreeNode extends SortableTreeNode {
	
	public SelectableTreeNode(Object val) {
		super(val);
	}
	
	public boolean wantSelect() {
		return false;
	}
	
	/**
	 * Return true if the node and all its ancestors have been updated
	 */
	public boolean handleSelect() {
		// Default implementation does nothing and doesn't want selection
		return false;
	}
	
	/** For drag n drop */
	public boolean importData(Transferable t) {
		return false;
	}
	
	@Override
	public int compareTo(SortableTreeNode o) {
		return 0;
	}
}
