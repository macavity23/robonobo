package com.robonobo.gui.model;

import com.robonobo.common.swing.SortableTreeNode;

/**
 * A tree node that just executes something when it's clicked, but never retains selection
 * @author macavity
 *
 */
@SuppressWarnings("serial")
public abstract class ButtonTreeNode extends SortableTreeNode {
	
	public ButtonTreeNode(String title) {
		super(title);
	}

	@Override
	public int compareTo(SortableTreeNode o) {
		if(o instanceof ButtonTreeNode)
			return 0;
		return -1;
	}
	
	public abstract void onClick();
}
