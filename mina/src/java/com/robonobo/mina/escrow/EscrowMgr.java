package com.robonobo.mina.escrow;

import java.util.HashMap;
import java.util.Map;

import com.robonobo.common.concurrent.Attempt;
import com.robonobo.core.api.proto.CoreApi.Node;
import com.robonobo.mina.instance.MinaInstance;
import com.robonobo.mina.message.proto.MinaProtocol.AdvEscrow;
import com.robonobo.mina.message.proto.MinaProtocol.QueryEscrow;
import com.robonobo.mina.network.ControlConnection;

/**
 * Handles use of escrow servers
 */
public class EscrowMgr {
	MinaInstance mina;
	Map<String, AdvEscrow> advEscrowMap = new HashMap<String, AdvEscrow>();

	public EscrowMgr(MinaInstance mina) {
		this.mina = mina;
	}
	
	public void notifySuccessfulConnection(ControlConnection cc) {
		for (Node escrowNode : mina.getCurrencyClient().getTrustedEscrowNodes()) {
			if(cc.getNodeId().equals(escrowNode.getId())) {
				// Ask them for escrow services
				cc.sendMessage("QueryEscrow", QueryEscrow.getDefaultInstance());
				return;
			}
		}
	}
	
	public void gotAdvEscrow(String fromNodeId, AdvEscrow ae) {
		advEscrowMap.put(fromNodeId, ae);
	}
	
	public boolean isAcceptableEscrowProvider(String nodeId) {
		for (Node n : mina.getCurrencyClient().getTrustedEscrowNodes()) {
			if(n.getId().equals(nodeId))
				return true;
		}
		return false;
	}

	public String startNewEscrow(double cashToSend) {
		// TODO: Implement me
		return null;
	}

	public void setupEscrowAccount(String escrowProvId, Attempt a) {
		// TODO: Implement me
	}
}
