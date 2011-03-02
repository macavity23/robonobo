package com.robonobo.mina.agoric;

import static com.robonobo.common.util.NumberUtil.*;
import static com.robonobo.common.util.TextUtil.*;
import static com.robonobo.common.util.TimeUtil.*;

import java.util.*;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;

import com.google.protobuf.ByteString;
import com.robonobo.common.concurrent.Attempt;
import com.robonobo.common.concurrent.CatchingRunnable;
import com.robonobo.common.exceptions.SeekInnerCalmException;
import com.robonobo.common.util.NumberUtil;
import com.robonobo.core.api.CurrencyException;
import com.robonobo.core.api.StreamVelocity;
import com.robonobo.mina.external.buffer.PageBuffer;
import com.robonobo.mina.instance.MinaInstance;
import com.robonobo.mina.message.proto.MinaProtocol.Agorics;
import com.robonobo.mina.message.proto.MinaProtocol.Bid;
import com.robonobo.mina.message.proto.MinaProtocol.BidUpdate;
import com.robonobo.mina.message.proto.MinaProtocol.CloseAcct;
import com.robonobo.mina.message.proto.MinaProtocol.EscrowBegan;
import com.robonobo.mina.message.proto.MinaProtocol.ReceivedBid;
import com.robonobo.mina.message.proto.MinaProtocol.SourceStatus;
import com.robonobo.mina.message.proto.MinaProtocol.TopUp;
import com.robonobo.mina.network.ControlConnection;
import com.robonobo.mina.network.LCPair;

/**
 * Handles accounts we have with other nodes, and their auction states
 * @syncpriority 170
 * @author macavity
 */
public class BuyMgr {
	static final int AUCTION_STATE_HISTORY = 8;
	MinaInstance mina;
	Map<String, AuctionState> asMap = new HashMap<String, AuctionState>();
	Map<String, Agorics> agMap = new HashMap<String, Agorics>();
	Map<String, Account> accounts = new HashMap<String, Account>();
	Set<String> accountsInProgress = new HashSet<String>();
	Log log;

	public BuyMgr(MinaInstance mina) {
		this.mina = mina;
		log = mina.getLogger(getClass());
	}

	/**
	 * @syncpriority 170
	 */
	public synchronized AuctionState getAuctionState(String nodeId) {
		return asMap.get(nodeId);
	}

	/**
	 * @syncpriority 170
	 */
	public synchronized Agorics getAgorics(String nodeId) {
		return agMap.get(nodeId);
	}

	/**
	 * @syncpriority 170
	 */
	public synchronized boolean haveActiveAccount(String nodeId) {
		return accounts.containsKey(nodeId);
	}

	/**
	 * @syncpriority 170
	 * @return <=0 if no agreed bid yet
	 */
	public synchronized double getAgreedBidTo(String nodeId) {
		if (!accounts.containsKey(nodeId))
			return -1;
		AuctionState as = accounts.get(nodeId).getMostRecentAs();
		if (as == null)
			return -1;
		return as.getMyBid();
	}

	/**
	 * @syncpriority 170
	 */
	public synchronized float calculateMyGamma(String nodeId) {
		Account ac = accounts.get(nodeId);
		if (ac == null)
			return 0f;
		AuctionState as = ac.getMostRecentAs();
		if (as == null)
			return 0f;
		double myBid = as.getMyBid();
		if (myBid == 0d)
			return 0f;
		double topBid = as.getTopBid();
		return (float) (myBid / topBid);
	}

	/**
	 * Gets the most recent auction status index in nodeId's auction
	 * 
	 * @return -1 If no auction status yet received
	 * @syncpriority 170
	 */
	public synchronized int getCurrentStatusIdx(String nodeId) {
		if (!accounts.containsKey(nodeId))
			return -1;
		AuctionState as = accounts.get(nodeId).getMostRecentAs();
		if (as == null)
			return -1;
		return as.getIndex();
	}

