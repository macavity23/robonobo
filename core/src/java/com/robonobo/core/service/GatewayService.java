package com.robonobo.core.service;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.util.Set;

import net.sbbi.upnp.impls.InternetGatewayDevice;
import net.sbbi.upnp.messages.UPNPResponseException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.robonobo.common.exceptions.SeekInnerCalmException;
import com.robonobo.common.util.NetUtil;
import com.robonobo.mina.external.MinaConfig;

public class GatewayService extends AbstractService {
	private InetSocketAddress publicDetails;
	private InternetGatewayDevice gateway;
	Log log = LogFactory.getLog(getClass());

	public GatewayService() {
		super();
	}

	public String getName() {
		return "Gateway Service";
	}

	public void startup() throws Exception {
		InetAddress localAddr = chooseLocalIP();
		String cfgMode = getRobonobo().getConfig().getGatewayCfgMode();
		if (cfgMode.equals("off")) {
			MinaConfig minaCfg = (MinaConfig) getRobonobo().getConfig("mina");
			minaCfg.setGatewayAddress(null);
		} else if (cfgMode.equals("auto")) {
			if (localAddr.isSiteLocalAddress())
				configureUPnP();
			else
				log.info("Local IP address is public, assuming gateway not present");
		} else
			configurePortForwarding(Integer.parseInt(cfgMode));
		getRobonobo().saveConfig();
	}

	private InetAddress chooseLocalIP() {
		// Check that the local-address is one of the addresses on this host
		MinaConfig minaCfg = (MinaConfig) getRobonobo().getConfig("mina");
		InetAddress myAddr = null;
		String configAddrStr = minaCfg.getLocalAddress();
		Set<InetAddress> localAddrs = NetUtil.getLocalInetAddresses(false);
		if (configAddrStr != null) {
			try {
				InetAddress configAddr = InetAddress.getByName(configAddrStr);
				if (!configAddr.isLoopbackAddress() && localAddrs.contains(configAddr))
					myAddr = configAddr;
				else
					log.info("Local address in config (" + configAddrStr + ") not valid for this host, picking new one");
			} catch (UnknownHostException ignore) {
			}
		}
		if (myAddr == null) {
			// Select an address from our local ones
			myAddr = NetUtil.getFirstPublicInet4Address();
			// TODO IPv6
			if (myAddr == null)
				myAddr = NetUtil.getFirstNonLoopbackInet4Address();
			if (myAddr == null) {
				try {
					myAddr = InetAddress.getByName("127.0.0.1");
				} catch (UnknownHostException e) {
					throw new SeekInnerCalmException();
				}
			}
		}
		minaCfg.setLocalAddress(myAddr.getHostAddress());
		log.info("Configured local address: " + myAddr.toString());
		return myAddr;
	}

	private void configureUPnP() throws UPNPResponseException {
		MinaConfig minaCfg = (MinaConfig) getRobonobo().getConfig("mina");
		minaCfg.setGatewayAddress(null);
		try {
			InternetGatewayDevice[] gatewayDevices = InternetGatewayDevice.getDevices(getRobonobo().getConfig().getUpnpTimeout());
			if (gatewayDevices != null && gatewayDevices.length > 0) {
				gateway = gatewayDevices[0];
				int numTried = 0;
				while (numTried < getRobonobo().getConfig().getUpnpPortsToTry()) {
					boolean success = false;
					int port = getRobonobo().getConfig().getUpnpDefaultPort() + numTried;
					try {
						success = gateway.addPortMapping("Robonobo", // Mapping
								// name
								null, // External address - null for wildcard
								minaCfg.getListenUdpPort(), // Internal port
								port, // External port
								minaCfg.getLocalAddress(), // Internal address
								0, // Duration - 0 for infinite
								"UDP" // Protocol
						);
					} catch (UPNPResponseException ignore) {
					}
					if (success) {
						publicDetails = new InetSocketAddress(InetAddress.getByName(gateway.getExternalIPAddress()), port);
						minaCfg.setGatewayAddress(publicDetails.getAddress().getHostAddress());
						minaCfg.setGatewayUdpPort(port);
						log.info("Successfully configured UPnP, using external details " + publicDetails);
						return;
					}
					// Another service has got this port, try the next one
					numTried++;
				}
			}
		} catch (IOException ignore) {
		} catch(Exception e) {
			log.error("Caught "+e.getClass().getName()+" while creating port mapping: "+e.getMessage());
		}
		log.info("Failed to create UPnP port mapping");
	}

	private void configurePortForwarding(int publicPort) {
		// Manually-specified port - figure out what my public IP address is by
		// pinging sonar
		MinaConfig minaCfg = (MinaConfig) getRobonobo().getConfig("mina");
		String ipDetectUrl = getRobonobo().getConfig().getSonarServerUrl() + "ipdetect";
		GetMethod get = new GetMethod(ipDetectUrl);
		HttpClient client = new HttpClient();
		try {
			int status = client.executeMethod(get);
			if (status != HttpStatus.SC_OK) {
				log.error("IP detect @ " + ipDetectUrl + " returned status " + status + " - port forwarding config failed");
				minaCfg.setGatewayAddress(null);
				return;
			}
			String myPublicIP = get.getResponseBodyAsString();
			try {
				InetAddress.getByName(myPublicIP);
			} catch (Exception e) {
				log.error("IP detect @ " + ipDetectUrl + " returned unparsable body '" + myPublicIP + "' - port forwarding config failed");
				minaCfg.setGatewayAddress(null);
				return;
			}
			log.info("IP detect @ " + ipDetectUrl + " returned public IP address " + myPublicIP);
			minaCfg.setGatewayAddress(myPublicIP);
			minaCfg.setGatewayUdpPort(publicPort);
		} catch (Exception e) {
			log.error("Caught exception when querying sonar for port forwarding", e);
			minaCfg.setGatewayAddress(null);
			return;
		} finally {
			get.releaseConnection();
		}
	}

	public void shutdown() {
		if (gateway != null && publicDetails != null) {
			try {
				gateway.deletePortMapping(null, publicDetails.getPort(), "UDP");
			} catch (IOException ignore) {
			} catch (UPNPResponseException ignore) {
			}
		}
	}

	public String getProvides() {
		return "core.gateway";
	}

	public InetSocketAddress getPublicDetails() {
		return publicDetails;
	}
}
