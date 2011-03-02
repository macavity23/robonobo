package com.robonobo.plugin.mp3;

import java.io.IOException;
import java.nio.channels.ByteChannel;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.robonobo.common.pageio.paginator.Paginator;
import com.robonobo.mina.external.buffer.Page;
import com.robonobo.mina.external.buffer.PageBuffer;
import com.robonobo.mina.external.buffer.PageInfo;

public class Mp3Paginator implements Paginator {
	private static final long PROGRESS_INCREMENT = 1024 * 1024;
	Log log = LogFactory.getLog(getClass());
	boolean stopping = false;
	boolean stopped = false;
	
	public Mp3Paginator() {
	}

//	public void paginate(PullSource source, PageSink sink, PaginationCallback callback) throws SourceException,
//			IOException, PageSinkException {
//		// ffs, having to write your own mp3 parser just so you can do things
//		// properly... *sigh*
//		Mp3Parser p = new Mp3Parser(source.createReadableByteChannel());
//		Frame f = null;
//		int unannouncedBytes = 0;
//		int totalRead = 0;
//		stopped = false;
//		do {
//			f = p.nextFrame();
//			if(f != null) {
//				sink.putPage(f.getFrameBuffer(), f.getTimeOffset());
//				totalRead += f.getFrameLength();
//				unannouncedBytes += f.getFrameLength();
//				if(unannouncedBytes > PROGRESS_INCREMENT) {
//					callback.gotProgress(totalRead);
//					unannouncedBytes = 0;
//				}
//			}
//		} while(f != null && !stopping);
//		stopped = true;
//		// hmmm, that seems suspiciously easy.
//	}
	
	public void paginate(ByteChannel c, PageBuffer pageBuf) throws IOException {
		// ffs, having to write your own mp3 parser just so you can do things
		// properly... *sigh*
		Mp3Parser p = new Mp3Parser(c);
		Frame f = null;
		int unannouncedBytes = 0;
		int totalRead = 0;
		stopped = false;
		int pageNum = 0;
		do {
			f = p.nextFrame();
			if(f != null) {
				PageInfo pi = new PageInfo(pageNum++, totalRead, f.getTimeOffset(), f.getFrameBuffer().limit(), 0);
				Page pg = new Page(pi, f.getFrameBuffer());
				pageBuf.putPage(pg);
				totalRead += f.getFrameLength();
			}
		} while(f != null && !stopping);
		stopped = true;
		// hmmm, that seems suspiciously easy.		
	}

	public void stop() {
		stopping = true;
		while(!stopped) {
			try {
				Thread.sleep(100);
			}
			catch(InterruptedException e) {
				Thread.currentThread().interrupt();
				return;
			}
		}
	}
}
