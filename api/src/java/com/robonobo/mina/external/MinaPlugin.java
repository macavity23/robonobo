package com.robonobo.mina.external;


public interface MinaPlugin
{
	public void start();
	public void stop();
	
	/**
	 * Called by the application/mina when it wants us to start receiving a stream
	 */
	public void startReception(String streamUri);
	
	/**
	 * Returns an array of mime type strings, each representing a media type handled by this plugin
	 */
	public String[] getMediaTypes();

	/**
	 * Returns the buffer time, in seconds
	 */
	public int getBufferTime();
	
	public void putListenData(String streamUri, long curSeqNum, byte[] buffer, int start, int length);
	
	/**
	 * @return the number of bytes read.  This should always be equal to the length parameter.
	 */
	public int getBroadcastData(String streamUri, long curSeqNum, byte[] buffer, int start, int length);
	
	/**
	 * If Mina has not received a particular packet by the time it sends data up to the plugin, the value 
	 * of NullPacketFiller will be used in place of that packet. If this is set to null (which is the 
	 * default), the null packet will simply be skipped over and the previous (non-null) packet will be 
	 * contiguously followed by the next non-null packet. The length of NullPacketFiller must be equal to 
	 * the Mina config option "MISPPacketPayloadSize" (unless it is null).
	 * @return
	 */
	public byte[] getNullPacketFiller();
	
	public MinaControl getMainControl();
	public void setMainControl(MinaControl ctrl);
}
