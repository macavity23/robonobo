package com.robonobo.plugin.defaultplugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.util.concurrent.ThreadPoolExecutor;

import com.robonobo.common.exceptions.SeekInnerCalmException;
import com.robonobo.common.pageio.paginator.EqualSizeFilePaginator;
import com.robonobo.core.RobonoboInstance;
import com.robonobo.core.api.AudioPlayer;
import com.robonobo.core.api.Robonobo;
import com.robonobo.core.api.model.Stream;
import com.robonobo.mina.external.buffer.PageBuffer;
import com.robonobo.spi.FormatSupportProvider;


public class DefaultFormatSupportProvider implements FormatSupportProvider {
	public static final int PAGE_SIZE_BYTES = 16384;
	Robonobo robonobo;

	public void init(Robonobo r) {
		robonobo = r;
	}

	public RobonoboInstance getRobonobo() {
		return (RobonoboInstance) robonobo;
	}

	public String getFormatName() {
		return "Data";
	}

	public String getMimeType() {
		return "application/octet-stream";
	}

	public String getDefaultFileExtension() {
		return "";
	}

	public String[] getSupportedFileExtensions() {
		return new String[] { "*" };
	}

	public boolean supportsBroadcast() {
		return true;
	}

	public boolean supportsReception() {
		return true;
	}

	public long getFileStreamDuration(File f) {
		return -1;
	}

	public Stream getStreamForFile(File f) {
		Stream s = new Stream();
		s.setTitle(f.getName());
		s.setMimeType(getMimeType());
		s.setSize(f.length());
		s.setDuration(0);
		return s;
	}
	
	public void paginate(File f, PageBuffer pageBuf) throws IOException {
		FileChannel fc = new FileInputStream(f).getChannel();
		new EqualSizeFilePaginator(32*1024, f.length(), 0).paginate(fc, pageBuf);
	}
	
	public AudioPlayer getAudioPlayer(Stream s, PageBuffer pageBuf, ThreadPoolExecutor executor) {
		throw new SeekInnerCalmException();
	}
}
