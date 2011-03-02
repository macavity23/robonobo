package com.robonobo.eon;

/*
 * Eye-Of-Needle
 * Copyright (C) 2008 Will Morton (macavity@well.com) & Ray Hilton (ray@wirestorm.net)

 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */

import java.io.IOException;
import java.net.ConnectException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.PortUnreachableException;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.robonobo.common.concurrent.StartStopable;
import com.robonobo.common.util.Modulo;

public class EONManager implements StartStopable {
	private static final int SOCKET_BUFFER_SIZE = 64 * 1024;
	public static final int MAX_PKT_SIZE = 8192;
	Log log;
	String instanceName;
	InetSocketAddress localEP;
	DatagramChannel chan;
	boolean sockOK;
	ReceiverThread recvThread;
	PktSender pktSender;
	ConnectionHolder conns;
	ScheduledThreadPoolExecutor executor;
	Modulo mod;
	Date uploadCheckLastBase = new Date();
	int bytesUploadedSinceBase = 0;
	final boolean debugLogging;

	public static InetAddress getWildcardAddress() {
		try {
			return Inet4Address.getByName("0.0.0.0");
		} catch (UnknownHostException e) {
			// Can't happen
			throw new RuntimeException(e);
		}
	}

	public EONManager(String instanceName, ScheduledThreadPoolExecutor executor, int port) throws EONException {
		this(instanceName, executor, new InetSocketAddress(port));
	}

	public EONManager(String instanceName, ScheduledThreadPoolExecutor executor) throws EONException {
		this(instanceName, executor, null);
	}

	public EONManager(String instanceName, ScheduledThreadPoolExecutor executor, InetSocketAddress localEP)
			throws EONException {
		this.instanceName = instanceName;
		log = getLogger(getClass());
		debugLogging = log.isDebugEnabled();
		this.executor = executor;
		try {
			chan = DatagramChannel.open();
			chan.socket().setReceiveBufferSize(SOCKET_BUFFER_SIZE);
			chan.socket().setSendBufferSize(SOCKET_BUFFER_SIZE);
			chan.socket().bind(localEP);
			// Re-grab the value to see what we ended up on
			this.localEP = (InetSocketAddress) chan.socket().getLocalSocketAddress();
		} catch (IOException e) {
			throw new EONException("Unable to construct udp socket on " + localEP.toString(), e);
		}
		sockOK = true;
		pktSender = new PktSender(chan);
		recvThread = null;
		conns = new ConnectionHolder(this);
		mod = new Modulo((long) Integer.MAX_VALUE + 1);
		log.debug("EONManager created on endpoint " + this.localEP.getAddress().getHostAddress()+":"+this.localEP.getPort());
	}

	public boolean isRunning() {
		return sockOK;
	}

	public void start() throws EONException {
		recvThread = new ReceiverThread();
		recvThread.start();
		pktSender.start();
	}

	public void stop() {
		log.debug("Stopping EONManager");
		conns.closeAllConns(30000);
		pktSender.stop();
		recvThread.terminate();
		try {
			chan.close();
		} catch (Exception ignore) {
		}
		sockOK = false;
	}

	public Log getLogger(Class callerClass) {
		return LogFactory.getLog(callerClass.getName() + ".INSTANCE." + instanceName);
	}

	public DEONConnection createDEONConnection() {
		return new DEONConnection(this);
	}

	public SEONConnection createSEONConnection() throws EONException {
		return new SEONConnection(this);
	}

	public void sendNATSeed(InetSocketAddress remoteEP) throws EONException {
		DEONPacket seed = new DEONPacket(new EonSocketAddress(localEP, 0), new EonSocketAddress(remoteEP, 0), null);
		sendPktImmediate(seed);
	}

	public ScheduledThreadPoolExecutor getExecutor() {
		return executor;
	}

	public boolean requestPort(int localEONPort, EONConnection thisConn) throws EONException, IllegalArgumentException {
		return requestPort(localEONPort, new EonSocketAddress(getWildcardAddress(), 1, 1), thisConn);
	}

	public boolean requestPort(int localEONPort, EonSocketAddress addressMask, EONConnection thisConn)
			throws EONException {
		if (!sockOK)
			throw new EONException("No socket available");
		return conns.requestPort(localEONPort, addressMask, thisConn);
	}

	public int getPort(EONConnection thisConn) throws IllegalArgumentException, EONException {
		return getPort(new EonSocketAddress(getWildcardAddress(), 1, 1), thisConn);
	}

	public int getPort(EonSocketAddress addressMask, EONConnection thisConn)
			throws IllegalArgumentException, EONException {
		if (!sockOK)
			throw new EONException("No socket available");
		return conns.getPort(addressMask, thisConn);
	}

	public void returnPort(int localEONPort, EONConnection thisConn) throws EONException, IllegalArgumentException {
		returnPort(localEONPort, new EonSocketAddress(getWildcardAddress(), 1, 1), thisConn);
	}

	public void returnPort(int localEONPort, EonSocketAddress addressMask, EONConnection thisConn)
			throws EONException, IllegalArgumentException {
		conns.returnPort(localEONPort, addressMask, thisConn);
	}

	final void sendPktImmediate(EONPacket pkt) {
		pktSender.sendPktImmediate(pkt);
	}

