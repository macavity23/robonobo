package com.robonobo.wang.beans;

import java.math.BigInteger;

import com.robonobo.wang.proto.WangProtocol.CoinRequestMsg;

import uk.co.aldigital.ben.lucre.PublicCoinRequest;

public class CoinRequestPublic extends PublicCoinRequest {
	private int denom;

	public CoinRequestPublic() {
	}

	public CoinRequestPublic(BigInteger request) {
		super(request);
	}

	public CoinRequestPublic(CoinRequestMsg msg) {
		super(new BigInteger(msg.getRequest(), 16));
		denom = msg.getDenom();
	}

	public CoinRequestPublic(CoinRequestPrivate coinReqPriv) {
		super(coinReqPriv.getRequest());
		denom = coinReqPriv.getDenom();
	}

	public int getDenom() {
		return denom;
	}

	public void setDenom(int value) {
		this.denom = value;
	}

	public CoinRequestMsg toMsg() {
		return CoinRequestMsg.newBuilder().setRequest(getRequest().toString(16)).setDenom(denom).build();
	}
}
