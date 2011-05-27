package com.robonobo.common.util;

import java.util.Iterator;
import java.util.TreeSet;

public class ContiguousBlock {
	TreeSet<Integer> set = new TreeSet<Integer>();

	public ContiguousBlock() {
	}

	public synchronized void add(int i) {
		set.add(i);
	}

	/* returns int[2], start of block and end of block inclusive */
	public synchronized int[] getNextBlock() {
		if (set.size() == 0)
			return null;
		int blockStart = -1;
		int lasti = -1;
		for (Iterator<Integer> it = set.iterator(); it.hasNext();) {
			int i = it.next();
			if (blockStart < 0)
				// Start new block
				blockStart = i;
			else if (i != (lasti + 1)) {
				// End of block
				int[] result = new int[] { blockStart, lasti };
				blockStart = -1;
				return result;
			}
			lasti = i;
			it.remove();
		}
		// Block at end
		return new int[] { blockStart, lasti };
	}

	public static void main(String[] args) {
		int[] nums = new int[] { 1, 3, 4, 7, 9, 15, 16, 17, 1001, 1002, 1004, 1005 };
		ContiguousBlock cb = new ContiguousBlock();
		for (int i : nums) {
			cb.add(i);
		}
		int[] block = null;
		while ((block = cb.getNextBlock()) != null) {
			System.out.println("Got block: " + block[0] + ":" + block[1]);
		}
	}
}