	/**
	 * @syncpriority 170
	 */
	public void setupAccount(SourceStatus ss) {
		String nodeId = ss.getFromNode().getId();
		// If our currency client isn't ready yet (fast connection, this one!),
		// wait until it is
		while (!mina.getCurrencyClient().isReady()) {
			try {
				Thread.sleep(100L);
			} catch (InterruptedException e) {
				return;
			}
			if (mina.getCCM().isShuttingDown())
				return;
		}
		synchronized (this) {
			if (accounts.containsKey(nodeId) || accountsInProgress.contains(nodeId))
				return;
			accountsInProgress.add(nodeId);
			asMap.put(nodeId, new AuctionState(ss.getAuctionState()));
			agMap.put(nodeId, ss.getAgorics());
		}
		String paymentMethod = getBestPaymentMethod(ss.getAgorics());
		if (paymentMethod == null) {
			log.info("Failed to setup account with " + nodeId + " - no acceptable payment methods");
			return;
		}
		if (paymentMethod.equals("upfront")) {
			setupUpfrontAccount(nodeId);
			return;
		} else if (paymentMethod.startsWith("escrow:")) {
			String escrowProvId = paymentMethod.substring(7);
			setupEscrowAccount(nodeId, escrowProvId);
			return;
		}
		log.error("Error: could not setup account with " + nodeId + " - unknown payment method '" + paymentMethod + "'");
	}

	private void setupUpfrontAccount(final String nodeId) {
		log.info("Setting up upfront account with node " + nodeId);
		ControlConnection cc = mina.getCCM().getCCWithId(nodeId);
		if (cc == null) {
			log.error("No cc for setting up account with " + nodeId);
			return;
		}
		double cashToSend = mina.getCurrencyClient().getOpeningBalance();
		byte[] token;
		try {
			token = mina.getCurrencyClient().withdrawToken(cashToSend, "Setting up account with node " + nodeId);
		} catch (CurrencyException e) {
			log.error("Error withdrawing token of value " + cashToSend + " while trying to open account with " + nodeId);
			return;
		}
		synchronized (this) {
			Account a = new Account();
			a.addRecentAs(asMap.get(nodeId));
			accounts.put(nodeId, a);
		}
		try {
			TopUp tu = TopUp.newBuilder().setCurrencyToken(ByteString.copyFrom(token)).build();
			cc.sendMessageOrThrow("TopUp", tu);
		} catch (Exception e) {
			// This failed - recover the cash
			final byte[] tok = token;
			mina.getExecutor().execute(new CatchingRunnable() {
				public void doRun() throws Exception {
					log.error("Attempting to return cash for failed openacct");
					mina.getCurrencyClient().depositToken(tok,
							"Returning cash after failing to open account with node " + nodeId);
				}
			});
		}
		synchronized (this) {
			accounts.get(nodeId).balance += cashToSend;
		}
		accountSetupSucceeded(nodeId);
	}

	private void setupEscrowAccount(final String nodeId, final String escrowProvId) {
		// TODO This attempt-based stuff lacks wang... just tell the escrow mgr to open the account, and have it call
		// our accountSetupSucceeded when it's done
		// TODO Possibly split off upfront stuff into own UpfrontAccountMgr, and have sibling EscrowAccountMgr...?
		Attempt a = new Attempt(mina.getExecutor(), mina.getConfig().getMessageTimeout(), "escrow-" + nodeId) {
			protected void onSuccess() {
				// We're now connected to the escrow provider
				double cashToSend = mina.getCurrencyClient().getOpeningBalance();
				String escrowId = mina.getEscrowMgr().startNewEscrow(cashToSend);
				EscrowBegan.Builder ebb = EscrowBegan.newBuilder();
				ebb.setAmount(cashToSend);
				ebb.setEscrowId(escrowId);
				mina.getCCM().getCCWithId(nodeId).sendMessage("EscrowBegan", ebb.build());
				synchronized (BuyMgr.this) {
					Account a = new Account();
					a.addRecentAs(asMap.get(nodeId));
					a.balance += cashToSend;
					accounts.put(nodeId, a);
				}
			}
		};
		mina.getEscrowMgr().setupEscrowAccount(escrowProvId, a);
		// TODO finish this - end up by calling accountSetupSucceeded somewhere
	}

	/**
	 * @syncpriority 170
	 */
	public void closeAccount(String nodeId) {
		boolean gotAccount;
		synchronized (this) {
			gotAccount = accounts.containsKey(nodeId);
		}
		ControlConnection cc = mina.getCCM().getCCWithId(nodeId);
		if (gotAccount) {
			if (cc != null) {
				CloseAcct ca = CloseAcct.newBuilder().build();
				cc.sendMessage("CloseAcct", ca);
				sentBid(nodeId, 0);
				return;
			}
		} else
			cc.closeGracefully();
	}

