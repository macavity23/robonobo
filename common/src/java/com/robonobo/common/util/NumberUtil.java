package com.robonobo.common.util;

public class NumberUtil {
	/** Never instantiate this class
	 */
	private NumberUtil() {
	}
	
	public static final boolean dblEq(double a, double b) {
		return (a - b) == 0d;
	}
}
