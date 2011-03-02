package com.robonobo.wang.beans;

import java.math.BigInteger;

import com.robonobo.wang.proto.WangProtocol.DenominationMsg;

import uk.co.aldigital.ben.lucre.PublicBank;

public class DenominationPublic extends PublicBank {
	private int denom;

	public DenominationPublic() {
	}

	public DenominationPublic(BigInteger generator, BigInteger prime, BigInteger publicKey) {
		super(generator, prime, publicKey);
	}

	public DenominationPublic(DenominationMsg msg) {
		super(new BigInteger(msg.getGenerator(), 16), new BigInteger(msg.getPrime(), 16), new BigInteger(msg.getPublicKey(), 16));
		denom = msg.getDenom();
	}

	public DenominationPublic(DenominationPrivate denomPriv) {
		super(denomPriv.getGenerator(), denomPriv.getPrime(), denomPriv.getPublicKey());
		denom = denomPriv.getDenom();
	}

	public int getDenom() {
		return denom;
	}

	public void setDenom(int value) {
		this.denom = value;
	}

	public DenominationMsg toMsg() {
		DenominationMsg.Builder bldr = DenominationMsg.newBuilder();
		bldr.setDenom(denom);
		bldr.setGenerator(getGenerator().toString(16));
		bldr.setPrime(getPrime().toString(16));
		bldr.setPublicKey(getPublicKey().toString(16));
		return bldr.build();
	}
}
