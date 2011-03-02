package com.robonobo.core.wang;

public interface WangListener {
	public void balanceChanged(double newBalance);
	/**
	 * @param creditValue >0 if credit, <0 if debit
	 */
	public void accountActivity(double creditValue, String narration);
}
