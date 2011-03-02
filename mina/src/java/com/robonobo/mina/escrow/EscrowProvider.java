package com.robonobo.mina.escrow;

import com.robonobo.core.api.CurrencyClient;
import com.robonobo.mina.instance.MinaInstance;

/**
 * Runs escrow services on this node
 */
public class EscrowProvider {
	MinaInstance mina;
	CurrencyClient curClient;

	public EscrowProvider(MinaInstance mina) {
		this.mina = mina;
	}
	
	public void setCurrencyClient(CurrencyClient client) {
		this.curClient = client;
	}
	
	/**
	 * Percentage
	 * @return
	 */
	public double getEscrowFee() {
		return (mina.getConfig().getEscrowFee() / 100d);
	}
	
	public double getOpeningBalance() {
		return curClient.getOpeningBalance();
	}
}
