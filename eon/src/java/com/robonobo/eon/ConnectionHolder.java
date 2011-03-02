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


import java.net.*;
import java.util.*;

import org.apache.commons.logging.Log;



public class ConnectionHolder {
	private Map<Integer, EONConnection> defaultConns;
	private Map<Integer, Map<EonSocketAddress,EONConnection>> specificConns;
	private List<EONConnection> connsToWaitFor = null;
	private Log log;
	private EONManager mgr;

	public ConnectionHolder(EONManager mgr) {
		this.mgr = mgr;
		defaultConns = new HashMap<Integer, EONConnection>();
		specificConns = new HashMap<Integer, Map<EonSocketAddress,EONConnection>>();
		log = mgr.getLogger(getClass());
	}

	int numConnsWaitingFor() {
		List<EONConnection> tmpList = connsToWaitFor;
		if(tmpList == null) return 0;
		return tmpList.size();
	}
	
	void closeAllConns(int waitTimeMs) {
		log.debug("Closing all EON connections");
		Date startWaitingTime = new Date();
		Date endWaitingTime = new Date(startWaitingTime.getTime()+waitTimeMs);
		List<EONConnection> connsToClose = new ArrayList<EONConnection>();
		connsToWaitFor = new ArrayList<EONConnection>();
		// Come out of this sync block before closing anything, else EONConnection.close() may cause a race condition
		synchronized(this) {
			ArrayList<EONConnection> allConns = new ArrayList<EONConnection>(defaultConns.values());
			Iterator<Map<EonSocketAddress,EONConnection>> spIter = specificConns.values().iterator();
			while(spIter.hasNext()) {
				Map<EonSocketAddress,EONConnection> thisMap = spIter.next();
				allConns.addAll(thisMap.values());
			}
			Iterator<EONConnection> allIter = allConns.iterator();
			while(allIter.hasNext()) {
				EONConnection conn = allIter.next();
				if(conn instanceof SEONConnection)
					connsToWaitFor.add(conn);
				connsToClose.add(conn);
			}
		}
		Iterator<EONConnection> i = connsToClose.iterator();
		while(i.hasNext()) {
			EONConnection conn = i.next();
			conn.close();
		}
		synchronized(this) {
			while(connsToWaitFor.size() > 0) {
				SEONConnection thisConn = (SEONConnection) connsToWaitFor.get(0);
				if(!thisConn.isOpen())
					connsToWaitFor.remove(0);
				else {
					try {
						Date nowTime = new Date();
						long msToWait = endWaitingTime.getTime() - nowTime.getTime();
						if(msToWait <= 0) {
							log.debug("EON finished waiting for connections");
							break;
						} else {
							log.debug("EON waiting for "+connsToWaitFor+" conns, "+msToWait+"ms");
							wait(msToWait);
						}
					} catch(InterruptedException e) {
						log.debug("EON caught InterruptedException while closing down", e);
					}
				}
			}
		}
		for (EONConnection eonConn : connsToWaitFor) {
			SEONConnection seonConn = (SEONConnection) eonConn;
			seonConn.cancelTimeouts();
		}
		connsToWaitFor.clear();
		log.debug("All EON conns closed");
	}

	synchronized boolean requestPort(int localEONPort, EonSocketAddress addressMask, EONConnection thisConn) {
		Integer eonp = new Integer(localEONPort);
		if(addressMask.getAddress().equals(EONManager.getWildcardAddress())) {
			log.debug("EONManager: Port " + localEONPort + " requested for listening");
			if(defaultConns.containsKey(eonp)) {
				return false;
			}
			else {
				defaultConns.put(eonp, thisConn);
				return true;
			}
		}
		else {
			log.debug("EONManager: Port " + localEONPort + " requested for address mask " + addressMask);
			// This is a child connection caused by a remote process
			// connecting to a local listening socket
			if(specificConns.containsKey(eonp)) {
				Map<EonSocketAddress, EONConnection> portMap = specificConns.get(eonp);
				if(portMap.containsKey(addressMask)) {
					return false;
				}
				else {
					portMap.put(addressMask, thisConn);
					return true;
				}
			}
			else {
				Map<EonSocketAddress, EONConnection> map = new HashMap<EonSocketAddress, EONConnection>();
				map.put(addressMask, thisConn);
				specificConns.put(new Integer(localEONPort), map);
				return true;
			}
		}
	}

