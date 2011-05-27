package com.robonobo.mina.external.buffer;

import java.io.IOException;
import java.io.Serializable;
import java.util.List;

public interface PageBuffer extends Serializable {
	/**
	 * Header is page 0
	 * 
	 * @syncpriority 80
	 */
	public void putPage(Page page) throws IOException;

	/**
	 * Header is page 0
	 * 
	 * @syncpriority 80
	 */
	public Page getPage(long pageNum) throws IOException;

	/**
	 * Header is page 0
	 * 
	 * @syncpriority 80
	 */
	public PageInfo getPageInfo(long pageNum);

	/**
	 * Header is page 0
	 * 
	 * @syncpriority 80
	 */
	public boolean haveGotPage(long pageNum);

	/**
	 * Flushes data to disk
	 * 
	 * @syncpriority 80
	 */
	public void flush() throws IOException;

	public void close();

	/**
	 * The total number of pages in the stream, not the number we have. Does not
	 * include header. Value < 0 indicates unknown
	 * 
	 * @syncpriority 80
	 */
	public long getTotalPages();

	/**
	 * Puts all page infos in one go
	 */
	public void putAllPageInfo(List<PageInfo> pageInfos) throws IOException;

	/**
	 * @syncpriority 80
	 */
	public void setTotalPages(long totalPages);

	/**
	 * The lowest page number N for which we have all pages n where 0 <= n <= N
	 * 
	 * @syncpriority 80
	 */
	public long getLastContiguousPage();

	public StreamPosition getStreamPosition();

	public long getAvgPageSize();

	public long getBytesReceived();

	/** Is this a full copy of the stream? */
	public boolean isComplete();

	/**
	 * If true, the page buffer will be nuked on shutdown/startup
	 * 
	 * @return
	 */
	public boolean isTransient();

	/**
	 * Called when no short-term activity is expected, to indicate that the
	 * pagebuf should release any scarce resources, eg file handles. The page
	 * buffer is responsible for waking itself up when the appropriate methods
	 * are called.
	 */
	public void sleep() throws IOException;

	public void addListener(PageBufferListener listener);

	public void removeListener(PageBufferListener listener);

	public String getStreamId();
}
