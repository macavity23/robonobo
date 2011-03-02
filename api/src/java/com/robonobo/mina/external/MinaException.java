package com.robonobo.mina.external;

public class MinaException extends Exception
{
	private static final long serialVersionUID = 3601927684516253826L;

	public MinaException()
	{
		super();
	}
	
	public MinaException(String message)
	{
		super(message);
	}
	
	public MinaException(String message, Throwable t)
	{
		super(message, t);
	}

	public MinaException(Throwable t)
	{
		super(t);
	}
}
