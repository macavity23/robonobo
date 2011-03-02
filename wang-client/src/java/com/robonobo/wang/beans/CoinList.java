package com.robonobo.wang.beans;

import com.robonobo.wang.proto.WangProtocol.CoinListMsg;
import com.robonobo.wang.proto.WangProtocol.CoinMsg;

/** Wrapper class around CoinListMsg */
public class CoinList {
	/** Never instantiate this class */
	private CoinList() {
	}

	public static double totalValue(CoinListMsg coinList) {
		double result = 0;
		for (CoinMsg coin : coinList.getCoinList()) {
			result += Math.pow(2, coin.getDenom());
		}
		return result;
	}
}
