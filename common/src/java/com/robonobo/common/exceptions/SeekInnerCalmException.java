package com.robonobo.common.exceptions;

@SuppressWarnings("serial")
public class SeekInnerCalmException extends RuntimeException {
	public SeekInnerCalmException() {
		super("Seek inner calm by rearranging the water feature");
	}

	public SeekInnerCalmException(Throwable t) {
		super("Seek inner calm by rearranging the water feature", t);
	}

	public SeekInnerCalmException(String message, Throwable cause) {
		super(message, cause);
	}

	public SeekInnerCalmException(String message) {
		super(message);
	}
}
