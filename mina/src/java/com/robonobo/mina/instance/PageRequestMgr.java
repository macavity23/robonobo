package com.robonobo.mina.instance;

import java.util.*;

import org.apache.commons.logging.Log;

import com.robonobo.mina.external.buffer.PageBuffer;
import com.robonobo.mina.external.buffer.StreamPosition;
import com.robonobo.mina.message.proto.MinaProtocol.StreamStatus;

/**
 * Handles what pages to request when
 * 
 * @author macavity
 * @syncpriority 90
 */
public class PageRequestMgr {
	static final int MIN_PAGE_WINDOW = 4;
	static final int MAX_PAGE_WINDOW = 32;

	private MinaInstance mina;
	private Map<String, Set<Long>> pendingPages = new HashMap<String, Set<Long>>();
	private Map<String, SortedSet<Long>> overduePages = new HashMap<String, SortedSet<Long>>();
	/** Map<streamid, Map<sourceNodeId, streampos>> */
	private Map<String, Map<String, StreamPosition>> spMap = new HashMap<String, Map<String, StreamPosition>>();
	private Map<String, Long> winStartMap = new HashMap<String, Long>();
	private Map<String, Long> winEndMap = new HashMap<String, Long>();
	private Random rand = new Random();
	private Log log;

	public PageRequestMgr(MinaInstance mina) {
		this.mina = mina;
		log = mina.getLogger(getClass());
	}

	/**
	 * @syncpriority 90
	 */
	public synchronized boolean isUsefulSource(String sid, StreamPosition pos) {
		PageBuffer pb = mina.getPageBufProvider().getPageBuf(sid);
		if (pos.highestIncludedPage() <= pb.getLastContiguousPage())
			return false;
		updateWindow(sid, pb);
		long winStart = winStartMap.get(sid);
		long winEnd = winEndMap.get(sid);
		// See if we can get any pages from our window
		for (long pn = winStart; pn <= winEnd; pn++) {
			if (pb.haveGotPage(pn) || havePendingPage(sid, pn))
				continue;
			if (pos.includesPage(pn))
				return true;
		}
		// If they can't get any from our window, but they can handle things beyond the window (maybe our entire window
		// is in-flight), then they're good
		for (long pn = winEnd + 1; pn <= pos.highestIncludedPage(); pn++) {
			if (pb.haveGotPage(pn) || havePendingPage(sid, pn))
				continue;
			if (pos.includesPage(pn))
				return true;
		}
		return false;
	}

	/**
	 * Must be called only from inside sync block
	 */
	private boolean havePendingPage(String sid, long pageNum) {
		return (pendingPages.containsKey(sid) && pendingPages.get(sid).contains(pageNum));
	}

	/**
	 * Must be called only from inside sync block
	 */
	private void addPendingPage(String sid, Long pageNum) {
		if(!pendingPages.containsKey(sid))
			pendingPages.put(sid, new HashSet<Long>());
		pendingPages.get(sid).add(pageNum);
	}
	
	/**
	 * Go through our page window, and figure which pages to ask for based on least-common-first
	 * 
	 * @syncpriority 90
	 */
	public synchronized SortedSet<Long> getPagesToRequest(String sid, String sourceId, int numPages,
			Set<Long> alreadyReqdPages) {
		SortedSet<Long> result = new TreeSet<Long>();
		StreamPosition thisSourceSp = spMap.get(sid).get(sourceId);
		PageBuffer pb = mina.getPageBufProvider().getPageBuf(sid);
		updateWindow(sid, pb);
		long winStart = winStartMap.get(sid);
		long winEnd = winEndMap.get(sid);
		// log.debug("PRM requesting " + numPages + " pages for s:" + sm.getStreamId() + "/n:" + sourceId + " win:" +
		// winStart + "-" + winEnd);

		// Count how many sources have each page in this window
		int winSz = (int) ((winEnd - winStart) + 1);
		int[] windowCounts = new int[winSz];
		for (int i = 0; i < windowCounts.length; i++) {
			long thisPn = winStart + i;
			// If we already have this page, or it's pending, or it's beyond the
			// end of the stream, mark it as 0 and don't ask for it
			if (pb.haveGotPage(thisPn) || havePendingPage(sid, thisPn) || thisPn >= pb.getTotalPages()) {
				windowCounts[i] = 0;
				continue;
			}
			int numSources = 0;
			for (StreamPosition sp : spMap.get(sid).values()) {
				if (sp.includesPage(thisPn))
					numSources++;
			}
			windowCounts[i] = numSources;
		}
		// StringBuffer sb = new StringBuffer("PRM win counts: ");
		// for (int i = 0; i < windowCounts.length; i++) {
		// if (i > 0)
		// sb.append(", ");
		// sb.append(windowCounts[i]);
		// }
		// log.debug(sb);

		// sb = new StringBuffer("PRM select: ");
		// Figure out which pages to ask for
		List<Long> candidates = new ArrayList<Long>();
		nextPage: while (result.size() < numPages) {
			// Overdue pages first
			if (overduePages.containsKey(sid)) {
				Iterator<Long> opIter = overduePages.get(sid).iterator();
				while (opIter.hasNext()) {
					Long pn = opIter.next();
					// Don't ask the source who was supposed to give it to us first
					// time around
					if (thisSourceSp.includesPage(pn) && !alreadyReqdPages.contains(pn)) {
						opIter.remove();
						if (!pb.haveGotPage(pn)) {
							// sb.append(pn).append("(o) ");
							result.add(pn);
							continue nextPage;
						}
					}
				}
			}
			// Now find the least-common pages
			int minCount = Integer.MAX_VALUE;
			candidates.clear();
			// sb.append("{ ");
			for (int i = 0; i < windowCounts.length; i++) {
				int thisCount = windowCounts[i];
				if (thisCount == 0)
					continue;
				Long thisPn = winStart + i;
				if (result.contains(thisPn))
					continue;
				if (!thisSourceSp.includesPage(thisPn))
					continue;
				if (thisCount == minCount) {
					candidates.add(thisPn);
					// sb.append("+c:").append(thisPn).append(" ");
				} else if (thisCount < minCount) {
					candidates.clear();
					// sb.append(".c:").append(thisPn).append(" ");
					candidates.add(thisPn);
					minCount = thisCount;
				}
			}
			// sb.append("} ");
			// Now randomly pick one of the least-common pages
			if (candidates.size() == 0)
				break nextPage;
			else if (candidates.size() == 1) {
				result.add(candidates.get(0));
				// sb.append(candidates.get(0)).append(" ");
			} else {
				Long winrar = candidates.get(rand.nextInt(candidates.size()));
				result.add(winrar);
				// sb.append("[");
				// for (int i = 0; i < candidates.size(); i++) {
				// if (i > 0)
				// sb.append(",");
				// sb.append(candidates.get(i));
				// }
				// sb.append("]=").append(winrar).append(" ");
			}
		}
		// If they can't supply any pages in our window, but they have others
		// after that, ask them for one of those
		if (result.size() == 0 && thisSourceSp.highestIncludedPage() > winEnd) {
			for (long pn = winEnd + 1; pn <= thisSourceSp.highestIncludedPage(); pn++) {
				if (!thisSourceSp.includesPage(pn))
					continue;
				if (pb.haveGotPage(pn) || havePendingPage(sid, pn))
					continue;
				// sb.append(pn).append("(ex)");
				result.add(pn);
				break;
			}
		}
		// log.debug(sb);
		// Mark these pages as pending
		for (Long pn : result) {
			addPendingPage(sid, pn);
		}
		return result;
	}