	synchronized int getPort(EonSocketAddress addressMask, EONConnection thisConn) {
		for(int i = 1025; i < 65535; i++) {
			Integer thisInt = new Integer(i);
			if(!defaultConns.containsKey(thisInt) && !specificConns.containsKey(thisInt)) {
				if(!requestPort(i, addressMask, thisConn))
					throw new RuntimeException("RequestPort failed after being called from GetPort");
				return i;
			}
		}
		// We've run out of ports! Something pretty bad is going on...
		throw new RuntimeException("No more EON ports available");
	}

	synchronized void returnPort(int localEONPort, EonSocketAddress addressMask, EONConnection thisConn) {
		Integer eonp = new Integer(localEONPort);
		if(addressMask.getAddress().equals(EONManager.getWildcardAddress())) {
			log.debug("EONManager: Port " + localEONPort + " returned for listening");
			if(defaultConns.containsKey(eonp)) {
				if(defaultConns.get(eonp).equals(thisConn))
					defaultConns.remove(eonp);
				else
					throw new IllegalArgumentException("This connection does not own this address mask on this port");
			}
			else
				log.debug("No such address mask/port when returning port " + localEONPort + " for address mask "
						+ addressMask);
		}
		else {
			log.debug("EONManager: Port " + localEONPort + " returned for address mask " + addressMask);
			if(specificConns.containsKey(eonp)) {
				Map<EonSocketAddress, EONConnection> portMap = specificConns.get(eonp);
				if(portMap.containsKey(addressMask)) {
					if(portMap.get(addressMask) == thisConn) {
						portMap.remove(addressMask);
						if(portMap.size() == 0) specificConns.remove(eonp);
					}
					else
						throw new IllegalArgumentException("This connection does not own this address mask on this port");
				}
				else
					log.debug("No such address mask/port when returning port " + localEONPort + " for address mask "
							+ addressMask);
			}
			else
				log.debug("No such address mask/port when returning port " + localEONPort + " for address mask "
						+ addressMask);
		}
		// We might be waiting for connections to close();
		notifyAll();
	}

	void killAllConnsAssociatedWith(InetSocketAddress addr) {
		List<EONConnection> toDie = new ArrayList<EONConnection>();
		synchronized(this) {
			Iterator<Map<EonSocketAddress, EONConnection>> i = specificConns.values().iterator();
			while(i.hasNext()) {
				Map<EonSocketAddress, EONConnection> map = i.next();
				Iterator<EonSocketAddress> j = map.keySet().iterator();
				while(j.hasNext()) {
					EonSocketAddress ep = j.next();
					if(ep.getInetSocketAddress().equals(addr)) 
						toDie.add(map.get(ep));
				}
			}
		}
		Iterator<EONConnection> i = toDie.iterator();
		while(i.hasNext()) {
			EONConnection conn = i.next();
			conn.abort();
		}
	}

	synchronized EONConnection getLocalConnForIncoming(EonSocketAddress destSockAddr, EonSocketAddress sourceSockAddr) {
		EONConnection resultConn = null;
		Integer eonp = new Integer(destSockAddr.getEonPort());
		// We try and match the packet to an endpoint-specific
		// connection.
		// If this fails, chuck it to the default connection on that
		// port.
		if(specificConns.containsKey(eonp)) {
			Map<EonSocketAddress, EONConnection> portMap = specificConns.get(eonp);
			Iterator<EonSocketAddress> i = portMap.keySet().iterator();
			while(i.hasNext()) {
				EonSocketAddress thisEP = (EonSocketAddress) i.next();
				if(thisEP.equals(sourceSockAddr)) {
					resultConn = (EONConnection) portMap.get(thisEP);
					break;
				}
			}
		}
		if(resultConn == null) {
			if(defaultConns.containsKey(eonp)) resultConn = (EONConnection) defaultConns.get(eonp);
		}
		return resultConn;
	}

	synchronized boolean haveConnection(EONConnection conn, int localEonPort) {
		Integer eonPort = new Integer(localEonPort);
		if(defaultConns.containsValue(conn)) return true;
		if(specificConns.containsKey(eonPort)) {
			Map<EonSocketAddress, EONConnection> portMap = specificConns.get(eonPort);
			if(portMap.containsValue(conn)) return true;
		}
		return false;
	}
	
	synchronized int getLowestMaxObservedRtt(SEONConnection exceptConn) {
		// TODO: Optimise me!  This will perform horribly with lots of connections
		int result = -1;
		for (Map<EonSocketAddress, EONConnection> connMap : specificConns.values()) {
			for (EONConnection conn : connMap.values()) {
				if(conn == exceptConn)
					continue;
				int thisMax = ((SEONConnection)conn).getMaxObservedRtt();
				if(thisMax > 0 && ((thisMax < result) || result < 0))
					result = thisMax;				
			}
		}
		return result;
	}

}
