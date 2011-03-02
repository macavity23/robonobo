package com.robonobo.mina.network.eon;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.apache.commons.logging.Log;

import com.robonobo.common.async.PullDataProvider;
import com.robonobo.common.pageio.buffer.PageSerializer;
import com.robonobo.eon.EONConnectionEvent;
import com.robonobo.eon.EONConnectionListener;
import com.robonobo.eon.EONException;
import com.robonobo.eon.EONManager;
import com.robonobo.eon.EonSocketAddress;
import com.robonobo.eon.SEONConnection;
import com.robonobo.mina.external.buffer.Page;
import com.robonobo.mina.external.node.EonEndPoint;
import com.robonobo.mina.instance.MinaInstance;
import com.robonobo.mina.message.proto.MinaProtocol.StopSource;
import com.robonobo.mina.network.BCPair;
import com.robonobo.mina.network.BroadcastConnection;

public class EonBroadcastConnection implements BroadcastConnection, PullDataProvider, EONConnectionListener {
	private MinaInstance mina;
	private Log log;
	private EONManager eonMgr;
	private BCPair bcp;
	private EonEndPoint theirEp;
	private SEONConnection seonConn;
	private PageSerializer serial;
	private LinkedList<Long> pageQ = new LinkedList<Long>();
	private LinkedList<Integer> auctionStatusQ = new LinkedList<Integer>();
	private boolean closed = false;

	public EonBroadcastConnection(MinaInstance mina, EONManager eonMgr, EonEndPoint theirEp) throws EONException {
		this.mina = mina;
		this.eonMgr = eonMgr;
		this.theirEp = theirEp;
		log = mina.getLogger(getClass());
		serial = new PageSerializer();

		EonSocketAddress theirSockAddr = new EonSocketAddress(theirEp.getAddress(), theirEp.getUdpPort(), theirEp.getEonPort());
		seonConn = eonMgr.createSEONConnection();
		seonConn.setNoDelay(true);
		seonConn.connect(theirSockAddr);
		seonConn.addListener(this);
	}

	public void close() {
		if (closed)
			return;
		closed = true;
		// Set the gamma to 0 here; this tells the connection to close without waiting for data to be sent
		seonConn.setGamma(0f);
		seonConn.close();
	}

	/** Called when seon conn is closed */
	public void onClose(EONConnectionEvent event) {
		if (!closed) {
			log.debug("Network error for BC[" + bcp.getCC().getNodeId() + "/" + bcp.getSM().getStreamId());
			bcp.die(false);
		}
	}

	@Override
	public String toString() {
		return "BC[" + bcp.getCC().getNodeId() + "/" + bcp.getSM().getStreamId() + "]";
	}

	public void addPageToQ(long pageNum, int auctionStatus) {
		log.debug("Adding queued page " + pageNum + " for sending to " + bcp.getCC().getNodeId() + " for stream " + bcp.getSM().getStreamId());
		// If we currently have no pending pages, notify the seon conn that we
		// need to send data
		boolean tellConn;
		synchronized (this) {
			tellConn = (pageQ.size() == 0);
			pageQ.add(pageNum);
			auctionStatusQ.add(auctionStatus);
		}
		if (tellConn)
			seonConn.setDataProvider(this);
	}

	public ByteBuffer getMoreData() {
		long pageNum;
		int auctionStatus;
		synchronized (this) {
			if (pageQ.size() == 0)
				return null;
			pageNum = pageQ.removeFirst();
			auctionStatus = auctionStatusQ.removeFirst();
		}
		Page p;
		try {
			p = bcp.getSM().getPageBuffer().getPage(pageNum);
		} catch (IOException e) {
			log.error("Error fetching page " + pageNum + " for stream " + bcp.getSM().getStreamId() + ": " + e.getMessage());
			bcp.die();
			return null;
		}
		p.setAuctionStatus(auctionStatus);
		int bufSz = serial.sizeOfSerializedPage(p);
		ByteBuffer sendBuf = ByteBuffer.allocate(bufSz);
		serial.serializePage(p, sendBuf);
		sendBuf.flip();
		log.debug("Sending page " + pageNum + " to " + bcp.getCC().getNodeId() + " for stream " + bcp.getSM().getStreamId());
		return sendBuf;
	}

	public void setGamma(float gamma) {
		seonConn.setGamma(gamma);
	}

	public void setBCPair(BCPair bcPair) {
		this.bcp = bcPair;
	}

	public int getFlowRate() {
		return seonConn.getOutFlowRate();
	}

}
