package com.robonobo.common.serialization;

import com.robonobo.mina.external.buffer.PageBuffer;

public class PageBufferSerializer extends GenericObjectStreamSerializer {
	public Class getDefaultClass() {
		return PageBuffer.class;
	}
}
