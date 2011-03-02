package com.robonobo.core.api;

public class CurrencyException extends Exception {

	public CurrencyException() {
	}

	public CurrencyException(String message) {
		super(message);
	}

	public CurrencyException(Throwable cause) {
		super(cause);
	}

	public CurrencyException(String message, Throwable cause) {
		super(message, cause);
	}

}
