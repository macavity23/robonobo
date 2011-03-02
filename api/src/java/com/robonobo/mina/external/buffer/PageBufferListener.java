package com.robonobo.mina.external.buffer;

public interface PageBufferListener {
	public void gotPage(PageBuffer pb, long pageNum);
	public void advisedOfTotalPages(PageBuffer pb);
}
