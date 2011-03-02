package com.robonobo.common.swing;

public class DefaultSortableTreeNode extends SortableTreeNode {
	public DefaultSortableTreeNode(Object userObject) {
		super(userObject);
	}

	@Override
	public int compareTo(SortableTreeNode o) {
		return getUserObject().toString().compareTo(o.getUserObject().toString());
	}
}
