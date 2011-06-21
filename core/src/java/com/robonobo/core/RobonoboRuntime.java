package com.robonobo.core;

import java.io.File;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.BasicConfigurator;

import com.robonobo.common.concurrent.Attempt;
import com.robonobo.common.exceptions.Errot;
import com.robonobo.common.serialization.ConfigBeanSerializer;
import com.robonobo.eon.*;
import com.robonobo.mina.external.MinaConfig;

/**
 * Handles detecting running rbnb instances, and if necessary hand-offs between them
 * 
 * @author macavity
 * 
 */
public class RobonoboRuntime {
	private File homeDir;

	public RobonoboRuntime(File homeDir) {
		this.homeDir = homeDir;
	}

	/**
	 * If another instance is running, tries to handover to them, and returns true, otherwise returns false
	 * 
	 * @throws IOException
	 *             Something went wrong on the filesystem
	 * @throws EONException
	 *             The handover didn't work properly - there is another robonobo instance running, but it's not
	 *             responding
	 * 
	 */
	public boolean handoverIfRunning(String arg) throws IOException, EONException {
		// Look up our port from the mina config (overridden from env if necessary)
		int udpPort = -1;
		if(homeDir.exists()) {
			File cfgDir = new File(homeDir, "config");
			if(cfgDir.exists()) {
				File minaCfgFile = new File(cfgDir, "mina.cfg");
				if(minaCfgFile.exists()) {
					ConfigBeanSerializer cbs = new ConfigBeanSerializer(false);
					MinaConfig cfg = cbs.deserializeConfig(MinaConfig.class, minaCfgFile);
					cbs.overrideCfgFromEnv(cfg, "mina");
					udpPort = cfg.getListenUdpPort();
				}
			}
		}
		if(udpPort > 0) {
			// Let's try and listen on this port - if we get an exception, they're listening
			// A bit fugly to use exceptions to do this, but seems the easiest way...
			try {
				// Use wildcard address
				DatagramSocket sock = new DatagramSocket(udpPort);
				sock.close();
			} catch (SocketException e) {
				handover(udpPort, arg);
				return true;
			}
		}
		return false;
	}

	private void handover(int theirUdpPort, String arg) throws EONException {
		if (arg == null)
			arg = "";
		// Bring up a minimal log4j so eon doesn't shit itself
		BasicConfigurator.configure();
		// Start an eonmanager, and send them our arg via the local request service
		ScheduledThreadPoolExecutor executor = new ScheduledThreadPoolExecutor(1);
		EONManager eonMgr = new EONManager("handover", executor);
		eonMgr.start();
		try {
			final DEONConnection conn = eonMgr.createDEONConnection();
			EonSocketAddress theirSockAddr;
			try {
				theirSockAddr = new EonSocketAddress(theirUdpPort, 1);
			} catch (UnknownHostException e) {
				throw new Errot();
			}
			conn.bind();
			conn.sendTo(theirSockAddr, arg.getBytes());
			// We should get back a string <num>:<msg>, where <num> is 0 on success or non-zero on error
			Attempt attempt = new Attempt(executor, 30000, "handoverAttempt") {
				protected void onTimeout() {
					conn.close();
				}
			};
			attempt.start();
			ByteBuffer retBuf = (ByteBuffer) conn.read()[0];
			attempt.succeeded();
			String retStr = new String(retBuf.array(), retBuf.arrayOffset(), retBuf.remaining());
			Pattern respPat = Pattern.compile("^(\\d+):(.*)$");
			Matcher m = respPat.matcher(retStr);
			if (m.matches()) {
				int status = Integer.parseInt(m.group(1));
				if (status == 0) {
					System.out.println("robonobo: handed over arg '" + arg + "' to local instance on port "
							+ theirUdpPort + ": exiting");
				} else
					System.out.println("robonobo got error handing over arg '"+arg+"': " + m.group(2));
				return;
			} else
				throw new EONException("Received unexpected response to handover: "+retStr);
		} catch (InterruptedException e) {
			throw new EONException("Handover timed out");
		} finally {
			eonMgr.stop();
		}
	}
}
