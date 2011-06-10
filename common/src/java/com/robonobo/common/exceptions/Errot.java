package com.robonobo.common.exceptions;

/**
 * errot
 */
@SuppressWarnings("serial")
public class Errot extends RuntimeException {
	public Errot() {
		super("Errot");
	}

	public Errot(Throwable t) {
		super("Errot", t);
	}

	public Errot(String message, Throwable cause) {
		super(message, cause);
	}

	public Errot(String message) {
		super(message);
	}
}
