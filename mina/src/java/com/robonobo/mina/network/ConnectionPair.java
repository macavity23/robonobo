package com.robonobo.mina.network;

import org.apache.commons.logging.Log;

import com.google.protobuf.GeneratedMessage;
import com.robonobo.common.concurrent.CatchingRunnable;
import com.robonobo.mina.instance.MinaInstance;
import com.robonobo.mina.stream.StreamMgr;
import com.robonobo.mina.util.MinaConnectionException;

public abstract class ConnectionPair {
	protected ControlConnection cc;
	protected StreamMgr sm;
	protected MinaInstance mina;
	protected Log log;

	ConnectionPair(StreamMgr sm, ControlConnection cc) {
		this.sm = sm;
		mina = sm.getMinaInstance();
		this.cc = cc;
		log = mina.getLogger(getClass());
	}

	public void die() {
		// Calling higher syncpriority (than subclass), use separate thread
		mina.getExecutor().execute(new CatchingRunnable() {
			public void doRun() {
				sm.notifyDeadConnection(ConnectionPair.this);
			}
		});
	}

	/** Bytes per sec */
	public abstract int getFlowRate();
	
	public ControlConnection getCC() {
		return cc;
	}

	public StreamMgr getSM() {
		return sm;
	}

	public void sendMessage(String msgName, GeneratedMessage msg) {
		cc.sendMessage(msgName, msg);
	}

}