	final void haveDataToSend(EONConnection conn) {
		pktSender.haveDataToSend(conn);
	}
	
	public int getLowestMaxObservedRtt(SEONConnection exceptConn) {
		return conns.getLowestMaxObservedRtt(exceptConn);
	}

	// Note: If we have allowed the system to choose our IP address and/or
	// port, this will
	// not be set until Start() is called
	public InetSocketAddress getLocalSocketAddress() throws EONException {
		if (sockOK)
			return localEP;
		else
			throw new EONException("Underlying socket not ready");
	}

	/**
	 * Pass -1 to specify no limit
	 */
	public void setMaxOutboundBps(int maxBps) {
		pktSender.setMaxBps(maxBps);
	}

	final int getMaxOutboundBps() {
		return pktSender.getMaxBps();
	}
	
	private class ReceiverThread extends Thread {
		boolean terminated = false;

		public ReceiverThread() {
			super();
			setName("EON-Recv");
		}

		public void run() {
			EONPacket thisPacket;
			log.debug("EONManagerThread running");
			// Loop, picking up packets and passing them off to connections as
			// necessary
			while (true) {
				// Awooga! Awooga! Inner loop alert!
				InetSocketAddress remoteEP = null;
				try {
					// Allocate a new buffer every time, as it means we don't
					// have to copy it when handling gets passed to another
					// thread, and it's cheaper to keep allocating same-sized
					// buffers
					ByteBuffer recvBuf = ByteBuffer.allocate(MAX_PKT_SIZE);
					// Pick up next packet (will block here if no packets)
					remoteEP = (InetSocketAddress) chan.receive(recvBuf);
					recvBuf.flip();
					thisPacket = EONPacket.parse(recvBuf);
					if (thisPacket == null) {
						// Duff packet. Discard and move on.
						log.error("Error parsing packet from " + remoteEP.getAddress().toString() + ":"
								+ remoteEP.getPort());
						continue;
					}
					// Set the IP data on the packet
					thisPacket.getSourceSocketAddress().setAddress(remoteEP.getAddress());
					thisPacket.getSourceSocketAddress().setUdpPort(remoteEP.getPort());
					InetSocketAddress locSockAddr = getLocalSocketAddress();
					thisPacket.getDestSocketAddress().setAddress(locSockAddr.getAddress());
					thisPacket.getDestSocketAddress().setUdpPort(locSockAddr.getPort());
					if (debugLogging) {
						String logStr = "r " + thisPacket.toString();
						log.debug(logStr);
					}
					handlePacket(thisPacket);
				} catch (EONException e) {
					if (terminated)
						return;
					log.error("Caught EONException in Eon Mgr thread", e);
					continue;
				} catch (ConnectException e) {
					// We're exiting
					return;
				} catch (PortUnreachableException e) {
					conns.killAllConnsAssociatedWith(remoteEP);
					continue;
				} catch (SocketException e) {
					if (!terminated)
						log.error("Caught SocketException when waiting for packets", e);
					return;
				} catch (IOException e) {
					if (!terminated)
						log.error("IOException when attempting to receive UDP Packet", e);
					return;
				} catch (Exception e) {
					log.error("EONMgr caught " + e.getClass().getName(), e);
					continue;
				}
			}
		}

		public void terminate() {
			// set the terminated flag
			terminated = true;

			// close socket, forces receive() to stop blocking
			try {
				chan.close();
			} catch (IOException ignore) {
			}

			// interrupt the thread
			interrupt();
		}

		public void handlePacket(EONPacket thisPacket) throws EONException {
			// If this is sent to EON port 0, it is a NATseed - discard
			if (thisPacket.getDestSocketAddress().getEonPort() == 0)
				return;
			EONConnection thisConn = conns.getLocalConnForIncoming(thisPacket.getDestSocketAddress(), thisPacket.getSourceSocketAddress());
			if(thisConn != null)
				thisConn.receivePacket(thisPacket);
			else
				handleUnwantedPacket(thisPacket);
		}

		private void handleUnwantedPacket(EONPacket thisPacket) {
			// No connection to take this packet
			if (thisPacket.getProtocol() == EONPacket.EON_PROTOCOL_SEON) {
				SEONPacket sPkt = (SEONPacket) thisPacket;
				sendRstPacketForBadPkt(sPkt);
			}
			// Just drop the packet and move on
			log.warn("EON Packet dropped: " + thisPacket);
		}

		private void sendRstPacketForBadPkt(SEONPacket badPkt) {
			if(badPkt.isRST())
				return;
			SEONPacket rstPacket = new SEONPacket(badPkt.getDestSocketAddress(), badPkt.getSourceSocketAddress(), null);
			rstPacket.setRST(true);
			if (badPkt.isACK())
				rstPacket.setSequenceNumber(badPkt.getAckNumber());
			else {
				try {
					rstPacket.setSequenceNumber(0);
					rstPacket.setAckNumber(mod.add(badPkt.getSequenceNumber(), (long) ((badPkt.getPayload() == null) ? 0
							: badPkt.getPayload().remaining())));
					rstPacket.setACK(true);
				} catch (IllegalArgumentException e) {
					log.error("Unable to perform modulo operation", e);
				}
			}
			sendPktImmediate(rstPacket);
		}

	}
}
