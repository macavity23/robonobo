package com.robonobo.mina.external.buffer;

import java.nio.ByteBuffer;

public class Page {
	/** Header is page 0, first page is page 1, etc */
	private long pageNumber;
	/** Offset into stream, in bytes */
	private long byteOffset;
	/** Offset into stream, in ms */
	private long timeOffset;
	private ByteBuffer pageData;
	/** The status marker when this page was sent - allow us to keep track of prices */
	private int auctionStatus;

	public Page() {
	}

	public Page(PageInfo hdr,
			ByteBuffer pageData) {
		this.pageNumber = hdr.getPageNumber();
		this.byteOffset = hdr.getByteOffset();
		this.timeOffset = hdr.getTimeOffset();
		this.auctionStatus = hdr.getAuctionStatus();
		this.pageData = pageData;
	}

	public long getPageNumber() {
		return pageNumber;
	}

	public void setPageNumber(long pageNumber) {
		this.pageNumber = pageNumber;
	}

	public long getByteOffset() {
		return byteOffset;
	}

	public void setByteOffset(long byteOffset) {
		this.byteOffset = byteOffset;
	}

	public long getTimeOffset() {
		return timeOffset;
	}

	public void setTimeOffset(long timeOffset) {
		this.timeOffset = timeOffset;
	}

	public ByteBuffer getData() {
		return pageData;
	}

	public void setData(ByteBuffer pageData) {
		this.pageData = pageData;
	}

	/**
	 * Length of the page data, not including the info block */
	public long getLength() {
		if(pageData == null)
			return 0;
		return pageData.limit();
	}
	
	
	
	public PageInfo getPageInfo() {
		return new PageInfo(getPageNumber(), getByteOffset(), getTimeOffset(), getLength(), getAuctionStatus());
	}
	
	public boolean isHeader() {
		return (pageNumber == 0);
	}
	
	public boolean equals(Object obj) {
		if(!(obj instanceof Page))
			return false;
		Page other = (Page) obj;
		if(!other.getPageInfo().equals(getPageInfo()))
			return false;
		if(!(pageData.limit() == other.getData().limit()))
			return false;
		pageData.position(0);
		other.getData().position(0);
		for(int i=0;i<pageData.limit();i++) {
			byte myByte = pageData.get();
			byte otherByte = other.getData().get();
			if(myByte != otherByte)
				return false;
		}
		return true;
	}
	
	public String toString() {
		return getPageInfo().toString();
	}

	public int getAuctionStatus() {
		return auctionStatus;
	}

	public void setAuctionStatus(int statusIdx) {
		this.auctionStatus = statusIdx;
	}
	
	// For debug only!  Spammy!
//	public String toDebugString() {
//		StringBuffer sb = new StringBuffer(toString());
//		sb.append("[data=");
//		pageData.position(0);
//		for(int i=0;i<pageData.limit();i++) {
//			byte b = pageData.get();
//			if(i != 0)
//				sb.append(", ");
//			sb.append(Integer.toHexString(b & 0xff));
//		}
//		sb.append("]");
//		return sb.toString();
//	}
}
