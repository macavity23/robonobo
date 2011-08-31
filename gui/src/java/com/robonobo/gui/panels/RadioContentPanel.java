package com.robonobo.gui.panels;

import info.clearthought.layout.TableLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import com.robonobo.common.concurrent.CatchingRunnable;
import com.robonobo.core.api.UserAdapter;
import com.robonobo.core.api.model.*;
import com.robonobo.gui.GuiUtil;
import com.robonobo.gui.components.base.RLabel16B;
import com.robonobo.gui.components.base.RRadioButton;
import com.robonobo.gui.frames.RobonoboFrame;
import com.robonobo.gui.model.RadioTableModel;

@SuppressWarnings("serial")
public class RadioContentPanel extends MyPlaylistContentPanel {
	public RRadioButton autoRb;
	public RRadioButton manualRb;
	public RRadioButton offRb;

	public RadioContentPanel(RobonoboFrame f, Playlist pl, PlaylistConfig pc) {
		super(f, pl, pc, RadioTableModel.create(f, pl, false));
		tabPane.insertTab("my radio station", null, new RadioSettingsPanel(), null, 0);
		commentsPanel = new PlaylistCommentsPanel(f);
		tabPane.insertTab("comments", null, commentsPanel, null, 1);
		tabPane.setSelectedIndex(0);
		setupComments();
		frame.ctrl.addUserListener(new UserAdapter() {
			@Override
			public void userConfigChanged(UserConfig cfg) {
				userCfgUpdated(cfg);
			}
		});
		UserConfig uc = frame.ctrl.getMyUserConfig();
		if (uc != null)
			userCfgUpdated(uc);
	}

	private RadioTableModel rtm() {
		return (RadioTableModel) trackList.getModel();
	}

	private void userCfgUpdated(final UserConfig uc) {
		GuiUtil.runOnUiThread(new CatchingRunnable() {
			public void doRun() throws Exception {
				autoRb.setEnabled(true);
				manualRb.setEnabled(true);
				offRb.setEnabled(true);
				String cfg = uc.getItem("radioPlaylist");
				if (cfg == null || cfg.equalsIgnoreCase("auto"))
					autoRb.setSelected(true);
				else if (cfg.equalsIgnoreCase("manual"))
					manualRb.setSelected(true);
				else if (cfg.equalsIgnoreCase("off"))
					offRb.setSelected(true);
				updateModel(cfg);
			}
		});
	}

	private void settingsUiChanged() {
		String cfg = null;
		if (autoRb.isSelected())
			cfg = "auto";
		else if (manualRb.isSelected())
			cfg = "manual";
		else if (offRb.isSelected())
			cfg = "off";
		if (cfg == null)
			return;
		updateModel(cfg);
		final String fCfg = cfg;
		frame.ctrl.getExecutor().execute(new CatchingRunnable() {
			public void doRun() throws Exception {
				frame.ctrl.saveUserConfigItem("radioPlaylist", fCfg);
			}
		});
	}

	private void updateModel(String cfg) {
		if (cfg == null || cfg.equalsIgnoreCase("auto"))
			rtm().setCanEdit(false);
		else if (cfg.equalsIgnoreCase("manual"))
			rtm().setCanEdit(true);
		else if (cfg.equalsIgnoreCase("off")) {
			rtm().setCanEdit(false);
			if (p.getStreamIds().size() > 0) {
				frame.ctrl.getExecutor().execute(new CatchingRunnable() {
					public void doRun() throws Exception {
						p.getStreamIds().clear();
						frame.ctrl.updatePlaylist(p);
					}
				});
			}
		}
	}

	class RadioSettingsPanel extends JPanel {
		public RadioSettingsPanel() {
			double[][] cells = { { 10, TableLayout.FILL, 10}, { 5, TableLayout.FILL, 5} };
			setLayout(new TableLayout(cells));
			JPanel optsPnl = new JPanel();
			optsPnl.setLayout(new BoxLayout(optsPnl, BoxLayout.Y_AXIS));
			RLabel16B titleLbl = new RLabel16B("Choose radio tracks:");
			titleLbl.setAlignmentX(LEFT_ALIGNMENT);
			optsPnl.add(titleLbl);
			optsPnl.add(Box.createVerticalStrut(10));
			ActionListener al = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					settingsUiChanged();
				}
			};
			autoRb = new RRadioButton("Automatically add the tracks I play in robonobo");
			autoRb.setEnabled(false);
			autoRb.addActionListener(al);
			autoRb.setAlignmentX(LEFT_ALIGNMENT);
			optsPnl.add(autoRb);
			optsPnl.add(Box.createVerticalStrut(5));
			manualRb = new RRadioButton("Manage radio playlist manually");
			manualRb.setEnabled(false);
			manualRb.addActionListener(al);
			manualRb.setAlignmentX(LEFT_ALIGNMENT);
			optsPnl.add(manualRb);
			optsPnl.add(Box.createVerticalStrut(5));
			offRb = new RRadioButton("Turn off radio playlist");
			offRb.setEnabled(false);
			offRb.addActionListener(al);
			offRb.setAlignmentX(LEFT_ALIGNMENT);
			optsPnl.add(offRb);
			ButtonGroup bg = new ButtonGroup();
			bg.add(autoRb);
			bg.add(manualRb);
			bg.add(offRb);
			add(optsPnl, "1,1");
		}
	}
}
