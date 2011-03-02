package com.robonobo.mina.util;

public class MinaThread extends Thread
{
	protected boolean stopped;
	
	public MinaThread()
	{
		stopped = false;
	}
	
	public void start()
	{
		stopped = false;
		super.start();
	}
	
	public void terminate()
	{
		stopped = true;
		interrupt();
	}
}
