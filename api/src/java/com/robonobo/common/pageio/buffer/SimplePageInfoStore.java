package com.robonobo.common.pageio.buffer;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.robonobo.mina.external.buffer.PageInfo;
import com.robonobo.mina.external.buffer.StreamPosition;

public class SimplePageInfoStore implements PageInfoStore {
	private Map<String, Map<Long, PageInfo>> piMap = new HashMap<String, Map<Long,PageInfo>>();
	private Map<String, Long> bytesRecvd = new HashMap<String, Long>();
	private Map<String, Long> lastContigPage = new HashMap<String, Long>();
	private Map<String, Long> pagesRecvd = new HashMap<String, Long>();
	private Map<String, Long> totalPages = new HashMap<String, Long>();
	
	public SimplePageInfoStore() {
	}
	
	public int getNumPageInfos(String streamId) {
		return piMap.get(streamId).size();
	}

	public PageInfo getPageInfo(String streamId, long pageNum) {
		return piMap.get(streamId).get(pageNum);
	}

	public boolean haveGotPage(String streamId, long pageNum) {
		return piMap.get(streamId).containsKey(pageNum);
	}

	public void init(String streamId) {
		piMap.put(streamId, new HashMap<Long, PageInfo>());
		bytesRecvd.put(streamId, 0L);
		lastContigPage.put(streamId, -1L);
		pagesRecvd.put(streamId, 0L);
		totalPages.put(streamId, -1L);
	}

	public void putPageInfo(String streamId, PageInfo pi) throws IOException {
		piMap.get(streamId).put(pi.getPageNumber(), pi);
		pagesRecvd.put(streamId, pagesRecvd.get(streamId)+1);
		bytesRecvd.put(streamId, bytesRecvd.get(streamId)+pi.getLength());
		long lcp = lastContigPage.get(streamId);
		while(haveGotPage(streamId, lcp+1)) {
			lcp++;
		}
		lastContigPage.put(streamId, lcp);
	}

	public long getBytesReceived(String streamId) {
		return bytesRecvd.get(streamId);
	}

	public long getLastContiguousPage(String streamId) {
		return lastContigPage.get(streamId);
	}

	public StreamPosition getStreamPosition(String streamId) {
		int pageMap = 0;
		long lastContig = getLastContiguousPage(streamId);
		// Start loop at 2 as we know we don't have 1 (or else it would be lastContig)
		for(int i=2;i<=32;i++) {
			if(haveGotPage(streamId, lastContig+i))
				pageMap |= (1 << (i-1));
		}
		return new StreamPosition(lastContig, pageMap);
	}
	
	public long getPagesReceived(String streamId) {
		return pagesRecvd.get(streamId);
	}

	public long getTotalPages(String streamId) {
		return totalPages.get(streamId);
	}

	public void setTotalPages(String streamId, long tp) {
		totalPages.put(streamId, tp);
	}

	public Map<Long, PageInfo> getAllPageInfo(String sid) {
		return piMap.get(sid);
	}
	
	public void putAllPageInfo(String streamId, List<PageInfo> pis) throws IOException {
		for (PageInfo pi : pis) {
			putPageInfo(streamId, pi);
		}
	}
}
