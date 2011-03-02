package com.robonobo.mina.stream;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.robonobo.mina.external.buffer.StreamPosition;
import com.robonobo.mina.instance.MinaInstance;
import com.robonobo.mina.message.proto.MinaProtocol.StreamStatus;

/**
 * @syncpriority 90
 */
public class PageRequestMgr {
	static final int MIN_PAGE_WINDOW = 4;
	static final int MAX_PAGE_WINDOW = 32;

	private MinaInstance mina;
	private StreamMgr sm;
	private Set<Long> pendingPages = new HashSet<Long>();
	private SortedSet<Long> overduePages = new TreeSet<Long>();
	private Map<String, StreamPosition> spMap = new HashMap<String, StreamPosition>();
	private Random rand = new Random();
	private long winStart = -1, winEnd = -1;
	private Log log = LogFactory.getLog(getClass());

	public PageRequestMgr(StreamMgr sm) {
		this.mina = sm.getMinaInstance();
		this.sm = sm;
	}

	public synchronized boolean isUsefulSource(StreamPosition pos) {
		if (pos.highestIncludedPage() <= sm.getPageBuffer().getLastContiguousPage())
			return false;
		updateWindow();
		// See if we can get any pages from our window
		for (long pn = winStart; pn <= winEnd; pn++) {
			if (sm.getPageBuffer().haveGotPage(pn) || pendingPages.contains(pn))
				continue;
			if (pos.includesPage(pn))
				return true;
		}
		// If they can't get any from our window, but they can handle things beyond the window (maybe our entire window
		// is in-flight), then they're good
		for (long pn = winEnd + 1; pn <= pos.highestIncludedPage(); pn++) {
			if (sm.getPageBuffer().haveGotPage(pn) || pendingPages.contains(pn))
				continue;
			if (pos.includesPage(pn))
				return true;
		}
		return false;
	}

	/**
	 * Go through our page window, and figure which pages to ask for based on least-common-first
	 * 
	 * @syncpriority 90
	 */
	public synchronized SortedSet<Long> getPagesToRequest(String sourceId, int numPages, Set<Long> alreadyReqdPages) {
		SortedSet<Long> result = new TreeSet<Long>();
		StreamPosition thisSourceSp = spMap.get(sourceId);

		updateWindow();
		// log.debug("PRM requesting " + numPages + " pages for s:" + sm.getStreamId() + "/n:" + sourceId + " win:" +
		// winStart + "-" + winEnd);

		// Count how many sources have each page in this window
		int winSz = (int) ((winEnd - winStart) + 1);
		int[] windowCounts = new int[winSz];
		for (int i = 0; i < windowCounts.length; i++) {
			long thisPn = winStart + i;
			// If we already have this page, or it's pending, or it's beyond the
			// end of the stream, mark it as 0 and don't ask for it
			if (sm.getPageBuffer().haveGotPage(thisPn) || pendingPages.contains(thisPn)
					|| thisPn >= sm.getPageBuffer().getTotalPages()) {
				windowCounts[i] = 0;
				continue;
			}
			int numSources = 0;
			for (StreamPosition sp : spMap.values()) {
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

		// Figure out which pages to ask for
		// sb = new StringBuffer("PRM select: ");
		List<Long> candidates = new ArrayList<Long>();
		nextPage: while (result.size() < numPages) {
			// Overdue pages first
			Iterator<Long> opIter = overduePages.iterator();
			while (opIter.hasNext()) {
				Long pn = opIter.next();
				// Don't ask the source who was supposed to give it to us first
				// time around
				if (thisSourceSp.includesPage(pn) && !alreadyReqdPages.contains(pn)) {
					opIter.remove();
					if (!sm.getPageBuffer().haveGotPage(pn)) {
						// sb.append(pn).append("(o) ");
						result.add(pn);
						continue nextPage;
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
			if (candidates.size() == 0)
				break nextPage;
			else if (candidates.size() == 1) {
				result.add(candidates.get(0));
				// sb.append(candidates.get(0)).append(" ");
			} else {
				Long winner = candidates.get(rand.nextInt(candidates.size()));
				// sb.append("[");
				// for (int i = 0; i < candidates.size(); i++) {
				// if (i > 0)
				// sb.append(",");
				// sb.append(candidates.get(i));
				// }
				// sb.append("]=").append(winner).append(" ");
				result.add(winner);
			}
		}
		// If they can't supply any pages in our window, but they have others
		// after that, ask them for one of those
		if (result.size() == 0 && thisSourceSp.highestIncludedPage() > winEnd) {
			for (long pn = winEnd + 1; pn <= thisSourceSp.highestIncludedPage(); pn++) {
				if (!thisSourceSp.includesPage(pn))
					continue;
				if (sm.getPageBuffer().haveGotPage(pn) || pendingPages.contains(pn))
					continue;
				// sb.append(pn).append("(ex)");
				result.add(pn);
				break;
			}
		}
		// log.debug(sb);
		// Mark these pages as pending
		for (Long pn : result) {
			pendingPages.add(pn);
		}
		return result;
	}

	/**
	 * Must be called only from inside sync block
	 */
	private void updateWindow() {
		// Always grab page 0 & 1 first - hopefully enough to start playback
		if (winStart < 0) {
			winStart = 0;
			winEnd = 1;
		}
		long lastContigPage = sm.getPageBuffer().getLastContiguousPage();
		if (lastContigPage >= winEnd) {
			// We have everything in our window, update
			winStart = lastContigPage + 1;
			winEnd = winStart + windowSize() - 1;
			long totPgs = sm.getPageBuffer().getTotalPages();
			if (winEnd >= totPgs)
				winEnd = totPgs - 1;
		}
	}

	private int windowSize() {
		if (sm.getPageBuffer().getAvgPageSize() <= 0)
			return MIN_PAGE_WINDOW;
		float windowSecs = mina.getConfig().getPageWindowTime() / 1000;
		int windowBytes = (int) (windowSecs * sm.getListeningFlowRate());
		int result = (int) (windowBytes / sm.getPageBuffer().getAvgPageSize());
		if (result < MIN_PAGE_WINDOW)
			result = MIN_PAGE_WINDOW;
		else if (result > MAX_PAGE_WINDOW)
			result = MAX_PAGE_WINDOW;
		return result;
	}

	/**
	 * @syncpriority 90
	 */
	public synchronized void notifyPageReceived(long pageNum) {
		pendingPages.remove(pageNum);
		overduePages.remove(pageNum);
	}

	/**
	 * @syncpriority 90
	 */
	public synchronized void notifyOverduePage(long pageNum) {
		pendingPages.remove(pageNum);
		overduePages.add(pageNum);
	}

	/**
	 * @syncpriority 90
	 */
	public synchronized void notifyStreamStatus(String sourceId, StreamStatus ss) {
		spMap.put(sourceId, new StreamPosition(ss.getLastContiguousPage(), ss.getPageMap()));
	}

	/**
	 * @syncpriority 90
	 */
	public synchronized void notifyDeadConnection(String sourceId) {
		spMap.remove(sourceId);
	}
}
