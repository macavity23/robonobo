package com.robonobo.common.pageio.buffer;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.util.List;

import com.robonobo.mina.external.buffer.PageBufferListener;
import com.robonobo.mina.external.buffer.PageInfo;

@SuppressWarnings("serial")
public class FilePageBuffer extends AbstractPageBuffer {
	private File file;
	private transient FileChannel channel;
	private boolean sleeping = true;
	/**
	 * If we are closed but have listeners, don't actually close until the
	 * listeners are removed
	 */
	private boolean waitingOnClose = false;

	public FilePageBuffer(String streamId, File file, PageInfoStore pis) {
		super(streamId, pis);
		this.file = file;
	}

	/** For when we've copied/moved the data file to a new location, but we want to reuse all our page info */
	public FilePageBuffer(FilePageBuffer oldBuffer, File newFile) {
		super(oldBuffer.streamId, oldBuffer.pageInfoStore);
		this.file = newFile;
	}
	
	@Override
	protected synchronized ByteBuffer getPageData(PageInfo pi) throws IOException {
		if(sleeping)
			wakeup();
		return channel.map(MapMode.READ_ONLY, pi.getByteOffset(), pi.getLength());
	}

	@Override
	protected synchronized void putPageData(PageInfo pi, ByteBuffer pageData) throws IOException {
		if(sleeping)
			wakeup();
		pageData.position(0);
		channel.write(pageData, pi.getByteOffset());
	}

	public synchronized void close() {
		if (listeners.size() > 0) {
			waitingOnClose = true;
			return;
		}
		try {
			sleep();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	public void flush() throws IOException {
		sleep();
	}

	public synchronized void sleep() throws IOException {
		if (channel != null) {
			channel.force(false);
			channel.close();
		}
		channel = null;
		sleeping = true;
	}

	@Override
	public synchronized void removeListener(PageBufferListener listener) {
		super.removeListener(listener);
		if (waitingOnClose)
			close();
	}

	public void putAllPageInfo(List<PageInfo> pageInfos) throws IOException {
		pageInfoStore.putAllPageInfo(streamId, pageInfos);
	}
	
	private void wakeup() {
		try {
			channel = new RandomAccessFile(file, "rw").getChannel();
			sleeping = false;
		} catch (FileNotFoundException e) {
			throw new RuntimeException(e);
		}
	}
}
