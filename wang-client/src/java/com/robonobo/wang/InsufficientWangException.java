package com.robonobo.wang;

public class InsufficientWangException extends WangException {
	public InsufficientWangException() {
		super("Your wang is insufficient!");
	}
}
