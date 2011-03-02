package com.robonobo.common.async;

import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * An object that is given data by a PushDataProvider
 */
public interface PushDataReceiver {
	/**
	 * Guaranteed not to be called again until a previous invocation (if any)
	 * has returned, and so does not need to be thread safe.
	 */
	public void receiveData(ByteBuffer data, Object metadata) throws IOException;
	
	public void providerClosed();
}
