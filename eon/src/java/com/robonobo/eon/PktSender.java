package com.robonobo.eon;

import static java.lang.System.*;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.robonobo.common.concurrent.CatchingRunnable;
import com.robonobo.common.exceptions.SeekInnerCalmException;

public class PktSender extends CatchingRunnable {
	/** millisecs */
	private static final long WAIT_TIME = 100;
	/** millisecs - the max number of ms data credit we can have */
	private static final int MAX_CREDIT_TIME = 1000;
	Log log = LogFactory.getLog(getClass());
	private DatagramChannel chan;
	ByteBuffer sendBuf = ByteBuffer.allocate(EONManager.MAX_PKT_SIZE);
	private int maxBps = -1;
	/** This represents the number of bytes we can send now */
	private int bytesCredit = Integer.MAX_VALUE;
	private int maxBytesCredit;
	/** The last time we calculated how much credit we have */
	private long lastCreditTime;
	/** If we are out of credit, the next time we can send */
	private long nextSendTime = 0;
	private boolean stopping = false;
	private Thread t;
	private ArrayList<EONPacket> unthrottledPkts = new ArrayList<EONPacket>();
	private LinkedList<EONConnection> readyConns = new LinkedList<EONConnection>();
	private ReentrantLock lock;
	private Condition canSend;
	private Condition noUnthrottledPkts;
	private Visitor vis = new Visitor();
	private final boolean debugLogging;

	PktSender(DatagramChannel chan) {
		// Those isDebugEnabled() calls add up, this class is inner-loopish
		debugLogging = log.isDebugEnabled();
		this.chan = chan;
		lock = new ReentrantLock();
		canSend = lock.newCondition();
		noUnthrottledPkts = lock.newCondition();
	}

	void start() {
		t = new Thread(this, "EON-Send");
		t.start();
	}

	void stop() {
		stopping = true;
		// Wait for all immediate pkts to be sent
		lock.lock();
		try {
			while (unthrottledPkts.size() > 0) {
				noUnthrottledPkts.await();
			}
		} catch (InterruptedException e) {
			// Just exit
		} finally {
			lock.unlock();
		}
		if (t != null)
			t.interrupt();
	}

	final void sendPktImmediate(EONPacket pkt) {
		lock.lock();
		try {
			unthrottledPkts.add(pkt);
			canSend.signal();
		} finally {
			lock.unlock();
		}
	}

	final void haveDataToSend(EONConnection conn) {
		lock.lock();
		try {
			insertReadyConn(conn);
			// If we're not waiting to send due to our credit limit, signal the thread
			if (currentTimeMillis() >= nextSendTime)
				canSend.signal();
		} finally {
			lock.unlock();
		}
	}

	private void insertReadyConn(EONConnection conn) {
		// DEBUG
		for (EONConnection testConn : readyConns) {
			if (testConn == conn)
				throw new SeekInnerCalmException();
		}

		// TODO Annoying that one can't 'reset' an iterator, which means we're doing this object creation every time
		// Need to profile a node with many connections and see if it's worth writing a custom class
		ListIterator<EONConnection> it = readyConns.listIterator();
		// Start at the beginning of the list and go up until we find a conn with a gamma >= to this one, then
		// insert it before (so that first-notifying conns get to send pkts first)
		while (it.hasNext()) {
			EONConnection testConn = it.next();
			if (testConn.getGamma() >= conn.getGamma()) {
				// Rewind so we insert before this one
				it.previous();
				break;
			}
		}
		it.add(conn);

		// DEBUG
		log.debug("pktSender added ready conn "+conn+", now have: " + readyConns);
	}

