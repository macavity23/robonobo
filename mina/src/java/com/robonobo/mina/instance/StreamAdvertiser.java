package com.robonobo.mina.instance;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.robonobo.common.concurrent.Batcher;
import com.robonobo.common.concurrent.CatchingRunnable;
import com.robonobo.mina.message.proto.MinaProtocol.AdvSource;

public class StreamAdvertiser extends Batcher<String> {
	MinaInstance mina;
	Log log = LogFactory.getLog(getClass());

	public StreamAdvertiser(MinaInstance mina) {
		super(mina.getConfig().getSourceRequestBatchTime(), mina.getExecutor());
		this.mina = mina;
	}

	/**
	 * @syncpriority 140
	 */
	public void advertiseStream(String streamId) {
		// If we don't yet have a connection to a supernode, don't advertise, as
		// when we do connect we will send an advert for all [re]broadcasting
		// streams, and we'd like to avoid sending duplicate streams on startup
		// (If we have local conns, they need to know about our broadcasts)
		if (mina.getCCM().haveSupernode() || mina.getCCM().haveLocalConn())
			add(streamId);
	}

	@Override
	protected void runBatch(final Collection<String> streamIds) {
		// If our currency client isn't ready yet, don't advertise until it is - keep track of it here and background poll as required
		if(mina.getConfig().isAgoric() && !mina.getCurrencyClient().isReady()) {
			log.debug("Waiting 5s to advertise streams - currency client not yet ready");
			mina.getExecutor().schedule(new CatchingRunnable() {
				public void doRun() throws Exception {
					runBatch(streamIds);
				}
			}, 5, TimeUnit.SECONDS);
			return;
		}
		AdvSource as = AdvSource.newBuilder().addAllStreamId(streamIds).build();
		mina.getCCM().sendMessageToNetwork("AdvSource", as);
	}

}
