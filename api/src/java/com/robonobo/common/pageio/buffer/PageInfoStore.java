package com.robonobo.common.pageio.buffer;

import java.io.IOException;
import java.util.List;

import com.robonobo.mina.external.buffer.PageInfo;
import com.robonobo.mina.external.buffer.StreamPosition;

/**
 * Provides for the central management of page info
 * 
 * @author macavity
 * 
 */
public interface PageInfoStore {
	public int getNumPageInfos(String streamId);

	public boolean haveGotPage(String streamId, long pageNum);

	public void putPageInfo(String streamId, PageInfo pi) throws IOException;

	public void putAllPageInfo(String streamId, List<PageInfo> pis) throws IOException;
	
	public PageInfo getPageInfo(String streamId, long pageNum);

	public long getTotalPages(String streamId);
	
	public void setTotalPages(String streamId, long totalPages);
	
	public long getPagesReceived(String streamId);
	
	public long getBytesReceived(String streamId);
	
	public long getLastContiguousPage(String streamId);
	
	public StreamPosition getStreamPosition(String streamId);
}
