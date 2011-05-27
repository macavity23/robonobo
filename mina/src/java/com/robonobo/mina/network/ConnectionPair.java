package com.robonobo.mina.network;

import org.apache.commons.logging.Log;

import com.google.protobuf.GeneratedMessage;
import com.robonobo.common.concurrent.CatchingRunnable;
import com.robonobo.mina.instance.MinaInstance;
import com.robonobo.mina.instance.StreamMgr;
import com.robonobo.mina.util.MinaConnectionException;

public abstract class ConnectionPair {
	protected ControlConnection cc;
	protected String sid;
	protected MinaInstance mina;
	protected Log log;

	ConnectionPair(MinaInstance mina, String sid, ControlConnection cc) {
		this.sid = sid;
		this.mina = mina;
		this.cc = cc;
		log = mina.getLogger(getClass());
	}

	public void die() {
		// Calling higher syncpriority (than subclass), use separate thread
		mina.getExecutor().execute(new CatchingRunnable() {
			public void doRun() {
				mina.getStreamMgr().notifyDeadConnection(ConnectionPair.this);
			}
		});
	}

	/** Bytes per sec */
	public abstract int getFlowRate();
	
	public abstract void abort();
	
	public ControlConnection getCC() {
		return cc;
	}

	public String getStreamId() {
		return sid;
	}
	
	public void sendMessage(String msgName, GeneratedMessage msg) {
		cc.sendMessage(msgName, msg);
	}

}
