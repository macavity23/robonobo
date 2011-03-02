package com.robonobo.common.pageio.buffer;

import java.io.IOException;
import java.nio.ByteBuffer;

import com.robonobo.common.dlugosz.Dlugosz;
import com.robonobo.mina.external.buffer.Page;
import com.robonobo.mina.external.buffer.PageInfo;


public class PageSerializer {
	public PageSerializer() {
	}

	/**
	 * Writes a page to the supplied buffer. If this buffer is not big enough,
	 * Bad Things will happen
	 */
	public void serializePage(Page p, ByteBuffer buf) {
		// When we serialize a page, we put a dlugosz-encoded number at the
		// beginning, which is the total size of the encoded page (not including
		// the size field itself)
		long size = Dlugosz.bytesToEncode(p.getPageNumber())
				+ Dlugosz.bytesToEncode(p.getByteOffset())
				+ Dlugosz.bytesToEncode(p.getTimeOffset())
				+ Dlugosz.bytesToEncode(p.getAuctionStatus())
				+ p.getLength();
		Dlugosz.encode(size, buf);
		Dlugosz.encode(p.getPageNumber(), buf);
		Dlugosz.encode(p.getByteOffset(), buf);
		Dlugosz.encode(p.getTimeOffset(), buf);
		Dlugosz.encode(p.getAuctionStatus(), buf);
		p.getData().position(0);
		buf.put(p.getData());
	}

	/**
	 * The size of the supplied page, in bytes, when it has been serialized
	 */
	public int sizeOfSerializedPage(Page p) {
		long size = Dlugosz.bytesToEncode(p.getPageNumber())
				+ Dlugosz.bytesToEncode(p.getByteOffset())
				+ Dlugosz.bytesToEncode(p.getTimeOffset())
				+ Dlugosz.bytesToEncode(p.getAuctionStatus())
				+ p.getLength();
		// Include the page-total-duration param at the beginning
		return (int) (size + Dlugosz.bytesToEncode(size));
	}

	/**
	 * Returns true iff the supplied buffer contains a complete serialized page,
	 * starting at the buffer's current position. The buffer's position is left
	 * unchanged.
	 */
	public boolean containsCompletePage(ByteBuffer buf) throws IOException {
		if (!Dlugosz.startsWithCompleteNum(buf))
			return false;
		int origPos = buf.position();
		long pageSize = Dlugosz.readLong(buf);
		boolean result = (buf.remaining() >= pageSize);
		buf.position(origPos);
		return result;
	}

	/**
	 * Deserializes a page from the supplied buffer, starting from the buffer's
	 * current position. When this method returns, the buffer's position will
	 * point to the first byte after the end of this page.
	 * @throws IOException if the buffer does not contain a complete page, or
	 * an io issue occurs
	 */
	public Page deserializePage(ByteBuffer buf) throws IOException {
		if(!containsCompletePage(buf))
			throw new IOException("Buffer does not contain complete page");
		long pageSize = Dlugosz.readLong(buf);
		int posBeforeHeader = buf.position();
		long pageNumber = Dlugosz.readLong(buf);
		long byteOffset = Dlugosz.readLong(buf);
		long timeOffset = Dlugosz.readLong(buf);
		int auctionStatus = (int) Dlugosz.readLong(buf);
		int headerSize = buf.position() - posBeforeHeader;
		int payloadSize = (int) (pageSize - headerSize);
		ByteBuffer payload = ByteBuffer.allocate(payloadSize);
		payload.put(buf.array(), buf.position(), payload.limit());
		buf.position(buf.position()+payload.limit());
		payload.flip();
		return new Page(new PageInfo(pageNumber, byteOffset, timeOffset, payload.limit(), auctionStatus), payload);
	}
}
