package com.robonobo.gui.sheets;

import static com.robonobo.gui.RoboColor.*;
import info.clearthought.layout.TableLayout;

import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.util.*;
import java.util.List;

import javax.swing.*;

import com.robonobo.common.util.NetUtil;
import com.robonobo.gui.RoboFont;
import com.robonobo.gui.components.base.*;
import com.robonobo.gui.frames.RobonoboFrame;
import com.robonobo.gui.preferences.*;

@SuppressWarnings("serial")
public class PreferencesSheet extends Sheet {
	Dimension sz = new Dimension(500, 400);
	List<PrefPanel> prefPanels = new ArrayList<PrefPanel>();
	private RButton saveBtn;
	private RButton cancelBtn;
	
	public PreferencesSheet(RobonoboFrame f) {
		super(f);
		setSize(sz);
		setPreferredSize(sz);
		double[][] cellSizen = { { 5, TableLayout.FILL, 5 }, { 5, TableLayout.FILL, 5, 30, 5 } };
		setLayout(new TableLayout(cellSizen));
		setBackground(LIGHT_GRAY);
		setOpaque(true);
		JTabbedPane tabPane = new JTabbedPane();
		tabPane.setFont(RoboFont.getFont(12, true));
		tabPane.setBackground(MID_GRAY);
		tabPane.addTab("Basic", new JScrollPane(createBasicPanel()));
		tabPane.addTab("Advanced", new JScrollPane(createAdvancedPanel()));
		tabPane.setSelectedIndex(0);
		add(tabPane, "1,1");
		add(createButtonPanel(), "1,3");
	}

	
	@Override
	public void onShow() {
		saveBtn.requestFocusInWindow();
	}

	@Override
	public JButton defaultButton() {
		return saveBtn;
	}

	private JPanel createButtonPanel() {
		JPanel p = new JPanel();
		p.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
		p.setLayout(new BoxLayout(p, BoxLayout.LINE_AXIS));
		cancelBtn = new RRedGlassButton("CANCEL");
		cancelBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doCancel();
			}
		});
		p.add(cancelBtn);
		p.add(Box.createHorizontalStrut(10));
		saveBtn = new RGlassButton("SAVE");
		saveBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				doSave();
			}
		});
		p.add(saveBtn);
		return p;
	}
	
	private JPanel createBasicPanel() {
		JPanel bp = new JPanel();
		bp.setLayout(new BoxLayout(bp, BoxLayout.Y_AXIS));
		bp.setName("playback.background.panel");
		FilePrefPanel dfPanel = new FilePrefPanel(frame, "robo.finishedDownloadsDirectory", "Downloads Folder", true);
		prefPanels.add(dfPanel);
		bp.add(dfPanel);
		bp.add(vertSpacer());

		IntPrefPanel mrdPanel = new IntPrefPanel(frame, "robo.maxRunningDownloads", "Max Simultaneous Downloads", false);
		prefPanels.add(mrdPanel);
		bp.add(mrdPanel);
		bp.add(vertSpacer());

		Set<InetAddress> localIps = NetUtil.getLocalInetAddresses(frame.ctrl.getConfig().getAllowLoopbackAddress());
		String[] ipArr = new String[localIps.size()];
		int i = 0;
		for (InetAddress addr : localIps) {
			ipArr[i++] = addr.getHostAddress();
		}
		ChoicePrefPanel lipPanel = new ChoicePrefPanel(frame, "mina.localAddress", "Local IP Address", ipArr);
		prefPanels.add(lipPanel);
		bp.add(lipPanel);
		bp.add(vertSpacer());

		GatewayPrefPanel gPanel = new GatewayPrefPanel(frame);
		prefPanels.add(gPanel);
		bp.add(gPanel);
		bp.add(Box.createVerticalGlue());

		return bp;
	}

	private JPanel createAdvancedPanel() {
		JPanel ap = new JPanel();
		ap.setLayout(new BoxLayout(ap, BoxLayout.Y_AXIS));
		ap.setName("playback.background.panel");
		StringPrefPanel suPanel = new StringPrefPanel(frame, "robo.sonarServerUrl", "Node Locator URL");
		prefPanels.add(suPanel);
		ap.add(suPanel);
		ap.add(vertSpacer());

		StringPrefPanel muPanel = new StringPrefPanel(frame, "robo.metadataServerUrl", "Metadata Server URL");
		prefPanels.add(muPanel);
		ap.add(muPanel);
		ap.add(vertSpacer());

		StringPrefPanel buPanel = new StringPrefPanel(frame, "wang.bankUrl", "Bank URL");
		prefPanels.add(buPanel);
		ap.add(buPanel);
		ap.add(vertSpacer());

		BoolPrefPanel llPanel = new BoolPrefPanel(frame, "mina.locateLocalNodes", "Locate Local Nodes");
		prefPanels.add(llPanel);
		ap.add(llPanel);
		ap.add(vertSpacer());

		BoolPrefPanel lrPanel = new BoolPrefPanel(frame, "mina.locateRemoteNodes", "Locate Remote Nodes");
		prefPanels.add(lrPanel);
		ap.add(lrPanel);
		ap.add(vertSpacer());

		ap.add(Box.createVerticalGlue());
		
		return ap;
	}

	private Component vertSpacer() {
		return Box.createRigidArea(new Dimension(0, 5));
	}

	private void doCancel() {
		for (PrefPanel pp : prefPanels) {
			pp.resetValue();
		}
		setVisible(false);
	}

	private void doSave() {
		List<PrefPanel> changedPrefs = new ArrayList<PrefPanel>();
		for (PrefPanel pp : prefPanels) {
			if (pp.hasChanged())
				changedPrefs.add(pp);
		}
		if (changedPrefs.size() == 0)
			setVisible(false);
		else {
			int retVal = JOptionPane.showConfirmDialog(this, "Changing preferences requires robonobo to be restarted.  Are you ready to restart now?", "Restart robonobo?", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
			if(retVal == JOptionPane.YES_OPTION) {
				for (PrefPanel pp : changedPrefs) {
					pp.applyChanges();
				}
				frame.ctrl.saveConfig();
				frame.restart();
			}
		}
	}
}
