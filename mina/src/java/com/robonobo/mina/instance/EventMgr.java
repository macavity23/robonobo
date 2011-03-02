package com.robonobo.mina.instance;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.robonobo.common.concurrent.CatchingRunnable;
import com.robonobo.mina.external.ConnectedNode;
import com.robonobo.mina.external.MinaListener;


public class EventMgr {
	private MinaInstance mina;
	private List<MinaListener> minaListeners = new ArrayList<MinaListener>();
	private Log log;

	public EventMgr(MinaInstance mina) {
		this.mina = mina;
		log = mina.getLogger(getClass());
	}

	void addMinaListener(MinaListener listener) {
		minaListeners.add(listener);
	}

	void fireMinaStarted() {
		mina.getExecutor().execute(new CatchingRunnable() {
			public void doRun() throws Exception {
				for (MinaListener listener : getArr()) {
					listener.minaStarted(mina);
				}
			}
		});
	}

	void fireMinaStopped() {
		mina.getExecutor().execute(new CatchingRunnable() {
			public void doRun() throws Exception {
				for (MinaListener listener : getArr()) {
					listener.minaStopped(mina);
				}
			}
		});
	}

	public void fireBroadcastStarted(final String streamId) {
		mina.getExecutor().execute(new CatchingRunnable() {
			public void doRun() throws Exception {
				for (MinaListener listener : getArr()) {
					listener.broadcastStarted(streamId);
				}
			}
		});
	}

	public void fireReceptionStarted(final String streamId) {
		mina.getExecutor().execute(new CatchingRunnable() {
			public void doRun() throws Exception {
				for (MinaListener listener : getArr()) {
					listener.receptionStarted(streamId);
				}
			}
		});
	}

	public void fireBroadcastStopped(final String streamId) {
		mina.getExecutor().execute(new CatchingRunnable() {
			public void doRun() throws Exception {
				for (MinaListener listener : getArr()) {
					listener.broadcastStopped(streamId);
				}
			}
		});
	}

	public void fireReceptionStopped(final String streamId) {
		mina.getExecutor().execute(new CatchingRunnable() {
			public void doRun() throws Exception {
				for (MinaListener listener : getArr()) {
					listener.receptionStopped(streamId);
				}
			}
		});
	}

	public void fireReceptionCompleted(final String streamId) {
		mina.getExecutor().execute(new CatchingRunnable() {
			public void doRun() throws Exception {
				for (MinaListener listener : getArr()) {
					listener.receptionCompleted(streamId);
				}
			}
		});
	}

	public void fireReceptionConnsChanged(final String streamId) {
		mina.getExecutor().execute(new CatchingRunnable() {
			public void doRun() throws Exception {
				for (MinaListener listener : getArr()) {
					listener.receptionConnsChanged(streamId);
				}
			}
		});
	}

	public void fireNodeConnected(final ConnectedNode node) {
		mina.getExecutor().execute(new CatchingRunnable() {
			public void doRun() throws Exception {
				MinaListener[] arrrrr = getArr();
				for (MinaListener listener : arrrrr) {
					listener.nodeConnected(node);
				}
			}
		});
	}

	public void fireNodeDisconnected(final ConnectedNode node) {
		mina.getExecutor().execute(new CatchingRunnable() {
			public void doRun() throws Exception {
				for (MinaListener listener : getArr()) {
					listener.nodeDisconnected(node);
				}
			}
		});
	}

	private MinaListener[] getArr() {
		MinaListener[] arr = new MinaListener[minaListeners.size()];
		minaListeners.toArray(arr);
		return arr;
	}
}