	/**
	 * Must be called only from inside sync block
	 */
	private void updateWindow(String sid, PageBuffer pb) {
		// Always grab page 0 & 1 first - hopefully enough to start playback
		if (!winStartMap.containsKey(sid)) {
			winStartMap.put(sid, 0L);
			winEndMap.put(sid, 1L);
		}
		long lastContigPage = pb.getLastContiguousPage();
		if (lastContigPage >= winEndMap.get(sid)) {
			// We have everything in our window, update
			long winStart = lastContigPage + 1;
			long winEnd = winStart + windowSize(sid, pb) - 1;
			long totPgs = pb.getTotalPages();
			if (winEnd >= totPgs)
				winEnd = totPgs - 1;
			winStartMap.put(sid, winStart);
			winEndMap.put(sid, winEnd);
		}
	}

	/**
	 * Must be called only from inside sync block
	 */
	private int windowSize(String sid, PageBuffer pb) {
		if (pb.getAvgPageSize() <= 0)
			return MIN_PAGE_WINDOW;
		float windowSecs = mina.getConfig().getPageWindowTime() / 1000;
		int windowBytes = (int) (windowSecs * mina.getFlowRateMgr().getListeningFlowRate(sid));
		int result = (int) (windowBytes / pb.getAvgPageSize());
		if (result < MIN_PAGE_WINDOW)
			result = MIN_PAGE_WINDOW;
		else if (result > MAX_PAGE_WINDOW)
			result = MAX_PAGE_WINDOW;
		return result;
	}

	/**
	 * @syncpriority 90
	 */
	public synchronized void notifyPageReceived(String sid, long pageNum) {
		pendingPages.get(sid).remove(pageNum);
		if(overduePages.containsKey(sid))
			overduePages.get(sid).remove(pageNum);
	}

	/**
	 * @syncpriority 90
	 */
	public synchronized void notifyOverduePage(String sid, Long pageNum) {
		if(!pendingPages.containsKey(sid)) {
			// Not receiving this any more, we don't care
			return;
		}
		pendingPages.get(sid).remove(pageNum);
		if(!overduePages.containsKey(sid))
			overduePages.put(sid, new TreeSet<Long>());
		overduePages.get(sid).add(pageNum);
	}

	/**
	 * @syncpriority 90
	 */
	public synchronized void notifyStreamStatus(String sid, String sourceId, StreamStatus ss) {
		if(!spMap.containsKey(sid))
			spMap.put(sid, new HashMap<String, StreamPosition>());
		spMap.get(sid).put(sourceId, new StreamPosition(ss.getLastContiguousPage(), ss.getPageMap()));
	}

	/**
	 * @syncpriority 90
	 */
	public synchronized void notifyDeadConnection(String sid, String sourceNodeId) {
		if(spMap.containsKey(sid))
			spMap.get(sid).remove(sourceNodeId);
	}
	
	/**
	 * @syncpriority 90
	 */
	public synchronized void cleanupStream(String sid) {
		overduePages.remove(sid);
		pendingPages.remove(sid);
		spMap.remove(sid);
		winStartMap.remove(sid);
		winEndMap.remove(sid);
	}
}
