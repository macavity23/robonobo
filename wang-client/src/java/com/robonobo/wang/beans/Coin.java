package com.robonobo.wang.beans;

import java.math.BigInteger;

import com.robonobo.wang.proto.WangProtocol.CoinMsg;
import com.robonobo.wang.proto.WangProtocol.CoinMsg.Builder;

/**
 * All wang coins are valued as a power of 2. So, denom 1 means 2 wang, denom 0
 * means 1 wang, denom 4 means 8 wang, denom -2 means 0.25 wang, etc
 */
public class Coin extends uk.co.aldigital.ben.lucre.Coin {
	private int denom;

	public Coin() {
	}

	public Coin(BigInteger coinId, BigInteger signature) {
		super(coinId, signature);
	}

	public Coin(CoinMsg msg) {
		super(new BigInteger(msg.getCoinId(), 16), new BigInteger(msg.getSignature(), 16));
		denom = msg.getDenom();
	}
	
	public int getDenom() {
		return denom;
	}

	public void setDenom(int value) {
		this.denom = value;
	}

	public String toString() {
		return "WangCoin[" + denom + "]";
	}

	public CoinMsg toMsg() {
		Builder bldr = CoinMsg.newBuilder();
		bldr.setCoinId(getCoinId().toString(16));
		bldr.setDenom(denom);
		bldr.setSignature(getSignature().toString(16));
		return bldr.build();
	}
}
