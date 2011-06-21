package com.robonobo.mina.network;

import java.util.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.robonobo.common.exceptions.Errot;
import com.robonobo.common.util.TimeUtil;
import com.robonobo.core.api.proto.CoreApi.EndPoint;
import com.robonobo.mina.external.buffer.PageInfo;
import com.robonobo.mina.instance.MinaInstance;
import com.robonobo.mina.instance.StreamMgr;
import com.robonobo.mina.message.MessageHolder;
import com.robonobo.mina.message.proto.MinaProtocol.ReqPage;
import com.robonobo.mina.message.proto.MinaProtocol.SourceStopping;
import com.robonobo.mina.util.MinaConnectionException;

/**
 * @syncpriority 120
 */
public class BCPair extends ConnectionPair {
	static final char GAMMA = 0x03b3;
	private BroadcastConnection bc;
	private boolean isClosed;
	private Lock reqPageLock = new ReentrantLock(true);

	/**
	 * @param pages
	 * @syncpriority 160
	 */
	public BCPair(MinaInstance mina, String sid, ControlConnection cc, EndPoint listenEp, List<Long> pages) {
		super(mina, sid, cc);
		try {
			bc = cc.getSCF().getBroadcastConnection(cc, listenEp);
			bc.setBCPair(this);
		} catch (MinaConnectionException e) {
			log.error("Error getting broadcast connection to talk to " + listenEp, e);
			die();
		}
		// This will set our gamma
		cc.addBCPair(this);
		log.info("Starting broadcast of " + sid + " to node " + cc.getNodeId());
		requestPages(pages);
	}

	public boolean isClosed() {
		return isClosed;
	}

	@Override
	public int getFlowRate() {
		return bc.getFlowRate();
	}

	/**
	 * @syncpriority 120
	 */
	public void die() {
		die(true);
	}

	/**
	 * @syncpriority 120
	 */
	public void die(boolean sendSourceStopping) {
		log.info("Stopping broadcast of " + sid + " to node " + cc.getNodeId());
		synchronized (this) {
			if (isClosed)
				return;
			isClosed = true;
			if (bc != null)
				bc.close();
		}
		if (sendSourceStopping)
			cc.sendMessage("SourceStopping", SourceStopping.newBuilder().setStreamId(sid).build());
		cc.removeBCPair(this);
		super.die();
	}

	/**
	 * @syncpriority 120
	 */
	public synchronized void abort() {
		if (bc != null)
			bc.close();
	}

	/**
	 * @syncpriority 160
	 */
	public void requestPages(List<Long> pages) {
		if (mina.getCCM().isShuttingDown()) {
			log.debug(this + " not requesting pages - closing down");
			return;
		}
		// Bug huntin
		if (bc == null)
			throw new Errot();
		List<Long> failedPages = null;
		// Use a fair reentrant lock to make sure we don't handle reqpage requests out of order
		reqPageLock.lock();
		try {
			// Get the total amount of data we want to send
			long totalPageLen = 0;
			if (mina.getConfig().isAgoric()) {
				for (Long pn : pages) {
					PageInfo pi = mina.getPageBufProvider().getPageBuf(sid).getPageInfo(pn);
					if (pi != null)
						totalPageLen += pi.getLength();
					else {
						// We will only get here very rarely (should be never, they should never ask), so no point in
						// pointlessly instantiating the list 99.9% of the time
						if (failedPages == null)
							failedPages = new ArrayList<Long>();
						failedPages.add(pn);
						if (log.isDebugEnabled())
							log.debug(this + " requested page " + pn + " which I do not have");
					}
				}
			}
			// Make sure they have enough ends to get these (if their
			// balance is too low, this call will cause them to be told to
			// pay up)
			int auctStatIdx = (mina.getConfig().isAgoric()) ? mina.getSellMgr().requestAndCharge(cc.getNodeId(),
					totalPageLen) : 0;
			if (auctStatIdx < 0) {
				// Ask again when they've paid up
				ReqPage rp = ReqPage.newBuilder().setStreamId(sid).addAllRequestedPage(pages).build();
				MessageHolder mh = new MessageHolder("ReqPage", rp, cc, TimeUtil.now());
				if (!mina.getSellMgr().haveActiveAccount(cc.getNodeId())) {
					mina.getSellMgr().msgPendingActiveAccount(mh);
					return;
				}
				if (!mina.getSellMgr().haveAgreedBid(cc.getNodeId())) {
					mina.getSellMgr().msgPendingAgreedBid(mh);
					return;
				}
				// wtf? we have an account and an agreed bid, something's not right
				log.error(this+" failed to charge sellmgr, but i'm not sure why!");
			} else {
				for (Long pn : pages) {
					if (failedPages == null || !failedPages.contains(pn))
						bc.addPageToQ(pn, auctStatIdx);
				}
			}
		} finally {
			reqPageLock.unlock();
		}
		// If they asked us for a page we didn't have, tell them where we are in the stream
		if (failedPages != null)
			cc.sendMessage("StreamStatus", mina.getStreamMgr().buildStreamStatus(sid, cc.getNodeId()));
	}

	public void setGamma(float gamma) {
		if (log.isDebugEnabled())
			log.debug(this + ": setting " + GAMMA + " to " + gamma);
		bc.setGamma(gamma);
	}

	public boolean equals(Object obj) {
		if (obj instanceof BCPair)
			return (hashCode() == obj.hashCode());
		else
			return false;
	}

	public int hashCode() {
		return getClass().getName().hashCode() ^ cc.getNodeId().hashCode() ^ sid.hashCode();
	}

	public String toString() {
		return "BCP[node=" + cc.getNodeId() + ",stream=" + sid + "]";
	}
}