	@Override
	/**
	 * Inner loop
	 */
	public void doRun() throws Exception {
		while (true) {
			long nowTime = currentTimeMillis();
			lock.lock();
			try {
				// If we're being throttled, wait until the specified time, otherwise wait until signalled
				if (unthrottledPkts.size() == 0 && nextSendTime > nowTime) {
					if (debugLogging)
						log.debug("Pausing pktSend: throttling - " + readyConns.size() + " ready conns");
					noUnthrottledPkts.signal();
					canSend.await((nextSendTime - nowTime), TimeUnit.MILLISECONDS);
				}
				while (unthrottledPkts.size() == 0 && readyConns.size() == 0) {
					if (debugLogging)
						log.debug("Pausing pktSend: no more data");
					noUnthrottledPkts.signal();
					canSend.await();
				}
				nowTime = currentTimeMillis();
				if (maxBps >= 0) {
					long elapsedMs = nowTime - lastCreditTime;
					float creditPerMs = (float)maxBps / 1000;
					bytesCredit += elapsedMs * creditPerMs;
					if (bytesCredit > maxBytesCredit)
						bytesCredit = maxBytesCredit;
				}
				lastCreditTime = nowTime;
				if (debugLogging)
					log.debug("pktSend running, send credit " + bytesCredit + "B");
				// Any unthrottled pkts we have, send them now
				for (EONPacket pkt : unthrottledPkts) {
					if (debugLogging)
						log.debug("Sending unthrottled pkt: " + pkt);
					sendPkt(pkt);
				}
				unthrottledPkts.clear();
				if (stopping) {
					noUnthrottledPkts.signal();
					return;
				}
			} catch (InterruptedException e) {
				if (stopping)
					return;
				else
					log.error("pktSender received unexpected InterruptedException - continuing");
			} finally {
				lock.unlock();
			}
			// Come out of the lock before we call conn.acceptVisitor() to remove deadlock possibilities
			// Send data from our ready conns until we're out of credit
			while (readyConns.size() > 0) {
				EONConnection conn;
				lock.lock();
				try {
					conn = readyConns.removeLast();
				} finally {
					lock.unlock();
				}
				try {
					if (conn.acceptVisitor(vis)) {
						// We're out of credit, and this guy still has data to send
						// Re-insert this guy behind any other waiting conns with the same gamma, otherwise one guy with
						// a 1MB send holds everyone up
						lock.lock();
						try {
							insertReadyConn(conn);
							// Wait to allow our credit to accumulate
							nextSendTime = nowTime + WAIT_TIME;
						} finally {
							lock.unlock();
						}
						break;
					}
				} catch (Exception e) {
					log.error("pktSender caught exception when visiting " + conn, e);
				}
			}
		}
	}

	private void sendPkt(EONPacket pkt) throws EONException {
		sendBuf.clear();
		pkt.toByteBuffer(sendBuf);
		sendBuf.flip();
		try {
			chan.send(sendBuf, pkt.getDestSocketAddress().getInetSocketAddress());
		} catch (IOException e) {
			throw new EONException(e);
		}
		if (debugLogging)
			log.debug("s " + pkt.toString());
	}

	/**
	 * Pass maxBps < 0 to indicate no limit
	 */
	public void setMaxBps(int maxBps) {
		lock.lock();
		try {
			if (debugLogging)
				log.debug("PktSender setting max bps to " + maxBps);
			this.maxBps = maxBps;
			if (maxBps >= 0) {
				bytesCredit = (int) (maxBps * (float) (MAX_CREDIT_TIME / 1000));
				lastCreditTime = currentTimeMillis();
				maxBytesCredit = (int) (MAX_CREDIT_TIME * ((float)maxBps / 1000));
			} else
				bytesCredit = Integer.MAX_VALUE;
		} finally {
			lock.unlock();
		}
	}

	public int getMaxBps() {
		return maxBps;
	}

	class Visitor implements PktSendVisitor {
		@Override
		public int bytesAvailable() {
			return bytesCredit;
		}

		@Override
		public void sendPkt(EONPacket pkt) throws EONException {
			int payloadSz = pkt.getPayloadSize();
			if (bytesCredit < payloadSz)
				throw new SeekInnerCalmException();
			PktSender.this.sendPkt(pkt);
			if (bytesCredit != Integer.MAX_VALUE)
				bytesCredit -= payloadSz;
		}

	}
}
