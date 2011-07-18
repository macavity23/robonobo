package com.robonobo.common.serialization;

public class TemporarilyUnavailableException extends SerializationException {

	public TemporarilyUnavailableException() {
		super();
	}

	public TemporarilyUnavailableException(String message, Throwable cause) {
		super(message, cause);
	}

	public TemporarilyUnavailableException(String message) {
		super(message);
	}

	public TemporarilyUnavailableException(Throwable cause) {
		super(cause);
	}
	
}
