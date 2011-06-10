package com.robonobo.common.util;

/*
 * Robonobo Common Utils
 * Copyright (C) 2008 Will Morton (macavity@well.com) & Ray Hilton (ray@wirestorm.net)

 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.

 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 */
import static com.robonobo.common.util.CodeUtil.*;

import java.util.*;
import java.awt.Desktop;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.*;

import com.twmacinta.util.MD5;

public class NetUtil {
	public static Set<InetAddress> getLocalInetAddresses(boolean includeLoopback) {
		Set<InetAddress> retSet = new HashSet<InetAddress>();
		Enumeration<NetworkInterface> interfaces;
		try {
			interfaces = NetworkInterface.getNetworkInterfaces();
		} catch (SocketException e) {
			return retSet;
		}
		while (interfaces.hasMoreElements()) {
			NetworkInterface thisInterface = interfaces.nextElement();
			Enumeration<InetAddress> addrs = thisInterface.getInetAddresses();
			while (addrs.hasMoreElements()) {
				InetAddress addr = addrs.nextElement();
				if (!includeLoopback && addr.isLoopbackAddress())
					continue;
				retSet.add(addr);
			}
		}
		return retSet;
	}

	public static Inet4Address getFirstPublicInet4Address() {
		return getFirstPublicInet4Address(new PublicNetworkDefinition());
	}

	public static Inet4Address getFirstPublicInet4Address(PublicNetworkDefinition def) {
		Set<InetAddress> localAddrs = getLocalInetAddresses(true);
		Iterator<InetAddress> rator = localAddrs.iterator();
		while (rator.hasNext()) {
			InetAddress addr = (InetAddress) rator.next();
			if (addr.isLoopbackAddress())
				continue;
			if (addr instanceof Inet4Address && def.addrIsPublic(addr))
				return (Inet4Address) addr;
		}
		return null;
	}

	public static Inet4Address getFirstNonLoopbackInet4Address() {
		Set<InetAddress> localAddrs = getLocalInetAddresses(true);
		Iterator<InetAddress> rator = localAddrs.iterator();
		while (rator.hasNext()) {
			InetAddress thisAddr = (InetAddress) rator.next();
			if (thisAddr instanceof Inet4Address && (!thisAddr.isLoopbackAddress()))
				return (Inet4Address) thisAddr;
		}
		return null;
	}

	/**
	 * An adaptation of Dem Pilafian's (public domain) code from http://www.centerkey.com/java/browser/
	 */
	@SuppressWarnings({ "rawtypes", "unchecked" })
	public static void browse(String url) throws IOException {
		try {
			if (CodeUtil.javaMajorVersion() >= 6) {
				// Use the library class, only in j6+
				Desktop.getDesktop().browse(new URI(url));
			} else {
				String osName = System.getProperty("os.name");
				// Mac OS has special Java class
				if (osName.startsWith("Mac OS")) {
					Class fileMgr = Class.forName("com.apple.eio.FileManager");
					Method openURL = fileMgr.getDeclaredMethod("openURL", new Class[] { String.class });
					openURL.invoke(null, new Object[] { url });
					return;
				}
				String[] cmd = null;
				// Windows execs url.dll
				if (osName.startsWith("Windows")) {
					cmd = new String[] { "rundll32", "url.dll,FileProtocolHandler", url };
					// else assume unix/linux: call one of the available browsers
				} else {
					String[] browsers = {
							// Freedesktop, http://portland.freedesktop.org/xdg-utils-1.0/xdg-open.html
							"xdg-open",
							// Debian
							"sensible-browser",
							// Otherwise call browsers directly
							"firefox", "opera", "konqueror", "epiphany", "mozilla", "netscape" };
					String browser = null;
					for (int count = 0; count < browsers.length && browser == null; count++) {
						if (Runtime.getRuntime().exec(new String[] { "which", browsers[count] }).waitFor() == 0)
							browser = browsers[count];
					}
					if (browser == null) {
						// logger.warning("No web browser found");
						throw new Exception("Could not find web browser");
					}
					cmd = new String[] { browser, url };
				}
				if (Runtime.getRuntime().exec(cmd).waitFor() != 0)
					throw new Exception("Error opening page: " + url);
			}
		} catch (Exception e) {
			if (javaMajorVersion() >= 6)
				throw new IOException("Caught ioe browsing to url " + url, e);
			throw new IOException("Caught " + shortClassName(e.getClass()) + " browsing to url " + url);
		}
	}
}
