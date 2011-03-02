package com.robonobo.plugin.mp3;

import java.io.IOException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Super simple mp3 file parser.  Should be quite efficient.
 * 
 * Given readable byte channel, each call to nextFrame() will try to read an Mp3Frame. 
 * The process is lossless, so unknown frames are still returned as such, so the output == input
 * 
 * @see http://www.multiweb.cz/twoinches/MP3inside.htm
 * @author ray
 *
 */
public class Mp3Parser {
	Log log = LogFactory.getLog(getClass());
	ReadableByteChannel in;
	ByteBuffer buffer = ByteBuffer.allocate(4096);
	ByteBuffer frameBuffer = ByteBuffer.allocate(4096);
//	Mp3Frame nextFrame = new Mp3Frame(frameBuffer);
	long realByteOffset = 0;
	long frameOffset = 0;
	long timeOffset = 0;
	
	public Mp3Parser(ReadableByteChannel in) {
		this.in = in;
	}
	
	public void seek(long offset) throws IOException {
		ByteBuffer b = ByteBuffer.allocate(1);
		buffer.clear();
		int read = 0;
		int totalRead=0;
		while(read!=-1 && totalRead<offset) {
			read=in.read(b);
			totalRead+=read;
		}
	}
		
	public Frame nextFrame() throws IOException {
		// read more data into the buffer, 
		in.read(buffer);
		buffer.limit(buffer.position());
		
		// rewind to start for the next batch of parsing
		buffer.rewind();
		
		Frame nextFrame = findNextFrame();
		if(nextFrame != null) {
			// set the frame duration explicitly as the number of bytes copied
			nextFrame.frameLength = copyToFrameBuffer();

			// update the byte offset by the amount of data copied
			realByteOffset+=nextFrame.frameLength;
			frameOffset++;
			if(nextFrame instanceof Mp3Frame) 
				timeOffset+=((Mp3Frame)nextFrame).getTimeLength();
	
			//log.debug("Next frame: " + nextFrame);
		}
		return nextFrame;
	}
	
	protected Frame findNextFrame() {
		Frame nextFrame = null;
		byte b;
		int passed = 0;
		try {
			while(buffer.remaining()>0) {
				b = (byte)buffer.get();
				switch(passed) {
				case 0:
					if((b & 0xFF) == 0xFF) {					// all bits are 1
						// potentially start of header, keep checking
						passed=1;
						nextFrame = new Mp3Frame(frameBuffer);
					}
					break;
				case 1:
					if((b >>> 5 & 0x7) == 0x7) {		// first three bits are 111 
						passed=2;
						((Mp3Frame)nextFrame).version = (b & 0x18) >>> 3;				// 0b00011000
						((Mp3Frame)nextFrame).layer = (b & 0x6) >>> 1;				// 0b00000110
						((Mp3Frame)nextFrame).crc = ((b & 0x1) == 1) ? true : false;	// 0b00000001
						((Mp3Frame)nextFrame).byteOffset = realByteOffset;
						((Mp3Frame)nextFrame).frameOffset = frameOffset;
						((Mp3Frame)nextFrame).timeOffset = timeOffset;
					} else {
						passed=0;
						nextFrame = null;
					}
					break;
				case 2:
					if((b >>> 2 & 0x3F ) != 0x3F) {			// first six bits are NOT 111111
						// seems genuine
						passed=3;
						((Mp3Frame)nextFrame).bitrate = (b & 0xF0) >>> 4;						// 0b11110000
						((Mp3Frame)nextFrame).samplingRate = (b &0xC) >>> 2;					// 0b00001100
						((Mp3Frame)nextFrame).padding = ((b & 0x2) >>> 1 == 1)? true : false;	// 0b00000010
						((Mp3Frame)nextFrame).priv = (b & 0x1) == 1 ? true : false;			// 0b00000001
					} else {
						passed=0;
						nextFrame = null;
					}
					break;
				case 3:
					((Mp3Frame)nextFrame).channel = (b & 0xC0) >>> 6;							// 0b11000000
					((Mp3Frame)nextFrame).modeext = (b & 0x30) >>> 4;							// 0b00110000
					((Mp3Frame)nextFrame).copyright = ((b & 0x8) >>> 3 == 1) ? true:false;	// 0b00001000
					((Mp3Frame)nextFrame).original = ((b & 0x4) >>> 2 == 1) ? true:false;		//0b00000100
					((Mp3Frame)nextFrame).emphasis = (b & 0x3);
					
					// if this is the first frame, then everything before this is unknown data
					if(buffer.position()>4) {					
						buffer.position(buffer.position()-4);
						passed=0;	// parse this bit again
						return new UnknownFrame(frameBuffer,realByteOffset,frameOffset,timeOffset);
					} else {
						passed=4;
					}
					break;
				case 4:
					if(nextFrame instanceof Mp3Frame) {
//						if(((Mp3Frame)nextFrame).getCalculatedFrameLength()<4) {
//							int i = 0;
//						}
						
						// we are in data mode
						// when we reach frameSize, return nextFrame
						if(buffer.position()==((Mp3Frame)nextFrame).getCalculatedFrameLength()) {
							passed=0;
							return nextFrame;
						}
					}
				}
			}
		} 
		catch(BufferUnderflowException e) {
			int i = 0;
			if(passed!=4 && buffer.position()>0)	// if it has not been recognized as an mp3 frame, and there is data, send a unknown frame
				return new UnknownFrame(frameBuffer,realByteOffset,frameOffset,timeOffset);
		}
		
		// if we have read some bytes, but found not header, return as a unknown frame
		if(buffer.position()>0)
			return new UnknownFrame(frameBuffer,realByteOffset,frameOffset,timeOffset);
			
		return nextFrame;
	}
	protected int copyToFrameBuffer() {
		int pos = buffer.position();
		int lim = buffer.limit();
		buffer.rewind();
		buffer.limit(pos);
		
		frameBuffer.clear();
		frameBuffer.limit(pos);
		frameBuffer.put(buffer);
		frameBuffer.rewind();
		
		// restore previous pos and limit
		buffer.position(pos);
		buffer.limit(lim);
		
		// get rid of read data
		buffer.compact();
		
		return pos;
		
		// pos is now at the end of the data, so we can write immediately
	}
}
