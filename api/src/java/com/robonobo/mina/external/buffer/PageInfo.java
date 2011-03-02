package com.robonobo.mina.external.buffer;

import java.io.Serializable;

public class PageInfo implements Serializable {
	private static final long serialVersionUID = 1L;
	long pageNumber;
	long byteOffset;
	long timeOffset;
	long length;
	int auctionStatus;

	public PageInfo() {
	}

	public PageInfo(long pageNumber, long byteOffset, long timeOffset, long length, int auctionStatus) {
		setPageNumber(pageNumber);
		setByteOffset(byteOffset);
		setTimeOffset(timeOffset);
		setLength(length);
		setAuctionStatus(auctionStatus);
	}

	public PageInfo(PageInfo pi) {
		this(pi.getPageNumber(), pi.getByteOffset(), pi.getTimeOffset(), pi.getLength(), pi.getAuctionStatus());
	}

	public boolean equals(Object obj) {
		if(!(obj instanceof PageInfo))
			return false;
		PageInfo other = (PageInfo) obj;
		return (other.getPageNumber() == getPageNumber() && other.getTimeOffset() == getTimeOffset()
				&& other.getByteOffset() == getByteOffset() && other.getLength() == getLength());
	}

	public long getByteOffset() {
		return byteOffset;
	}

	public long getLength() {
		return length;
	}

	public long getPageNumber() {
		return pageNumber;
	}

	public long getTimeOffset() {
		return timeOffset;
	}

	public void setByteOffset(long byteOffset) {
		this.byteOffset = byteOffset;
	}

	public void setLength(long length) {
		this.length = length;
	}

	public void setPageNumber(long pageNumber) {
		this.pageNumber = pageNumber;
	}

	public void setTimeOffset(long timeOffset) {
		this.timeOffset = timeOffset;
	}

	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("[num=").append(pageNumber).append(",time=").append(timeOffset);
		sb.append(",byte=").append(byteOffset).append(",len=").append(length).append("]");
		return sb.toString();
	}

	public int getAuctionStatus() {
		return auctionStatus;
	}

	public void setAuctionStatus(int marketStatus) {
		this.auctionStatus = marketStatus;
	}
}