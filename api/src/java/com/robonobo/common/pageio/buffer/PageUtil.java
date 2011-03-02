package com.robonobo.common.pageio.buffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.ByteChannel;


import com.robonobo.common.dlugosz.Dlugosz;
import com.robonobo.mina.external.buffer.Page;
import com.robonobo.mina.external.buffer.PageInfo;

/**
 * Removed the encoding stuff from the Page and PageInfo beans so they are just POJOs
 * This is only really relevant to mina, so they need not be in the external API
 * Also, removed robonobo-common dependency on mina-api
 * @author ray
 *
 */
public class PageUtil {
	// 4 fields
	private static final int MAX_PAGEINFO_SZ = 4 * Dlugosz.bytesToEncode(Long.MAX_VALUE);

	public static PageInfo decodePageInfo(ByteChannel chan) throws IOException {
		PageInfo pi = new PageInfo();
		pi.setPageNumber(Dlugosz.readLong(chan));
		pi.setLength(Dlugosz.readLong(chan));
		pi.setByteOffset(Dlugosz.readLong(chan));
		pi.setTimeOffset(Dlugosz.readLong(chan));
		return pi;
	}
	
	public static void encodePageInfo(PageInfo pi, ByteBuffer buf) {
		Dlugosz.encode(pi.getPageNumber(), buf);
		Dlugosz.encode(pi.getLength(), buf);
		Dlugosz.encode(pi.getByteOffset(), buf);
		Dlugosz.encode(pi.getTimeOffset(), buf);
	}
	
	public static int encodedPageSize(Page p) {
		return Dlugosz.bytesToEncode(p.getPageNumber()) +
		Dlugosz.bytesToEncode(p.getByteOffset()) +
		Dlugosz.bytesToEncode(p.getTimeOffset()) +
		p.getData().limit();
	}
	
	public static int encodedPageInfoSize(PageInfo pi) {
		return Dlugosz.bytesToEncode(pi.getPageNumber())
				+ Dlugosz.bytesToEncode(pi.getByteOffset())
				+ Dlugosz.bytesToEncode(pi.getTimeOffset())
				+ Dlugosz.bytesToEncode(pi.getLength());
	}
	
	public static int maxPageInfoSize() {
		return MAX_PAGEINFO_SZ;
	}
	
	public static Page decodePage(ByteBuffer buf) throws IOException{
		Page p = new Page();
		buf.position(0);
		p.setPageNumber(Dlugosz.readLong(buf));
		p.setByteOffset(Dlugosz.readLong(buf));
		p.setTimeOffset(Dlugosz.readLong(buf));
		p.setData(buf.slice());
		return p;
	}
	
	/**
	 * Gets the page, including metadata, as a byte buffer ready for sending
	 * over the network
	 */
	public static ByteBuffer pageToByteBuffer(Page p) {
		int bufLen = encodedPageSize(p);
		
		ByteBuffer result = ByteBuffer.allocate(bufLen);
		Dlugosz.encode(p.getPageNumber(), result);
		Dlugosz.encode(p.getByteOffset(), result);
		Dlugosz.encode(p.getTimeOffset(), result);
		p.getData().flip();
		result.put(p.getData());
		
		return result;
	}
	
	
}
