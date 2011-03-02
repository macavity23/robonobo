package com.robonobo.wang.beans;

import java.math.BigInteger;

import com.robonobo.wang.proto.WangProtocol.BlindedCoinMsg;

public class BlindedCoin extends uk.co.aldigital.ben.lucre.BlindedCoin {
	private int denom;

	public BlindedCoin() {
	}

	public BlindedCoin(BigInteger blindedSignature) {
		super(blindedSignature);
	}

	public BlindedCoin(BlindedCoinMsg msg) {
		super(new BigInteger(msg.getBlindedSignature(), 16));
		denom = msg.getDenom();
	}
	
	public int getDenom() {
		return denom;
	}

	public void setDenom(int value) {
		this.denom = value;
	}
	
	public BlindedCoinMsg toMsg() {
		return BlindedCoinMsg.newBuilder().setDenom(denom).setBlindedSignature(getSignature().toString(16)).build();
	}
}
