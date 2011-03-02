package com.robonobo.common.async;

import java.nio.ByteBuffer;

/**
 * An object that is asked to provide data by a PullDataReceiver
 */
public interface PullDataProvider {
	/**
	 * Do not mess with anything on this buffer after you have returned it, or Bad Things will happen
	 */
	public ByteBuffer getMoreData();
}
