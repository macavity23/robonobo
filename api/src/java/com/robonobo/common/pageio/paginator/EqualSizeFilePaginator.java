package com.robonobo.common.pageio.paginator;

import java.io.IOException;
import java.nio.channels.ByteChannel;
import java.util.ArrayList;
import java.util.List;

import com.robonobo.mina.external.buffer.PageBuffer;
import com.robonobo.mina.external.buffer.PageInfo;

/**
 * This is a cheat paginator that just assumes all pages are the same size and duration.
 * This doesn't actually move any data around, as we already have the whole file.
 * Bogus, but very fast.
 */
public class EqualSizeFilePaginator implements Paginator {
	private long totalSz;
	private int pageSz;
	/** secs */
	private int totalTime;
	
	public EqualSizeFilePaginator(int pageSize, long totalSize, int totalTime) {
		this.pageSz = pageSize;
		this.totalSz = totalSize;
		this.totalTime = totalTime;
	}


	public void paginate(ByteChannel c, PageBuffer pb) throws IOException {
		long totalMillis = totalTime * 1000;
		int numPages = 	(int) ((totalSz % pageSz == 0) ? (totalSz / pageSz) : (totalSz / pageSz) + 1);
		int pageNum=0;
		List<PageInfo> pageInfos = new ArrayList<PageInfo>();
		for(long byteOffset=0; byteOffset < totalSz; byteOffset += pageSz) {
			long timeOffset = pageNum * (totalMillis / numPages);
			int thisPageSz = pageSz;
			if((pageNum == numPages - 1) && (totalSz % pageSz != 0)) {
				// Last page will be smaller
				thisPageSz = (int) (totalSz % pageSz);
			}
			PageInfo pi = new PageInfo(pageNum, byteOffset, timeOffset, thisPageSz, 0);
			pageInfos.add(pi);
			pageNum++;
		}
		pb.putAllPageInfo(pageInfos);
	}
}
