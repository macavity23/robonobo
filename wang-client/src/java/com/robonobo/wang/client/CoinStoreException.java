package com.robonobo.wang.client;

import com.robonobo.wang.WangException;

public class CoinStoreException extends WangException {

	public CoinStoreException() {
	}

	public CoinStoreException(String message) {
		super(message);
	}

	public CoinStoreException(Throwable cause) {
		super(cause);
	}

	public CoinStoreException(String message, Throwable cause) {
		super(message, cause);
	}

}
