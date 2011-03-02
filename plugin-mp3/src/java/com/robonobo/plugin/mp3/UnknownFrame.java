package com.robonobo.plugin.mp3;

import java.nio.ByteBuffer;

public class UnknownFrame extends Frame {

	public UnknownFrame(ByteBuffer b, long fileOffset, long frameOffset, long timeOffset) {
		super(b);
		this.byteOffset = fileOffset;
		this.frameOffset = frameOffset;
		this.timeOffset = timeOffset;
	}
	
	@Override
	public String toString() {
		return "UnknownFrame[frame=" + frameOffset + ",fileOffset=" + getByteOffset() + ",len=" + getFrameBuffer().remaining()+"]";
	}
}
