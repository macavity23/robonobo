package com.robonobo.wang.client;

import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;

import com.robonobo.wang.beans.BlindedCoin;
import com.robonobo.wang.beans.Coin;
import com.robonobo.wang.beans.CoinRequestPrivate;
import com.robonobo.wang.beans.CoinRequestPublic;
import com.robonobo.wang.beans.DenominationPrivate;
import com.robonobo.wang.beans.DenominationPublic;

public class LucreFacade {
	public LucreFacade() {
	}

	public DenominationPrivate createDenomination(int denomExponent, int keyLength) {
		DenominationPrivate d = new DenominationPrivate(keyLength);
		d.setDenom(denomExponent);
		return d;
	}

	public CoinRequestPrivate createCoinRequest(DenominationPublic denom) {
		CoinRequestPrivate c;
		try {
			c = new CoinRequestPrivate(denom);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
		c.setDenom(denom.getDenom());
		return c;
	}

	public BlindedCoin signCoinRequest(DenominationPrivate denomPriv, CoinRequestPublic coinReqPub) {
		BigInteger signature = denomPriv.signRequest(coinReqPub);
		BlindedCoin coin = new BlindedCoin(signature);
		coin.setDenom(denomPriv.getDenom());
		return coin;
	}

	public Coin unblindCoin(DenominationPublic denomPub, BlindedCoin blindedCoin, CoinRequestPrivate coinReqPriv) {
		uk.co.aldigital.ben.lucre.Coin lucreCoin = coinReqPriv.processResponse(denomPub, blindedCoin.getSignature());
		Coin wangCoin = new Coin(lucreCoin.getCoinId(), lucreCoin.getSignature());
		wangCoin.setDenom(denomPub.getDenom());
		return wangCoin;
	}

	public boolean verifyCoin(DenominationPrivate denomPriv, Coin coin) {
		try {
			return denomPriv.verify(coin);
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException(e);
		}
	}
}
