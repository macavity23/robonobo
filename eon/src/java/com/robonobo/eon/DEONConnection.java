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

import java.nio.ByteBuffer;
import java.util.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.robonobo.common.async.PushDataProvider;
import com.robonobo.common.async.PushDataReceiver;
import com.robonobo.common.concurrent.CatchingRunnable;
import com.robonobo.common.exceptions.SeekInnerCalmException;

public class DEONConnection extends EONConnection implements PushDataProvider {
	public static final int DEONConnectionState_Open = 1;
	public static final int DEONConnectionState_Closed = 2;
	private static final Log log = LogFactory.getLog(DEONConnection.class);
	EonSocketAddress localEP, remoteEP;
	private List<ByteBuffer> incomingDataBufs = new ArrayList<ByteBuffer>();
	private List<EonSocketAddress> incomingDataAddrs = new ArrayList<EonSocketAddress>();
	private LinkedList<DEONPacket> outgoingPkts = new LinkedList<DEONPacket>();
	int state = DEONConnectionState_Closed;
	PushDataReceiver dataReceiver;
	ReentrantLock receiveLock = new ReentrantLock();
	Condition canReceive;
	ReentrantLock sendLock = new ReentrantLock();
	boolean dataReceiverRunning = false;
	boolean waitingForVisitor = false;

	// Don't allow this class to be instantiated directly - use
	// EONManager.GetDEONConnection()
	protected DEONConnection(EONManager mgr) {
		super(mgr);
		canReceive = receiveLock.newCondition();
	}

	public void bind() throws EONException, IllegalArgumentException {
		connect(-1, null);
	}

	public void bind(int localEONPort) throws EONException, IllegalArgumentException {
		connect(localEONPort, null);
	}

	public void connect(EonSocketAddress remoteEndPoint) throws EONException, IllegalArgumentException {
		connect(-1, remoteEndPoint);
	}

	public void connect(int localEONPort, EonSocketAddress remoteEndPoint) throws EONException,
			IllegalArgumentException {
		if (state == DEONConnectionState_Open) {
			throw new EONException("Connection is already open");
		}
		if (localEONPort != -1 && (localEONPort < 1 || localEONPort > 65535)) {
			throw new IllegalArgumentException("Port must be between 1 and 65535");
		}
		localEP = new EonSocketAddress(mgr.getLocalSocketAddress(), 1);
		// First, request our port
		if (localEONPort == -1) {
			// Unspecified port
			localEP.setEonPort(mgr.getPort(this));
			if (localEP.getEonPort() == -1) {
				throw new IllegalArgumentException("No EON ports available");
			}
		} else {
			localEP.setEonPort(localEONPort);
			mgr.requestPort(localEP.getEonPort(), this);
		}
		remoteEP = remoteEndPoint;
		state = DEONConnectionState_Open;
	}

	public void send(byte[] buffer) throws EONException {
		send(buffer, 0, buffer.length);
	}

	public void send(byte[] buffer, int startIndex, int numBytes) throws EONException {
		if (remoteEP == null) {
			throw new EONException("Remote EON Endpoint has not been specified");
		}
		sendTo(remoteEP, buffer, startIndex, numBytes);
	}

	public void sendTo(EonSocketAddress remoteEndPoint, byte[] buffer) throws EONException {
		sendTo(remoteEndPoint, buffer, 0, buffer.length);
	}

	public void sendTo(EonSocketAddress remoteEndPoint, byte[] buffer, int startIndex, int numBytes)
			throws EONException {
		if (startIndex + numBytes > buffer.length)
			throw new SeekInnerCalmException("Supplied indices do not fit in supplied buffer");
		if (state == DEONConnectionState_Closed)
			throw new EONException("Connection is closed");
		byte[] payloadArr = new byte[numBytes];
		System.arraycopy(buffer, startIndex, payloadArr, 0, numBytes);
		ByteBuffer payload = ByteBuffer.wrap(payloadArr);
		DEONPacket thisPacket = new DEONPacket(null, remoteEndPoint, payload);
		thisPacket.setSourceSocketAddress(localEP);
		sendLock.lock();
		try {
			outgoingPkts.add(thisPacket);
			haveDataToSend();
		} finally {
			sendLock.unlock();
		}
		// TODO - some way of specifying that some deonconnections ignore our throttling?
	}