	/**
	 * @syncpriority 170
	 */
	public void accountClosed(String nodeId, byte[] currencyToken) {
		Account acct;
		synchronized (this) {
			acct = accounts.remove(nodeId);
		}
		if (acct == null)
			log.error("Received acctclosed from " + nodeId + ", but I have no registered account");
		else {
			try {
				double val = mina.getCurrencyClient().depositToken(currencyToken,
						"Balance returned from node " + nodeId);
				if ((val - acct.balance) < 0) {
					// TODO Something more serious here
					log.error("ERROR: balance mismatch when closing acct with " + nodeId + ": I say " + acct.balance
							+ ", he gave me " + val);
				}
			} catch (CurrencyException e) {
				log.error("Error when depositing token from " + nodeId, e);
			}
			log.debug("Successfully closed account with " + nodeId);
			ControlConnection cc = mina.getCCM().getCCWithId(nodeId);
			if(cc != null)
				cc.closeGracefully();
		}
	}

	private void accountSetupSucceeded(final String sellerNodeId) {
		// w00t. Now, we bid
		long timeUntilBid;
		synchronized (this) {
			accountsInProgress.remove(sellerNodeId);
			AuctionState as = asMap.get(sellerNodeId);
			if (as == null)
				throw new SeekInnerCalmException();
			timeUntilBid = as.getBidsOpen();
		}
		if (timeUntilBid <= 0)
			openBidding(sellerNodeId);
		else {
			log.debug("Waiting " + timeUntilBid + "ms to open bid to " + sellerNodeId);
			mina.getExecutor().schedule(new CatchingRunnable() {
				public void doRun() throws Exception {
					openBidding(sellerNodeId);
				}
			}, timeUntilBid, TimeUnit.MILLISECONDS);
		}
	}

	public void possiblyRebid(final String nodeId) {
		ControlConnection cc = mina.getCCM().getCCWithId(nodeId);
		double lastBid;
		long timeUntilBid;
		if (cc == null) {
			log.error("Not rebidding to " + nodeId + " - no connection");
			return;
		}
		synchronized (this) {
			if(!accounts.containsKey(nodeId)) {
				log.error("Not rebidding to "+nodeId+" - no account");
				return;
			}
			AuctionState as = asMap.get(nodeId);
			if(as == null) {
				log.debug("Not rebidding to "+nodeId+" - no auctionstate");
				return;
			}
			lastBid = as.getLastSentBid();
			timeUntilBid = as.getBidsOpen();
		}
		// Poll everyone interested in this guy, use the highest bid
		double topBid = 0;
		for (LCPair lcp : cc.getLCPairs()) {
			double thisBid = lcp.getSM().getBidStrategy().getOpeningBid(nodeId);
			if (thisBid > topBid)
				topBid = thisBid;
		}
		if(dblEq(lastBid, topBid)) {
			log.debug("Not rebidding to "+nodeId+" - happy with current bid");
			return;
		}
		if (timeUntilBid <= 0)
			openBidding(nodeId);
		else {
			log.debug("Waiting " + timeUntilBid + "ms to open bid to " + nodeId);
			mina.getExecutor().schedule(new CatchingRunnable() {
				public void doRun() throws Exception {
					openBidding(nodeId);
				}
			}, timeUntilBid, TimeUnit.MILLISECONDS);
		}
	}
	
	private void openBidding(final String nodeId) {
		ControlConnection cc = mina.getCCM().getCCWithId(nodeId);
		if (cc == null) {
			log.error("Not opening bidding to " + nodeId + " - no connection");
			return;
		}
		synchronized (this) {
			if(!accounts.containsKey(nodeId)) {
				log.error("Not opening bidding to "+nodeId+" - no account");
				return;
			}
		}
		// Poll everyone interested in this guy, use the highest bid
		double topBid = 0;
		for (LCPair lcp : cc.getLCPairs()) {
			double thisBid = lcp.getSM().getBidStrategy().getOpeningBid(nodeId);
			if (thisBid > topBid)
				topBid = thisBid;
		}
		if (topBid == 0) {
			log.error("Told to open bidding to " + nodeId + ", but now nobody wants to bid!");
			return;
		}
		Bid bidMsg = Bid.newBuilder().setAmount(topBid).build();
		cc.sendMessage("Bid", bidMsg);
		sentBid(nodeId, topBid);
	}

	/**
	 * An auction has moved on - update the bids
	 * @syncpriority 170
	 */
	public synchronized void bidUpdate(String fromNodeId, BidUpdate bu) {
		AuctionState as = asMap.get(fromNodeId);
		if (as == null) {
			log.error("Received bidupdate from " + fromNodeId + ", but I have no auctionstate");
			return;
		}
		as.setBids(getUpdatedBids(as, bu));
	}

