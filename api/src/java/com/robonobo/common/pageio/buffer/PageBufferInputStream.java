package com.robonobo.common.pageio.buffer;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.robonobo.mina.external.buffer.Page;
import com.robonobo.mina.external.buffer.PageBuffer;
import com.robonobo.mina.external.buffer.PageBufferListener;

/**
 * Reads the pages in order from a pagebuffer and makes the data available as an
 * inputstream
 * 
 * @author macavity
 */
public class PageBufferInputStream extends InputStream implements PageBufferListener {
	private static final boolean LOG_PBIS = false;
	private Log log = LogFactory.getLog(getClass());
	private PageBuffer pb;
	private long nextPageNum = 0;
	private Page curPage = null;
	private long markedPageNum;
	private int markedPagePosition;

	public PageBufferInputStream(PageBuffer pageBuf) {
		this.pb = pageBuf;
		pageBuf.addListener(this);
	}

	@Override
	public int read(byte[] b) throws IOException {
		return read(b, 0, b.length);
	}

	@Override
	public synchronized int read() throws IOException {
		if(LOG_PBIS)
			log.debug("PBIS read()");
		if (haveReachedEnd())
			return -1;
		if (curPage != null && curPage.getData().remaining() == 0) {
			curPage = null;
			nextPageNum++;
		}
		if (curPage == null) {
			try {
				waitForNextPage();
				if (haveReachedEnd())
					return -1;
			} catch (InterruptedException e) {
				throw new IOException("Interrupted while waiting for page");
			}
		}
		return curPage.getData().get();
	}

	@Override
	public synchronized int read(byte[] b, int off, int len) throws IOException {
		if(LOG_PBIS)
			log.debug("PBIS read(byte[], "+len+"b)");
		int bytesRead = 0;
		int leftToRead = len;
		if (haveReachedEnd())
			return -1;
		while (leftToRead > 0 && !haveReachedEnd()) {
			if (curPage != null && curPage.getData().remaining() == 0) {
				nextPageNum = curPage.getPageNumber() + 1;
				curPage = null;
			}
			if (curPage == null) {
				try {
					waitForNextPage();
					if (haveReachedEnd())
						return bytesRead;
				} catch (InterruptedException e) {
					throw new IOException("Interrupted while waiting for page");
				}
			}
			int thisRead = (leftToRead < curPage.getData().remaining()) ? leftToRead : curPage.getData().remaining();
			curPage.getData().get(b, off + bytesRead, thisRead);
			bytesRead += thisRead;
			leftToRead -= thisRead;
		}
		return bytesRead;
	}

	@Override
	public long skip(long n) throws IOException {
		if(LOG_PBIS)
			log.debug("PBIS skip("+n+")");
		long bytesSkipped = 0;
		long leftToSkip = n;
		if (haveReachedEnd())
			return -1;
		while(leftToSkip > 0 && !haveReachedEnd()) {
			if (curPage != null && curPage.getData().remaining() == 0) {
				nextPageNum = curPage.getPageNumber() + 1;
				curPage = null;
			}
			if (curPage == null) {
				try {
					waitForNextPage();
					if (haveReachedEnd())
						return bytesSkipped;
				} catch (InterruptedException e) {
					throw new IOException("Interrupted while waiting for page");
				}
			}
			int thisSkip = (int) ((leftToSkip < curPage.getData().remaining()) ? leftToSkip : curPage.getData().remaining());
			curPage.getData().position(curPage.getData().position() + thisSkip);
			bytesSkipped += thisSkip;
			leftToSkip -= thisSkip;
		}
		return bytesSkipped;
	}
	
	@Override
	public boolean markSupported() {
		return true;
	}

	@Override
	public synchronized void mark(int readlimit) {
		if(LOG_PBIS)
			log.debug("PBIS mark: "+readlimit);
		if (curPage == null) {
			markedPageNum = nextPageNum;
			markedPagePosition = 0;
		} else {
			markedPageNum = curPage.getPageNumber();
			markedPagePosition = curPage.getData().position();
		}
	}

	@Override
	public synchronized void reset() throws IOException {
		if(LOG_PBIS)
			log.debug("PBIS reset()");
		curPage = pb.getPage(markedPageNum);
		curPage.getData().position(markedPagePosition);
	}

	@Override
	public void close() throws IOException {
		pb.removeListener(this);
	}

	public synchronized void gotPage(PageBuffer pb, long pageNum) {
		if (pageNum == nextPageNum)
			notifyAll();
	}

	public synchronized void advisedOfTotalPages(PageBuffer pb) {
		// Our reader might be waiting on a page that never comes, so wake them
		// up
		notifyAll();
	}

	private void waitForNextPage() throws InterruptedException, IOException {
		while (curPage == null) {
			if (haveReachedEnd())
				return;
			if (pb.haveGotPage(nextPageNum)) {
				curPage = pb.getPage(nextPageNum);
				curPage.getData().position(0);
				return;
			} else {
				synchronized (this) {
					wait();
				}
			}
		}
	}

	private boolean haveReachedEnd() {
		boolean result = pb.getTotalPages() >= 0 && nextPageNum >= pb.getTotalPages();
		return result;
	}
}
