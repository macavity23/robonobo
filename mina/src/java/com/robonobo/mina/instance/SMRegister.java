package com.robonobo.mina.instance;

import java.util.*;

import com.robonobo.mina.external.buffer.PageBuffer;
import com.robonobo.mina.stream.StreamMgr;

/**
 * @syncpriority 25
 */
public class SMRegister {
	private MinaInstance mina;
	private Map<String, StreamMgr> smsByStreamId = new HashMap<String, StreamMgr>();
	private Set<String> liveSms = new HashSet<String>();

	public SMRegister(MinaInstance mina) {
		this.mina = mina;
	}

	/**
	 * @syncpriority 25
	 */
	public synchronized StreamMgr getOrCreateSM(String streamId, PageBuffer pageBuf) {
		StreamMgr sm = smsByStreamId.get(streamId);
		if (sm != null) {
			// If the streammgr currently has no pagebuf, it means it was
			// created just to see how many broadcasters there are for this
			// stream. If a pagebuf is provided now, it's for reception or
			// broadcast, so set the pagebuf on the streammgr
			if (pageBuf != null)
				sm.setPageBuffer(pageBuf);
			return sm;
		}
		sm = new StreamMgr(mina, streamId, pageBuf);
		smsByStreamId.put(streamId, sm);
		return sm;
	}

	/**
	 * @syncpriority 25
	 */
	public synchronized StreamMgr getSM(String streamId) {
		return smsByStreamId.get(streamId);
	}

	/**
	 * @syncpriority 25
	 */
	public synchronized StreamMgr[] getAllSMs() {
		StreamMgr[] arr = new StreamMgr[smsByStreamId.size()];
		smsByStreamId.values().toArray(arr);
		return arr;
	}

	public synchronized StreamMgr[] getLiveSMs() {
		StreamMgr[] arr = new StreamMgr[liveSms.size()];
		int i=0;
		for (String sid : liveSms) {
			arr[i++] = smsByStreamId.get(sid);
		}
		return arr;
	}
	
	public synchronized void updateSmStatus(String streamId, boolean alive) {
		if(alive)
			liveSms.add(streamId);
		else
			liveSms.remove(streamId);
	}
	
	/**
	 * @syncpriority 25
	 */
	public synchronized void unregisterSM(String streamId) {
		smsByStreamId.remove(streamId);
		liveSms.remove(streamId);
	}	
}
