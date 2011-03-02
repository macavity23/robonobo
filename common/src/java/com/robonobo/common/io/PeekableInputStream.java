package com.robonobo.common.io;

import java.io.IOException;

public interface PeekableInputStream {

	public abstract int read() throws IOException;

	public abstract int peek() throws IOException;

	public abstract int available();

}