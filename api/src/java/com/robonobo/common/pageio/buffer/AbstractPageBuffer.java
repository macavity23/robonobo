package com.robonobo.common.pageio.buffer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import com.robonobo.mina.external.buffer.Page;
import com.robonobo.mina.external.buffer.PageBuffer;
import com.robonobo.mina.external.buffer.PageBufferListener;
import com.robonobo.mina.external.buffer.PageInfo;
import com.robonobo.mina.external.buffer.StreamPosition;

/**
 * @syncpriority 80
 */
abstract public class AbstractPageBuffer implements PageBuffer {
	private static final long serialVersionUID = 1L;
	protected String streamId;
	protected transient PageInfoStore pageInfoStore;
	protected transient List<PageBufferListener> listeners = new ArrayList<PageBufferListener>();

	public AbstractPageBuffer(String streamId) {
		this.streamId = streamId;
	}

	public AbstractPageBuffer(String streamId, PageInfoStore pageInfoStore) {
		this.streamId = streamId;
		this.pageInfoStore = pageInfoStore;
	}

	public void close() {
		try {
			flush();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void flush() throws IOException {
	}

	public long getAvgPageSize() {
		long pagesRecvd = getPagesReceived();
		if (pagesRecvd == 0)
			return 0;
		return getBytesReceived() / pagesRecvd;
	}

	public long getPagesReceived() {
		return pageInfoStore.getNumPageInfos(streamId);
	}

	public long getBytesReceived() {
		return pageInfoStore.getBytesReceived(streamId);
	}

	public long getTotalPages() {
		return pageInfoStore.getTotalPages(streamId);
	}

	public void setTotalPages(long totalPages) {
		pageInfoStore.setTotalPages(streamId, totalPages);
	}

	/**
	 * @return < 0 if this buffer holds no pages
	 */
	public long getLastContiguousPage() {
		return pageInfoStore.getLastContiguousPage(streamId);
	}

	public StreamPosition getStreamPosition() {
		return pageInfoStore.getStreamPosition(streamId);
	}
	
	/**
	 * @syncpriority 80
	 */
	public synchronized Page getPage(long pageNum) throws IOException {
		if (!haveGotPage(pageNum))
			return null;
		PageInfo pi = getPageInfo(pageNum);
		ByteBuffer buf = getPageData(pi);
		Page result = new Page(pi, buf);
		return result;
	}

	public PageInfoStore getPageInfoStore() {
		return pageInfoStore;
	}

	public boolean haveGotPage(long pageNum) {
		return pageInfoStore.haveGotPage(streamId, pageNum);
	}

	public boolean isTransient() {
		return false;
	}

	public boolean isComplete() {
		long totalPages = getTotalPages();
		return (totalPages > 0 && totalPages == (getLastContiguousPage() + 1));
	}

	/**
	 * @syncpriority 80
	 */
	public void putPage(Page p) throws IOException {
		if (haveGotPage(p.getPageNumber()))
			return;
		PageInfo pi = p.getPageInfo();
		PageBufferListener[] lisArr;
		synchronized (this) {
			ByteBuffer pageData = p.getData();
			pageData.position(0);
			putPageData(pi, pageData);
			putPageInfo(pi);
			lisArr = new PageBufferListener[listeners.size()];
			listeners.toArray(lisArr);
		}
		for (PageBufferListener listener : lisArr) {
			listener.gotPage(this, pi.getPageNumber());
		}
	}

	public void setPageInfoStore(PageInfoStore pageInfoStore) {
		this.pageInfoStore = pageInfoStore;
	}

	public String getStreamId() {
		return streamId;
	}

	protected abstract ByteBuffer getPageData(PageInfo pageInfo) throws IOException;

	public PageInfo getPageInfo(long pageNum) {
		return pageInfoStore.getPageInfo(streamId, pageNum);
	}

	protected abstract void putPageData(PageInfo pageInfo, ByteBuffer pageData) throws IOException;

	protected void putPageInfo(PageInfo pi) throws IOException {
		pageInfoStore.putPageInfo(streamId, pi);
	}

	public synchronized void addListener(PageBufferListener listener) {
		listeners.add(listener);
	}

	public synchronized void removeListener(PageBufferListener listener) {
		listeners.remove(listener);
	}
}
