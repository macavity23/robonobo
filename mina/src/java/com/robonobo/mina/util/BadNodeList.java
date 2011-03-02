package com.robonobo.mina.util;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;

import com.robonobo.mina.instance.MinaInstance;


/**
 * @syncpriority 50
 */
public class BadNodeList {
	private MinaInstance mina;
	private Map<String, Date> badNodeTryTime;
	private Log log;
	/**
	 * Seconds
	 */
	private Map<String, Integer> badNodeDuration;

	/**
	 * @syncpriority 50
	 */
	public synchronized void clear() {
		badNodeTryTime.clear();
		badNodeDuration.clear();
	}

	public BadNodeList(MinaInstance mina) {
		this.mina = mina;
		log = mina.getLogger(getClass());
		badNodeTryTime = new HashMap<String, Date>();
		badNodeDuration = new HashMap<String, Integer>();
	}

	/**
	 * Checks to see if we are currently not speaking to this node for being bad.
	 * @syncpriority 50
	 * @return true if the node is bad, false otherwise
	 */
	public synchronized boolean checkBadNode(String nodeId) {
		Date nodeTryTime = badNodeTryTime.get(nodeId);
		if(nodeTryTime == null) return false;
		if(nodeTryTime.before(new Date())) return false;
		return true;
	}

	/**
	 * @syncpriority 50
	 */
	public synchronized void markNodeAsBad(String nodeId) {
		Date nodeTryTime = badNodeTryTime.get(nodeId);
		if(nodeTryTime == null)
			markNodeAsBad(nodeId, mina.getConfig().getInitialBadNodeTimeout());
		else
			markNodeAsBad(nodeId, (badNodeDuration.get(nodeId)).intValue() * 2);
	}

	/**
	 * @syncpriority 50
	 */
	public synchronized void markNodeAsBad(String nodeId, int durationSecs) {
		log.debug("Marking node " + nodeId + " as bad for "+durationSecs+" seconds");
		Date nowTime = new Date();
		Date nextTryTime = new Date(nowTime.getTime() + durationSecs * 1000);
		badNodeTryTime.put(nodeId, nextTryTime);
		badNodeDuration.put(nodeId, new Integer(durationSecs));		
	}
	/**
	 * @syncpriority 50
	 */
	public synchronized void markNodeAsGood(String nodeId) {
		badNodeTryTime.remove(nodeId);
		badNodeDuration.remove(nodeId);
	}
}
