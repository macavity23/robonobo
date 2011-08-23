package com.robonobo.gui.preferences;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.util.Set;

import com.robonobo.common.util.NetUtil;
import com.robonobo.gui.frames.RobonoboFrame;

public class LocalIpPrefPanel extends ChoicePrefPanel {
	boolean userOverrode = false;

	public LocalIpPrefPanel(RobonoboFrame frame) {
		super(frame, "mina.localAddress", "Local IP Address", getLocalAddrs(frame.ctrl.getConfig().getAllowLoopbackAddress()));
		// Add a listener that registers when the user specifies the address themselves - this helps us when we pick
		// gateway config on startup
		combo.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				userOverrode = true;
			}
		});
	}

	@Override
	public void applyChanges() {
		super.applyChanges();
		if (userOverrode)
			setProperty("robo.userSpecifiedLocalAddr", "true");
	}

	private static String[] getLocalAddrs(boolean allowLoopback) {
		Set<InetAddress> localIps = NetUtil.getLocalInetAddresses(allowLoopback);
		String[] ipArr = new String[localIps.size()];
		int i = 0;
		for (InetAddress addr : localIps) {
			ipArr[i++] = addr.getHostAddress();
		}
		return ipArr;
	}
}
