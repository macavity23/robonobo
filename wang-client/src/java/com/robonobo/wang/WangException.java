package com.robonobo.wang;

public abstract class WangException extends Exception {

	public WangException() {
		super();
	}

	public WangException(String message, Throwable cause) {
		super(message, cause);
	}

	public WangException(String message) {
		super(message);
	}

	public WangException(Throwable cause) {
		super(cause);
	}

}
