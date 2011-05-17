package com.robonobo.mina.instance;

import java.util.*;

import com.robonobo.common.concurrent.CatchingRunnable;
import com.robonobo.mina.network.BCPair;
import com.robonobo.mina.network.LCPair;

public class FlowRateMgr {
	MinaInstance mina;
	Map<String, Integer> bMap = Collections.synchronizedMap(new HashMap<String, Integer>());
	Map<String, Integer> lMap = Collections.synchronizedMap(new HashMap<String, Integer>());
	long lastFRUpdate = 0;
	
	public FlowRateMgr(MinaInstance mina) {
		this.mina = mina;
	}
	
	/**
	 * Bytes/sec
	 */
	public int getBroadcastingFlowRate(String sid) {
		checkAndUpdateFlowRates();
		Integer result = bMap.get(sid);
		if(result == null)
			return 0;
		return result;
	}
	
	/**
	 * Bytes/sec
	 */
	public int getListeningFlowRate(String sid) {
		checkAndUpdateFlowRates();
		Integer result = lMap.get(sid);
		if(result == null)
			return 0;
		return result;
	}

	private void checkAndUpdateFlowRates() {
		// If it's been more than a second, fire off a thread to update the flow rate
		// Avoids repeated iteration over connections, and nary a synch block in sight
		// This means this flowrate data will be out of date, but this shouldn't matter
		long now = System.currentTimeMillis();
		if((now - lastFRUpdate) > 1000l) {
			lastFRUpdate = now;
			mina.getExecutor().execute(new CatchingRunnable() {
				public void doRun() throws Exception {
					Set<String> liveSids = new HashSet<String>(Arrays.asList(mina.getStreamMgr().getLiveStreamIds()));
					// Add any sids we currently have values for, to make sure they're removed if they're now 0
					liveSids.addAll(bMap.keySet());
					liveSids.addAll(lMap.keySet());
					for (String sid : liveSids) {
						int bfr = 0;
						BCPair[] bcps = mina.getSCM().getBroadcastConns(sid);
						for (int i = 0; i < bcps.length; i++) {
							bfr += bcps[i].getFlowRate();
						}
						if(bfr == 0)
							bMap.remove(sid);
						else
							bMap.put(sid, bfr);
						int lfr = 0;
						LCPair[] lcps = mina.getSCM().getListenConns(sid);
						for (int i = 0; i < lcps.length; i++) {
							lfr += lcps[i].getFlowRate();
						}
						if(lfr == 0)
							lMap.remove(sid);
						else
							lMap.put(sid, lfr);
					}
				}
			});
		}
	}

}
