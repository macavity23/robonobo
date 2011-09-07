package com.robonobo.eon.test;

import static com.robonobo.common.util.TimeUtil.msElapsedSince;
import static com.robonobo.common.util.TimeUtil.now;
import static java.lang.System.*;

import java.io.*;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.util.Date;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.robonobo.common.async.PullDataProvider;
import com.robonobo.common.async.PushDataReceiver;
import com.robonobo.common.concurrent.CatchingRunnable;
import com.robonobo.common.concurrent.SafetyNet;
import com.robonobo.common.exceptions.SeekInnerCalmException;
import com.robonobo.common.util.ByteUtil;
import com.robonobo.common.util.ExceptionEvent;
import com.robonobo.common.util.ExceptionListener;
import com.robonobo.common.util.FileUtil;
import com.robonobo.common.util.Modulo;
import com.robonobo.eon.EONConnectionEvent;
import com.robonobo.eon.EONManager;
import com.robonobo.eon.EonSocketAddress;
import com.robonobo.eon.SEONConnection;
import com.robonobo.eon.SEONConnectionListener;
/**
 * Dumps bytes onto one end and makes sure they come out of the other end in the
 * right order. Also for performance testing
 * 
 * @author macavity
 * 
 */
public class SeonTester {
	static Logger log;
	String sendRecvMode;
	String lisConMode;
	InetAddress addr;
	int port;
	ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(8);
	private EONManager eonMgr;

	public static void main(String[] args) throws Exception {
		PropertyConfigurator.configure("log4j.properties");
		log = Logger.getLogger(SeonTester.class);
		SeonTester tester = new SeonTester(args);
		tester.run();
	}

	public SeonTester(String[] args) throws Exception {
		if (args.length < 4)
			printUsage();
		if (args[0].equalsIgnoreCase("listen"))
			lisConMode = "listen";
		else if (args[0].equalsIgnoreCase("connect"))
			lisConMode = "connect";
		else
			printUsage();
		if (args[1].equalsIgnoreCase("send"))
			sendRecvMode = "send";
		else if (args[1].equalsIgnoreCase("receive"))
			sendRecvMode = "recv";
		else
			printUsage();

		addr = InetAddress.getByName(args[2]);
		port = Integer.parseInt(args[3]);
		SafetyNet.addListener(new ExceptionListener() {
			public void onException(ExceptionEvent e) {
				log.error("SafetyNet caught exception", e.getException());
			}
		});
	}

	void printUsage() {
		System.out.println("Usage: SeonTester [listen|connect] [send|receive] <ipaddr> <port>");
		System.exit(1);
	}

	void run() throws Exception {
		System.err.println("Ready to rock, hit enter to start...");
		BufferedReader in = new BufferedReader(new InputStreamReader(System.in));
		in.readLine();
		
		if (lisConMode.equals("listen"))
			doListen();
		else
			doConnect();
	}

	void doSend(final SEONConnection conn) {
//		eonMgr.setMaxOutboundBps(262144);
		
		System.out.println("Starting send");
		conn.setDataProvider(new PullDataProvider() {
			int lastSentByte = 0;
			Modulo mod = new Modulo(255);
			Date lastLog = new Date();

			public ByteBuffer getMoreData() {
				ByteBuffer buf = ByteBuffer.allocate(16384);
				byte b = (byte) (mod.add(lastSentByte, 1) & 0xff);
				for (int i = 0; i < buf.limit(); i++) {
					buf.put(b);
				}
				lastSentByte = (b & 0xff);
				if(msElapsedSince(lastLog) > 1000L) {
					if(conn.getOutFlowRate() > 0)
						System.out.println("Sending data at "+FileUtil.humanReadableSize(conn.getOutFlowRate())+"/s");
					lastLog = now();
				}
				buf.flip();
				StringBuffer sb = new StringBuffer("Tester sending buffer: ");
				ByteUtil.printBuf(buf, sb);
				log.info(sb);
				return buf;
			}
		});
	}

	void doRecv(final SEONConnection conn) {
		System.out.println("Starting receive");
		conn.setDataReceiver(new PushDataReceiver() {
			byte lastByte = -1;
			int numReceived = 0;
			long lastLog = currentTimeMillis();
			public void receiveData(ByteBuffer data, Object ignore) throws IOException {
				StringBuffer sb = new StringBuffer("Tester receiving buffer: ");
				ByteUtil.printBuf(data, sb);
				log.info(sb);
				while (data.remaining() > 0) {
					byte b = data.get();
					if(lastByte < 0)
						lastByte = b;
					if(b != lastByte)
						throw new SeekInnerCalmException();
					numReceived++;
					if(numReceived == 16384) {
						lastByte = -1;
						numReceived = 0;
					}
				}
				if((currentTimeMillis() - lastLog) > 1000L) {
					if(conn.getInFlowRate() > 0)
						System.out.println("Receiving data at "+FileUtil.humanReadableSize(conn.getInFlowRate())+"/s");
					lastLog = currentTimeMillis();
				}
			}
			public void providerClosed() {
				// Do nothing
			}
		});
	}

	void doListen() throws Exception {
		System.out.println("Starting EON...");
		eonMgr = new EONManager("test-eon", executor, port);
		eonMgr.start();
		SEONConnection listenConn = eonMgr.createSEONConnection();
		listenConn.addListener(new SEONConnectionListener() {
			public void onClose(EONConnectionEvent event) {
			}

			public void onNewSEONConnection(EONConnectionEvent event) {
				final SEONConnection conn = (SEONConnection) event.getConnection();
				System.out.println("Got incoming SEON conn from " + conn.getRemoteSocketAddress().getAddress().getHostAddress());
				executor.execute(new CatchingRunnable() {
					public void doRun() throws Exception {
						if (sendRecvMode.equals("send"))
							doSend(conn);
						else
							doRecv(conn);
					}
				});
			}
		});
		listenConn.bind(23);
		System.out.println("Listening for incoming SEON connections on " + listenConn.getLocalSocketAddress());
		while (true) {
			Thread.sleep(10000L);
		}
	}

	void doConnect() throws Exception {
		System.out.println("Connecting EON to " + addr + ":" + port);
		eonMgr = new EONManager("test-eon", executor);
		eonMgr.start();
		SEONConnection conn = eonMgr.createSEONConnection();
		conn.connect(new EonSocketAddress(addr, port, 23));
		if (sendRecvMode.equals("send"))
			doSend(conn);
		else
			doRecv(conn);
		while (true) {
			Thread.sleep(10000L);
		}
	}
}
