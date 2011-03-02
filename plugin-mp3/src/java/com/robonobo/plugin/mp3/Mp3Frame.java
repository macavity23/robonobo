package com.robonobo.plugin.mp3;

import java.nio.ByteBuffer;

public class Mp3Frame extends Frame {
	public static final int[] BITRATES = new int[] {
		0,
		32,
		40,
		48, 
		56,
		64,
		80,
		96,
		112,
		128,
		160,
		192,
		224,
		256,
		320,
		-1
	};
	public static final int[] RATES = new int[] {
		44100,
		48000,
		32000,
		-1
	};
	
	int version = 0;
	int layer = 0;
	boolean crc = false;
	int bitrate = 0;
	int samplingRate = 0;
	boolean padding = false;
	boolean priv = false;
	int channel = 0;
	int modeext = 0;
	boolean copyright = false;
	boolean original = false;
	int emphasis = 0;
	
	public Mp3Frame(ByteBuffer b) {
		super(b);
	}
	
	public int getBitrateActual() {
		return BITRATES[bitrate] * 1000;
	}
	public int getSampleRateActual() {
		return RATES[samplingRate];
	}
	
	public int getCalculatedFrameLength() {
		// FrameLen = int((144 * BitRate / SampleRate ) + Padding);
		int len = (int)(144 * getBitrateActual() / getSampleRateActual());
		if(padding) {
			len+=1;
		}
		return len;
	}
	
	public long getTimeLength() {
		if(getBitrateActual() != 0)
			return (getFrameLength() * 8 * 1000)/getBitrateActual();
		else
			return 0;
	}
	
	@Override
	public String toString() {
		return "Mp3Frame[frame=" + getFrameOffset() + ",byteOffset=" + getByteOffset() + ",byteLength=" + getFrameLength() + ",bitrate=" + getBitrateActual()+",timeLength=" + getTimeLength() + ",timeOffset=" +getTimeOffsetAsString() + "]";
	}
}