	private List<ReceivedBid> getUpdatedBids(AuctionState as, BidUpdate bu) {
		Map<String, ReceivedBid> map = new HashMap<String, ReceivedBid>();
		for (ReceivedBid bid : as.getBids()) {
			map.put(bid.getListenerId(), bid);
		}
		for (int i = 0; i < bu.getListenerIdCount(); i++) {
			ReceivedBid.Builder bb = ReceivedBid.newBuilder();
			String lId = bu.getListenerId(i);
			double bidAmt = bu.getBidAmount(i);
			bb.setListenerId(lId);
			bb.setBid(bidAmt);
			map.put(lId, bb.build());
		}
		ArrayList<ReceivedBid> list = new ArrayList<ReceivedBid>(map.size());
		list.addAll(map.values());
		Collections.sort(list, new ReceivedBidComparator());
		return list;
	}

	/**
	 * Are we able to bid high enough in this auction to get any data?
	 */
	public boolean canListenTo(AuctionState as, StreamVelocity sv) {
		if (as.getMyBid() > 0) {
			// wat
			return true;
		}
		int maxRL = as.getMaxRunningListeners();
		if (as.getBids().size() < maxRL)
			return true;
		double myMaxBid = mina.getCurrencyClient().getMaxBid(sv);
		int numRLsAboveMe = 0;
		for (ReceivedBid b : as.getBids()) {
			if (b.getFlowRate() > 0 && b.getBid() >= myMaxBid)
				numRLsAboveMe++;
		}
		return (numRLsAboveMe < maxRL);
	}

	private void bidFailure(final String sellerNodeId, AuctionState as) {
		// We bid in this auction, but we weren't in the result
		// Figure out if we are too cheap for this guy - work out our highest streamvelocity in order to do so
		ControlConnection cc = mina.getCCM().getCCWithId(sellerNodeId);
		if (cc == null)
			return;
		// TODO Real-time streamvelocity
		StreamVelocity sv = StreamVelocity.LowestCost;
		LCPair[] lcps = cc.getLCPairs();
		for (LCPair lcp : lcps) {
			if (lcp.getSM().getStreamVelocity() == StreamVelocity.MaxRate)
				sv = StreamVelocity.MaxRate;
		}
		if (!canListenTo(as, sv)) {
			log.debug("Failed to successfully bid with " + sellerNodeId + ": they're full and/or we are cheap");
			for (LCPair lcp : lcps) {
				lcp.agoricsFailure();
			}
			return;
		}
		// We can listen to this auctionstate... just some networking wibbles maybe
		// Fire off a task to open bidding again when we're allowed
		mina.getExecutor().schedule(new CatchingRunnable() {
			public void doRun() throws Exception {
				openBidding(sellerNodeId);
			}
		}, as.getBidsOpen(), TimeUnit.MILLISECONDS);
	}

	/**
	 * @syncpriority 170
	 */
	public void newAuctionState(final String nodeId, final AuctionState as) {
		as.setTimeReceived(now());
		boolean gotConfirmedBid;
		synchronized (this) {
			// Check our quoted bid, check it conforms to the last one we sent
			AuctionState oldAs = asMap.get(nodeId);
			if (oldAs != null) {
				if (AuctionState.INDEX_MOD.gte(oldAs.getIndex(), as.getIndex())) {
					// This auctionstate isn't newer than what we have, ignore
					return;
				}
				double myLastBid = oldAs.getLastSentBid();
				if (myLastBid > 0) {
					if (as.getMyBid() == 0) {
						// Oops, I'm not in this result and I expected to be
						// Spawn a thread to get out of this sync
						mina.getExecutor().execute(new CatchingRunnable() {
							public void doRun() throws Exception {
								bidFailure(nodeId, as);
							}
						});
					} else {
						double quotedBid = as.getMyBid();
						if ((quotedBid - myLastBid) != 0) {
							log.error("ERROR: node " + nodeId + " quoted my last bid as " + quotedBid + ", but it was "
									+ myLastBid + " (tid: " + Thread.currentThread().getId() + ")");
							// TODO Something much more serious here
						}
					}
					as.setLastSentBid(oldAs.getLastSentBid());
				}
			}
			// Keep track of this AS
			if (accounts.containsKey(nodeId))
				accounts.get(nodeId).addRecentAs(as);
			gotConfirmedBid = (as.getMyBid() > 0);
			asMap.put(nodeId, as);
		}
		if (gotConfirmedBid) {
			// Let everybody know
			for (LCPair lcp : mina.getCCM().getCCWithId(nodeId).getLCPairs()) {
				lcp.gotAgreedBid();
			}
		}
		// TODO Poll our interested SMs at intervals to see if our bid should change
	}