	protected void haveDataToSend() {
		if (waitingForVisitor)
			return;
		waitingForVisitor = true;
		mgr.haveDataToSend(this);
	}

	@Override
	boolean acceptVisitor(PktSendVisitor vis) throws EONException {
		sendLock.lock();
		try {
			waitingForVisitor = false;
			while (outgoingPkts.size() > 0) {
				if (vis.bytesAvailable() < outgoingPkts.getFirst().getPayloadSize()) {
					waitingForVisitor = true;
					return true;
				}
				vis.sendPkt(outgoingPkts.removeFirst());
			}
			return false;
		} finally {
			sendLock.unlock();
		}
	}

	public void close() {
		if (state == DEONConnectionState_Closed)
			log.warn("Connection is closed");
		try {
			mgr.returnPort(localEP.getEonPort(), this);
		} catch (Exception e) {
			// dont care, we are closing, but log anyway
			log.warn("Exception caught on close()", e);
		}
		state = DEONConnectionState_Closed;
		fireOnClose();
		receiveLock.lock(); 
		try {
			canReceive.signalAll();
		} finally {
			receiveLock.unlock();
		}
	}

	public void abort() {
		close();
	}

	public EonSocketAddress getLocalSocketAddress() {
		return localEP;
	}

	// This might be null
	public EonSocketAddress getRemoteSocketAddress() {
		return remoteEP;
	}

	public int getState() {
		return state;
	}

	@Override
	public float getGamma() {
		// DEON conns always have gamma 1
		return 1f;
	}

	/**
	 * @return 2 element array - bytebuffer at 0, eonsocketaddress at 1
	 */
	public Object[] read() throws EONException, InterruptedException {
		receiveLock.lock();
		try {
			while (incomingDataBufs.size() == 0 && state == DEONConnectionState_Open) {
				canReceive.await();
			}
			if (state == DEONConnectionState_Closed)
				throw new InterruptedException();
			ByteBuffer buf = (ByteBuffer) incomingDataBufs.get(0);
			incomingDataBufs.remove(0);
			EonSocketAddress addr = (EonSocketAddress) incomingDataAddrs.get(0);
			Object[] result = new Object[2];
			result[0] = buf;
			result[1] = addr;
			return result;			
		} finally {
			receiveLock.unlock();
		}
	}

	void receivePacket(EONPacket eonPacket) {
		DEONPacket packet = (DEONPacket) eonPacket;
		ByteBuffer buf = ByteBuffer.allocate(packet.getPayload().limit());
		buf.put(packet.getPayload());
		buf.flip();
		EonSocketAddress addr = packet.getSourceSocketAddress();
		receiveLock.lock();
		try {
			incomingDataBufs.add(buf);
			incomingDataAddrs.add(addr);
			if (dataReceiver == null)
				canReceive.signal();
			else {
				// Async read
				if (dataReceiverRunning) // This will be picked up by the already-running receiver
					return;
				else
					fireAsyncReceiver();
			}
		} finally {
			receiveLock.unlock();
		}
	}

	/** Must only be called when holding receiveLock*/
	private void fireAsyncReceiver() {
		dataReceiverRunning = true;
		// Fire off our async receiver
		mgr.getExecutor().execute(new CatchingRunnable() {
			public void doRun() throws Exception {
				while (true) {
					// Grab a copy of our datareceiver in case it's set to null
					PushDataReceiver dataRec = null;
					ByteBuffer buf = null;
					EonSocketAddress sockAddr = null;
					receiveLock.lock();
					try {
						dataRec = dataReceiver;
						if (dataRec == null || incomingDataBufs.size() == 0) {
							dataReceiverRunning = false;
							return;
						}
						buf = incomingDataBufs.remove(0);
						sockAddr = incomingDataAddrs.remove(0);
					} finally {
						receiveLock.unlock();
					}
					dataRec.receiveData(buf, sockAddr);
				}
			}
		});
	}

	public void setDataReceiver(PushDataReceiver dataReceiver) {
		receiveLock.lock();
		try {
			this.dataReceiver = dataReceiver;
			// If we've got incoming data blocks queued up, handle them now
			if (dataReceiver != null && incomingDataBufs.size() > 0 && !dataReceiverRunning)
				fireAsyncReceiver();
		} finally {
			receiveLock.unlock();
		}
	}
}