package com.robonobo.common.async;

/**
 * An object that pushes data to a PushDataReceiver
 */
public interface PushDataProvider {
	/** Set to null to unset the receiver */
	public void setDataReceiver(PushDataReceiver provider);
}
