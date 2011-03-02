package com.robonobo.mina.util;

import com.robonobo.mina.external.MinaException;

public class MinaConnectionException extends MinaException
{
	private static final long serialVersionUID = 1411667074718043325L;

	public MinaConnectionException(String message)
	{
		super(message);
	}
	
	public MinaConnectionException(String message, Throwable t)
	{
		super(message, t);
	}
	
	public MinaConnectionException(Throwable t)
	{
		super(t);
	}
}