	/**
	 * @syncpriority 170
	 */
	public void sentBid(String nodeId, double bid) {
		synchronized (this) {
			AuctionState as = asMap.get(nodeId);
			as.setLastSentBid(bid);
		}
	}

	public String getBestPaymentMethod(Agorics theirAgs) {
		String[] theirMethods = theirAgs.getAcceptPaymentMethods().split(",");
		// If they accept an escrow provider that we're already connected to, use that
		// Otherwise, if they accept an escrow provider that we also accept, use that
		// Otherwise use upfront if we support it
		for (String method : theirMethods) {
			if (method.startsWith("escrow:")) {
				String escrowProvNodeId = method.substring(7);
				if (mina.getEscrowMgr().isAcceptableEscrowProvider(escrowProvNodeId)
						&& mina.getCCM().haveRunningOrPendingCCTo(escrowProvNodeId))
					return method;
			}
		}
		for (String method : theirMethods) {
			if (method.startsWith("escrow:")) {
				String escrowProvNodeId = method.substring(7);
				if (mina.getEscrowMgr().isAcceptableEscrowProvider(escrowProvNodeId))
					return method;
			}
		}
		boolean gotUpfront = false;
		for (String method : theirMethods) {
			if (method.equals("upfront"))
				gotUpfront = true;
		}
		if (!gotUpfront)
			return null;
		for (String method : mina.getCurrencyClient().getAcceptPaymentMethods().split(",")) {
			if (method.equals("upfront"))
				return "upfront";
		}
		return null;
	}

	/**
	 * @syncpriority 170
	 */
	public void receivedPage(String fromNodeId, int statusIdx, long pageLen) {
		synchronized (this) {
			Account acct = accounts.get(fromNodeId);
			if (acct == null) {
				log.error("ERROR: received page from " + fromNodeId + " with status index " + statusIdx
						+ ", but I have no account with that node");
				// TODO Something much more serious here
				return;

			}
			AuctionState as = acct.getAs(statusIdx);
			if (as == null) {
				log.error("ERROR: received page from " + fromNodeId + " with status index " + statusIdx
						+ ", but I have no record of that status!");
				// TODO Something much more serious here
				return;
			}
			// Deduct from our account balance. All prices are per megabyte.
			double pageCost = ((double) pageLen / (1024 * 1024)) * as.getMyBid();
			acct.balance -= pageCost;
		}
		checkAcctBalance(fromNodeId);
	}

	private void checkAcctBalance(String fromNodeId) {
		// Make sure we have enough in our account for 30 secs' worth of
		// reception for all streams (or the rest of the stream if less)
		long bytesRequired = 0;
		ControlConnection cc = mina.getCCM().getCCWithId(fromNodeId);
		if (cc == null) {
			// Connection has died, but page made it through gasping and
			// wheezing before everything got killed
			return;
		}
		LCPair[] lcps = cc.getLCPairs();
		for (LCPair lcp : lcps) {
			int bytesForTime = lcp.getFlowRate() * mina.getConfig().getBalanceBufferTime();
			PageBuffer pb = lcp.getSM().getPageBuffer();
			long bytesForRestOfStream = (pb.getTotalPages() - pb.getLastContiguousPage()) * pb.getAvgPageSize();
			bytesRequired += Math.min(bytesForTime, bytesForRestOfStream);
		}
		double myBid;
		double balance;
		synchronized (this) {
			myBid = asMap.get(fromNodeId).getMyBid();
			balance = accounts.get(fromNodeId).balance;
		}
		double endsRequired = ((double) bytesRequired / (1024 * 1024)) * myBid;
		if (balance < endsRequired) {
			byte[] token;
			try {
				token = mina.getCurrencyClient().withdrawToken(endsRequired,
						"Topping up account with node " + fromNodeId);
			} catch (CurrencyException e) {
				log.error("Error withdrawing token of value " + endsRequired + " trying to top up account with "
						+ fromNodeId);
				return;
			}
			synchronized (this) {
				accounts.get(fromNodeId).balance += endsRequired;
			}
			TopUp tu = TopUp.newBuilder().setCurrencyToken(ByteString.copyFrom(token)).build();
			cc.sendMessage("TopUp", tu);
		}
	}

