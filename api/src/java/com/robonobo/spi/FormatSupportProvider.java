package com.robonobo.spi;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.ThreadPoolExecutor;

import com.robonobo.core.api.AudioPlayer;
import com.robonobo.core.api.Robonobo;
import com.robonobo.core.api.model.Stream;
import com.robonobo.mina.external.buffer.PageBuffer;


/**
 * SPI interface for providing format support
 * 
 * @author ray
 * 
 */
public interface FormatSupportProvider {
	public void init(Robonobo r);

	public String getMimeType();

	public String getFormatName();

	/**
	 * Case insensitive, so AVI will match files ending in avi. Do not include initial period.
	 */
	public String[] getSupportedFileExtensions();

	public String getDefaultFileExtension();

	public boolean supportsBroadcast();

	public boolean supportsReception();

	public Stream getStreamForFile(File f) throws IOException;
	
	public void paginate(File f, PageBuffer pageBuf) throws IOException;

	/**
	 * The executor is for calling the audioplayer methods, to prevent deadlocks
	 */
	public AudioPlayer getAudioPlayer(Stream stream, PageBuffer pageBuf, ThreadPoolExecutor executor);
}
