package com.robonobo.eon.test;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.util.concurrent.ScheduledThreadPoolExecutor;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;

import com.robonobo.common.concurrent.CatchingRunnable;
import com.robonobo.common.concurrent.SafetyNet;
import com.robonobo.common.util.ExceptionEvent;
import com.robonobo.common.util.ExceptionListener;
import com.robonobo.eon.EONConnectionEvent;
import com.robonobo.eon.EONException;
import com.robonobo.eon.EONManager;
import com.robonobo.eon.EonSocketAddress;
import com.robonobo.eon.SEONConnection;
import com.robonobo.eon.SEONConnectionListener;

public class SeonVsTcpTester {
	static int SEND_KB = 10 * 1024; // 10MB
	static Logger log;
	String sendRecvMode;
	String lisConMode;
	InetAddress addr;
	int port;
	ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(8);

	public static void main(String[] args) throws Exception {
		PropertyConfigurator.configure("log4j.properties");
		log = Logger.getLogger(SeonVsTcpTester.class);
		SeonVsTcpTester tester = new SeonVsTcpTester(args);
		tester.run();
	}

	public SeonVsTcpTester(String[] args) throws Exception {
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
		System.out.println("Usage: SeonVsTcpTester [listen|connect] [send|receive] <ipaddr> <port>");
		System.exit(1);
	}

	void run() throws Exception {
		if (lisConMode.equals("listen"))
			doListen();
		else
			doConnect();
	}

	void doSend(Socket sock) {
		log.info("Starting send");
		try {
			byte[] buf = new byte[1024];
			for (int i = 0; i < SEND_KB; i++) {
				sock.getOutputStream().write(buf);
			}
		} catch (IOException e) {
			log.error("Caught IOE while sending", e);
		}
		log.info("Send finished");
	}

	void doSend(SEONConnection conn) {
		log.info("Starting send");
		try {
			ByteBuffer buf = ByteBuffer.allocate(1024);
			for (int i = 0; i < SEND_KB; i++) {
				conn.send(buf);
			}
		} catch (EONException e) {
			log.error("Caught IOE while sending", e);
		}
		log.info("Send finished");
	}

	void doRecv(Socket sock) {
		log.info("Starting receive");
		// Just throw all the data away
		try {
			byte[] buf = new byte[1024];
			while (true) {
				int numRead = sock.getInputStream().read(buf);
				if(numRead < 0)
					break;
			}
		} catch (IOException e) {
			log.error("Caught IOE reading from socket");
		}
		log.info("Receive finished");
	}

	void doRecv(SEONConnection conn) {
		log.info("Starting receive");
		// Just throw all the data away
		try {
			ByteBuffer buf = ByteBuffer.allocate(1024);
			while (true) {
				conn.read(buf);
			}
		} catch (EONException e) {
			log.error("Caught EONE reading from socket");
		}
		log.info("Receive finished");
	}

	void doListen() throws Exception {
		log.info("Starting TCP...");
		ServerSocket servSock = new ServerSocket();
		servSock.bind(new InetSocketAddress(addr, port));
		log.info("Listening for incoming TCP connections on " + addr.getHostAddress() + ":" + port);

		log.info("Starting EON...");
		EONManager eonMgr = new EONManager("test-eon", executor, port);
		eonMgr.start();
		SEONConnection listenConn = eonMgr.createSEONConnection();
		listenConn.addListener(new SEONConnectionListener() {
			public void onClose(EONConnectionEvent event) {
			}

			public void onNewSEONConnection(EONConnectionEvent event) {
				final SEONConnection conn = (SEONConnection) event.getConnection();
				log.info("Got incoming SEON conn from " + conn.getRemoteSocketAddress().getAddress().getHostAddress());
				executor.execute(new CatchingRunnable() {
					public void doRun() throws Exception {
						if (sendRecvMode.equals("send"))
							doSend(conn);
						else
							doRecv(conn);
						conn.close();
					}
				});
			}
		});
		listenConn.bind(23);
		log.info("Listening for incoming SEON connections on " + listenConn.getLocalSocketAddress());

		Socket tcpSock = servSock.accept();
		log.info("Got incoming TCP Conn from " + tcpSock.getInetAddress());
		if (sendRecvMode.equals("send"))
			doSend(tcpSock);
		else
			doRecv(tcpSock);
		tcpSock.close();
	}

	void doConnect() throws Exception {
		InetSocketAddress ep = new InetSocketAddress(addr, port);
		log.info("Connecting TCP to " + addr + ":" + port);
		Socket sock = new Socket();
		sock.connect(ep);
		if (sendRecvMode.equals("send"))
			doSend(sock);
		else
			doRecv(sock);
		sock.close();
		log.info("Connecting EON to " + addr + ":" + port);
		EONManager eonMgr = new EONManager("test-eon", executor);
		eonMgr.start();
		SEONConnection conn = eonMgr.createSEONConnection();
		conn.connect(new EonSocketAddress(addr, port, 23));
		if (sendRecvMode.equals("send"))
			doSend(conn);
		else
			doRecv(conn);
		conn.close();
		log.info("All done.");
	}
}
