package com.robonobo.mina.external;

/** Called when we receive a handover from another robonobo instance */
public interface HandoverHandler {
	/**
	 * @return The message to pass back to the calling instance, in the form <num>:<msg>, where <num> is non-zero on error
	 */
	public String gotHandover(String arg);
}
