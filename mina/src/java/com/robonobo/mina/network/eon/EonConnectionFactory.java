package com.robonobo.mina.network.eon;


import com.robonobo.core.api.proto.CoreApi.EndPoint;
import com.robonobo.eon.EONException;
import com.robonobo.eon.EONManager;
import com.robonobo.mina.external.node.EonEndPoint;
import com.robonobo.mina.external.node.SeonEndPoint;
import com.robonobo.mina.instance.MinaInstance;
import com.robonobo.mina.network.*;
import com.robonobo.mina.util.MinaConnectionException;

public class EonConnectionFactory implements StreamConnectionFactory {
	private EONManager eonMgr;
	private MinaInstance mina;
		
	public EonConnectionFactory(EONManager eonMgr, MinaInstance mina) {
		this.eonMgr = eonMgr;
		this.mina = mina;
	}

	public BroadcastConnection getBroadcastConnection(ControlConnection cc,
			EndPoint listeningEp) throws MinaConnectionException {
		// Ignore the host & udp port in the listening ep, just use the CC's details
		EonEndPoint listenEonEp = EonEndPoint.parse(listeningEp.getUrl());
		EonEndPoint ccEp = EonEndPoint.parse(cc.getTheirEp().getUrl());
		EonEndPoint ep = new SeonEndPoint(ccEp.getAddress(), ccEp.getUdpPort(), listenEonEp.getEonPort());
		EonBroadcastConnection bc;
		try {
			bc = new EonBroadcastConnection(mina, eonMgr, ep);
		} catch (EONException e) {
			throw new MinaConnectionException(e);
		}
		return bc;
	}

	public ListenConnection getListenConnection(ControlConnection cc) throws MinaConnectionException {
		EonListenConnection lc;
		try {
			lc = new EonListenConnection(mina, eonMgr);
		} catch (EONException e) {
			throw new MinaConnectionException(e);
		}
		return lc;
	}
}
