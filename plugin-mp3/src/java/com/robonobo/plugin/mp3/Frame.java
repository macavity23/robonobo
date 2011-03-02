package com.robonobo.plugin.mp3;

import java.nio.ByteBuffer;

public class Frame {
	ByteBuffer frameBuffer;
	int frameLength = 0;
	long byteOffset = 0;
	long frameOffset = 0;
	long timeOffset = 0;
	
	public Frame(ByteBuffer buffer) {
		this.frameBuffer = buffer;
	}
	
	public ByteBuffer getFrameBuffer() {
		return frameBuffer;
	}
	
	public long getByteOffset() {
		return byteOffset;
	}
	
	public long getFrameOffset() {
		return frameOffset;
	}
	
	public long getTimeOffset() {
		return timeOffset;
	}
	
	public int getFrameLength() {
		return frameLength;
	}
	
	public String getTimeOffsetAsString() {
		long hrs = (timeOffset/1000/60/60);
		long min = (timeOffset/1000/60) % 60;
		long sec = (timeOffset/1000) % 60;
		long ms = timeOffset % 1000;
		
		return  hrs + ":" + min + ":" + sec + "." + ms;
	}
}
