package com.robonobo.plugin.mplayer;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hsqldb.lib.StringUtil;

import net.freeutils.httpserver.*;
import net.freeutils.httpserver.HTTPServer.ContextHandler;
import net.freeutils.httpserver.HTTPServer.Request;
import net.freeutils.httpserver.HTTPServer.Response;

import com.robonobo.common.pageio.buffer.PageBufferInputStream;
import com.robonobo.common.util.ByteUtil;
import com.robonobo.core.api.model.Stream;
import com.robonobo.mina.external.buffer.PageBuffer;

public class MplayerHttpServer {
	static final Pattern BYTE_RANGE_PAT = Pattern.compile("^bytes=(\\d+)-(\\d*)$");

	private HTTPServer server;
	Log log = LogFactory.getLog(getClass());

	public MplayerHttpServer() throws IOException {
		server = new HTTPServer();
	}
	
	public void start() throws IOException {
		server.start();
	}
	
	public void stop() {
		server.stop();
	}
	
	public int getPort() {
		return server.getPort();
	}
	
	public void addStream(Stream s, PageBuffer pb) {
		String sid = s.getStreamId();
		server.getVirtualHost(null).addContext("/"+sid+".mp3", new StreamReqHandler(s, pb));
	}
	
	public void removeStream(Stream s) {
		server.getVirtualHost(null).removeContext("/"+s.getStreamId()+".mp3");
	}
	
	class StreamReqHandler implements ContextHandler {
		Stream s;
		PageBuffer pb;
		
		public StreamReqHandler(Stream s, PageBuffer pb) {
			this.s = s;
			this.pb = pb;
		}

		@Override
		public int serve(Request req, Response resp) throws IOException {
			log.debug("Handling http request from mplayer for stream "+s.getStreamId());
			PageBufferInputStream pbis = new PageBufferInputStream(pb);
			try {
				resp.getHeaders().add("Connection", "close");
				resp.getHeaders().add("Content-Type", "audio/mpeg");
				String rangeStr = req.getHeaders().get("Range");
				long sz = s.getSize();
				if (StringUtil.isEmpty(rangeStr)) {
					// Normal GET
					resp.getHeaders().add("Accept-Ranges", "bytes");
					resp.getHeaders().add("Content-Length", String.valueOf(sz));
					log.debug("Handling normal get");
					resp.sendHeaders(200);
				} else {
					// Partial content request
					Matcher m = BYTE_RANGE_PAT.matcher(rangeStr);
					if (!m.matches()) {
						log.error("Error parsing byte range: " + rangeStr);
						return 500;
					}
					int firstByte = Integer.parseInt(m.group(1));
					if (firstByte >= sz) {
						log.debug("Duff byte range - returning 416");
						return 416; // Range not satisfiable, fuck knows why mplayer asks for this
					}
					String endRange = m.group(2);
					int lastByte = StringUtil.isEmpty(endRange) ? (int) (sz - 1) : Integer.parseInt(endRange);
					int len = lastByte - firstByte + 1;
					resp.getHeaders().add("Accept-Ranges", "bytes");
					resp.getHeaders().add("Content-Length", String.valueOf(len));
					resp.getHeaders().add("Content-Range", "bytes "+firstByte+"-"+lastByte+"/"+sz);
					log.debug("Sending partial content, range "+firstByte+"-"+lastByte);
					resp.sendHeaders(206); // Partial content
					log.debug("Starting skip...");
					pbis.skip(firstByte);
					log.debug("Finished skip");
				}
				streamDump(pbis, resp.getBody());
				log.debug("Request finished");
				return 0;
			} finally {
				pbis.close();
				log.debug("Finished http request for stream "+s.getStreamId());
			}
		}
		
		void streamDump(InputStream is, OutputStream os) throws IOException {
			int logEvery = 50 * 1024;
			byte[] buf = new byte[1024];
			int totalRead = 0;
			int readSinceLog = 0;
			int numRead;
			while((numRead = is.read(buf)) > 0) {
				os.write(buf, 0, numRead);
				totalRead += numRead;
				readSinceLog += numRead;
				if(readSinceLog > logEvery) {
					log.debug("Stream dumped "+totalRead+" bytes");
					readSinceLog = 0;
				}
			}
			is.close();
			os.close();
		}
	}
}
