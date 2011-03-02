package com.robonobo.wang.beans;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;

import uk.co.aldigital.ben.lucre.DoubleCoinRequest;

public class CoinRequestPrivate extends DoubleCoinRequest {
	private int denom;

	public CoinRequestPrivate(BigInteger coinId, BigInteger blindingY, BigInteger blindingG, BigInteger coinRequest) {
		super(coinId, blindingY, blindingG, coinRequest);
	}

	/** Don't call this directly, use LucreFacade.createCoinRequest() */
	public CoinRequestPrivate(DenominationPublic denomPublic) throws NoSuchAlgorithmException {
		super(denomPublic);
	}

	public int getDenom() {
		return denom;
	}

	public void setDenom(int value) {
		this.denom = value;
	}
}
