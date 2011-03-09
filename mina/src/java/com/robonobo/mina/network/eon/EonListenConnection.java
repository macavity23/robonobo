package com.robonobo.mina.network.eon;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;

import org.apache.commons.logging.Log;

import com.robonobo.common.async.PushDataReceiver;
import com.robonobo.common.pageio.buffer.PageSerializer;
import com.robonobo.core.api.proto.CoreApi.EndPoint;
import com.robonobo.eon.*;
import com.robonobo.mina.external.buffer.Page;
import com.robonobo.mina.external.node.EonEndPoint;
import com.robonobo.mina.external.node.SeonEndPoint;
import com.robonobo.mina.instance.MinaInstance;
import com.robonobo.mina.network.LCPair;
import com.robonobo.mina.network.ListenConnection;

public class EonListenConnection implements ListenConnection, PushDataReceiver {
	private LCPair lcPair;
	private MinaInstance mina;
	private Log log;
	private EONManager eonMgr;
	private SEONConnection listenConn;
	private SEONConnection dataConn;
	private EonEndPoint myListenEp;
	private PageSerializer serial = new PageSerializer();
	private ByteBuffer readBuf;
	boolean closed = false;

	public EonListenConnection(MinaInstance mina, EONManager eonMgr) throws EONException {
		this.mina = mina;
		this.eonMgr = eonMgr;
		log = mina.getLogger(getClass());
		listenConn = eonMgr.createSEONConnection();
		listenConn.addListener(new ConnectionListener());
		listenConn.bind();
		EonSocketAddress mySockAddr = listenConn.getLocalSocketAddress();
		try {
			// The host and udp port will be ignored here (they're the same as
			// our CC endpoint)
			myListenEp = new SeonEndPoint(InetAddress.getByName("0.0.0.0"), 0, mySockAddr.getEonPort());
		} catch (UnknownHostException e) {
			throw new EONException(e);
		}
		readBuf = ByteBuffer.allocate(mina.getConfig().getPageReadBufferSize());
	}

	public void providerClosed() {
		if (!closed) {
			// Something bad happened at the network layer - we're closing
			log.debug("Network error in LC " + lcPair.getCC().getNodeId() + "/" + lcPair.getSM().getStreamId());
			lcPair.die(false);
		}
	}

	public void close() {
		closed = true;
		listenConn.close();
		if (dataConn != null)
			dataConn.close();
	}

	public EndPoint getEndPoint() {
		return myListenEp.toMsg();
	}

	public void setLCPair(LCPair lcPair) {
		this.lcPair = lcPair;
	}

	public int getFlowRate() {
		if (dataConn == null)
			return 0;
		return dataConn.getInFlowRate();
	}

	public void receiveData(ByteBuffer data, Object ignore) throws IOException {
		// TODO Get rid of this arraycopy with a bytebufferinputstream - need to
		// make it support mark & reset first...
		try {
			readBuf.put(data);
		} catch (BufferOverflowException e) {
			log.error(this + " ERROR - overflowexception reading data");
			close();
			return;
		}
		readBuf.flip();
		while (serial.containsCompletePage(readBuf)) {
			Page p = serial.deserializePage(readBuf);
			lcPair.receivePage(p);
		}
		int bytesForNextTime = readBuf.remaining();
		// Shuffle the data down to the beginning of the buffer
		if (readBuf.position() != 0)
			System.arraycopy(readBuf.array(), readBuf.position(), readBuf.array(), 0, bytesForNextTime);
		readBuf.position(bytesForNextTime);
		readBuf.limit(readBuf.capacity());
	}

	private class ConnectionListener implements SEONConnectionListener {
		private boolean hadIncoming = false;

		public void onNewSEONConnection(EONConnectionEvent event) {
			// Should only ever get one connection request
			if (hadIncoming) {
				log.error("Error: listenconnection on " + listenConn.getLocalSocketAddress() + " received duplicate inbound connection");
				return;
			}
			hadIncoming = true;
			dataConn = (SEONConnection) event.getConnection();
			dataConn.setDataReceiver(EonListenConnection.this);
		}

		public void onClose(EONConnectionEvent event) {
			providerClosed();
		}
	}
}
