package com.robonobo.wang.beans;

import java.math.BigInteger;

import uk.co.aldigital.ben.lucre.Bank;

public class DenominationPrivate extends Bank {
	private int denom;

	public DenominationPrivate(BigInteger generator, BigInteger prime, BigInteger publicKey, BigInteger privateKey) {
		super(generator, prime, publicKey, privateKey);
	}

	/** Don't call this directly, use LucreFacade.createDenomination() */
	public DenominationPrivate(int keyLength) {
		super(keyLength);
	}

	public int getDenom() {
		return denom;
	}

	public void setDenom(int value) {
		this.denom = value;
	}
}
