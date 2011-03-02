package com.robonobo.common.swing;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.MutableTreeNode;

public abstract class SortableTreeNode extends DefaultMutableTreeNode implements Comparable<SortableTreeNode> {
	public SortableTreeNode(Object userObject) {
		super(userObject);
	}

	public abstract int compareTo(SortableTreeNode o);
	
	@Override
	public void remove(int childIndex) {
		// TODO Auto-generated method stub
		super.remove(childIndex);
	}
	
	@Override
	public void remove(MutableTreeNode aChild) {
		// TODO Auto-generated method stub
		super.remove(aChild);
	}
	
	@Override
	public void removeAllChildren() {
		// TODO Auto-generated method stub
		super.removeAllChildren();
	}
}
