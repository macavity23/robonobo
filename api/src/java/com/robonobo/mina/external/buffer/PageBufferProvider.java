package com.robonobo.mina.external.buffer;

import java.io.IOException;


public interface PageBufferProvider {

	public PageBuffer getPageBuf(String streamId);

}