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

import org.apache.commons.logging.Log;

import com.robonobo.common.concurrent.CatchingRunnable;
import com.robonobo.common.util.FlowRateIndicator;

public abstract class EONConnection {
	protected EONManager mgr;
	FlowRateIndicator inFlowRate = new FlowRateIndicator();
	FlowRateIndicator outFlowRate = new FlowRateIndicator();

	public EONConnection(EONManager mgr) {
		this.mgr = mgr;
	}

	public abstract void close();

	public abstract void abort();

	public abstract EonSocketAddress getRemoteSocketAddress();

	public abstract EonSocketAddress getLocalSocketAddress() throws EONException;

	public abstract float getGamma();

	abstract boolean acceptVisitor(PktSendVisitor visitor) throws EONException;

	abstract void receivePacket(EONPacket pkt) throws EONException;

	protected EONConnectionListener listener;

	public void addListener(EONConnectionListener listener) {
		this.listener = listener;
	}

	public void removeListener(EONConnectionListener listener) {
		this.listener = null;
	}

	protected synchronized void fireOnClose() {
		EONConnectionListener myListener = listener;
		if (myListener == null)
			return;
		EONConnectionEvent event = new EONConnectionEvent(this);
		// Small chance the executor might throw an exception here if we've
		// exited
		try {
			mgr.getExecutor().execute(new CloseRunner(myListener, event));
		} catch (Exception ignore) {
		}
	}

	/**
	 * Bps
	 */
	public int getInFlowRate() {
		return inFlowRate.getFlowRate();
	}

	/**
	 * Bps
	 */
	public int getOutFlowRate() {
		return outFlowRate.getFlowRate();
	}

	private class CloseRunner extends CatchingRunnable {
		private final EONConnectionListener listener;
		private final EONConnectionEvent event;

		public CloseRunner(EONConnectionListener listener, EONConnectionEvent event) {
			this.listener = listener;
			this.event = event;
		}

		public void doRun() {
			listener.onClose(event);
		}
	}
}