	/**
	 * @syncpriority 170
	 */
	public void gotPayUpDemand(String fromNodeId, double toldBalance) {
		gotPayUpDemand(fromNodeId, toldBalance, false);
	}

	private void gotPayUpDemand(final String fromNodeId, final double toldBalance, boolean tryingAgain) {
		synchronized (this) {
			// TODO For now we're just accepting all payment demands, even if we
			// don't agree with them - obviously need to change this once we've
			// figured out why the mismatches occur
			double myBalance = accounts.get(fromNodeId).balance;
			if ((toldBalance - myBalance) != 0) {
				log.error("ERROR: got mismatch in payment demand from " + fromNodeId + ": they say " + toldBalance
						+ ", I say " + myBalance);
				// if (!tryingAgain) {
				// int catchupSecs = mina.getConfig().getPayUpCatchUpTime();
				// log.info("Mismatch in payment demand from " + fromNodeId +
				// ": waiting " + catchupSecs + "s to catch up");
				// // This might be down to some pages still being in flight -
				// // back off to let them arrive, then calculate again
				// mina.getExecutor().schedule(new CatchingRunnable() {
				// public void doRun() throws Exception {
				// gotPayUpDemand(fromNodeId, toldBalance, true);
				// }
				// }, catchupSecs, TimeUnit.SECONDS);
				// } else {
				// // We've waited for things to catch up, and there's still a
				// // discrepancy...
				// // TODO What should we do here...?
				// log.error("ERROR: got mismatch in payment demand from " +
				// fromNodeId + ": they say " + toldBalance + ", I say " +
				// myBalance);
				// return;
				// }
			}
			// TODO This will just pay everyone whatever they demand...
			accounts.get(fromNodeId).balance = toldBalance;
		}
		checkAcctBalance(fromNodeId);
	}

	/**
	 * @syncpriority 170
	 */
	public synchronized void notifyDeadConnection(String nodeId) {
		// TODO: We should try and keep account info so that it's still there when we reconnect - but then we
		// need a way of synchronizing account state when they reconnect - look at this when we're implementing escrow
		log.debug("BuyMgr cleaning up after " + nodeId);
		asMap.remove(nodeId);
		agMap.remove(nodeId);
		accounts.remove(nodeId);
		accountsInProgress.remove(nodeId);
	}

	/**
	 * Notifies that we have had a minimum charge applied to us, as we were the top bidder and weren't receiving data
	 * fast enough
	 * @syncpriority 170
	 */
	public synchronized void minCharge(String nodeId, double charge) {
		// You trying to jack me, vato?
		Account acct = accounts.get(nodeId);
		AuctionState as = acct.getMostRecentAs();
		if (!as.amITopBid()) {
			log.error("Node " + nodeId + " applied mincharge to me, but I am not top bidder!");
			// TODO Something much more serious here
			return;
		}
		long msSinceAuction = msElapsedSince(as.timeReceived);
		long minBytes = (long) (agMap.get(nodeId).getMinTopRate() * (msSinceAuction / 1000f));
		long shortfallBytes = minBytes - acct.bytesRecvdOnMostRecentAs;
		double calcCharge = (shortfallBytes / (1024d * 1024d)) * as.getMyBid();
		if (calcCharge < charge) {
			// TODO Wiggle room? What should we do here?
			log.error("Node " + nodeId + " demanded unfair mincharge; he says " + charge + ", I say " + calcCharge);
			return;
		}
		acct.balance -= charge;
	}

	class Account {
		double balance = 0;
		long bytesRecvdOnMostRecentAs = 0;
		/** Keep track of the last few auction states we've received */
		private Map<Integer, AuctionState> recentAs = new HashMap<Integer, AuctionState>();
		private LinkedList<Integer> recentAsIdxs = new LinkedList<Integer>();

		void addRecentAs(AuctionState as) {
			if (recentAs.size() >= AUCTION_STATE_HISTORY) {
				int oldestAsIdx = recentAsIdxs.remove();
				recentAs.remove(oldestAsIdx);
			}
			recentAs.put(as.getIndex(), as);
			recentAsIdxs.add(as.getIndex());
		}

		AuctionState getAs(int asIdx) {
			return recentAs.get(asIdx);
		}

		AuctionState getMostRecentAs() {
			if (recentAsIdxs.size() == 0)
				return null;
			return recentAs.get((int) recentAsIdxs.getLast());
		}
	}
}
