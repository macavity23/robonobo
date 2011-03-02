package com.robonobo.eon;

import java.nio.ByteBuffer;

public interface DEONDataReceiver {
	/**
	 * Called with more data to receive. This method is guaranteed to be called
	 * in order of data reception
	 */
	public void receive(ByteBuffer buf, EonSocketAddress fromAddr);
}
