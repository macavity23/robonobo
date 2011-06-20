package com.robonobo.mina.instance;

import java.util.Collection;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.robonobo.common.concurrent.RateLimitedBatcher;
import com.robonobo.mina.message.proto.MinaProtocol.AdvSource;

public class StreamAdvertiser extends RateLimitedBatcher<String> {
	MinaInstance mina;
	Log log = LogFactory.getLog(getClass());

	public StreamAdvertiser(MinaInstance mina) {
		super(mina.getConfig().getStreamAdvertBatchTime(), mina.getExecutor(), (mina.getConfig().getStreamAdvertBatchTime() / 1000 * mina.getConfig().getStreamAdvertMaxPerSec()));
		this.mina = mina;
	}

	/**
	 * @syncpriority 140
	 */
	public void advertiseStream(String sid) {
		// If we don't yet have a connection to a supernode, don't advertise, as
		// when we do connect we will send an advert for all [re]broadcasting
		// streams, and we'd like to avoid sending duplicate streams on startup
		// (If we have local conns, they need to know about our broadcasts)
		if (mina.getCCM().haveSupernode() || mina.getCCM().haveLocalConn())
			add(sid);
	}

	/**
	 * @syncpriority 140
	 */
 	public void advertiseStreams(Collection<String> sids) {
		if (mina.getCCM().haveSupernode() || mina.getCCM().haveLocalConn())
			addAll(sids);
	}

	@Override
	protected void runBatch(final Collection<String> streamIds) {
		// If our currency client isn't ready yet, don't advertise until it is - retry in a lil while
		if (mina.getConfig().isAgoric() && !mina.getCurrencyClient().isReady()) {
			log.debug("Waiting to advertise streams - currency client not yet ready");
			addAll(streamIds);
			return;
		}
		AdvSource as = AdvSource.newBuilder().addAllStreamId(streamIds).build();
		mina.getCCM().sendMessageToNetwork("AdvSource", as);
	}

}
