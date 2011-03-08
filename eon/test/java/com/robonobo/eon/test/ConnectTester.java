package com.robonobo.eon.test;

import static java.lang.System.*;

import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.robonobo.eon.*;

/**
 * Just connects to one or more listeners. For testing NATs.
 * @author macavity
 *
 */
public class ConnectTester {
	private static final int EON_PORT = 23;

	public static void printUsageAndExit() {
		err.println("Usage: ConnectTester <listen|connect> <ip>:<udp port> [<ip>:<udp port>}...");
		System.exit(1);
	}
	
	public static void main(String[] args) throws Exception {
		if(args.length < 2)
			printUsageAndExit();
		String action = args[0].toLowerCase();
		List<InetSocketAddress> sockAddrs = new ArrayList<InetSocketAddress>();
		Pattern p = Pattern.compile("^(\\d+\\.\\d+\\.\\d+\\.\\d+):(\\d+)$");
		for(int i=1;i<args.length;i++) {
			Matcher m = p.matcher(args[i]);
			if(!m.matches())
				printUsageAndExit();
			InetAddress addr = InetAddress.getByName(m.group(1));
			int port = Integer.parseInt(m.group(2));
			sockAddrs.add(new InetSocketAddress(addr, port));
		}
		if(action.equals("connect"))
			doConnect(sockAddrs);
		else if(action.equals("listen"))
			doListen(sockAddrs.get(0));
		else
			printUsageAndExit();
	}

	public static void doConnect(List<InetSocketAddress> sockAddrs) throws Exception {
		EONManager em = new EONManager("flarp", new ScheduledThreadPoolExecutor(2));
		em.start();
		out.println("Starting EON on "+em.getLocalSocketAddress());
		for (InetSocketAddress sockAddr : sockAddrs) {
			SEONConnection conn = em.createSEONConnection();
			out.print("Connecting to "+sockAddr+"...");
			out.flush();
			EonSocketAddress esa = new EonSocketAddress(sockAddr, EON_PORT);
			conn.connect(esa);
			while(conn.getState() != SEONConnection.State.Established) {
				Thread.sleep(100L);
			}
			out.println("done");
		}
	}
	
	public static void doListen(InetSocketAddress sockAddr) throws Exception {
		EONManager em = new EONManager("flarp", new ScheduledThreadPoolExecutor(2), sockAddr);
		em.start();
		out.println("Listening on "+sockAddr.toString());
		SEONConnection listenConn = em.createSEONConnection();
		listenConn.addListener(new SEONConnectionListener() {
			public void onClose(EONConnectionEvent event) {
			}
			
			public void onNewSEONConnection(EONConnectionEvent event) {
				System.out.println("New incoming conn from "+event.getConnection().getRemoteSocketAddress().toString());
			}
		});
		listenConn.bind(EON_PORT);
	}
}
