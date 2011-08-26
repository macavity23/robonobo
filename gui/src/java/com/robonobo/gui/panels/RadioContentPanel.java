package com.robonobo.gui.panels;

import info.clearthought.layout.TableLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ButtonGroup;
import javax.swing.JPanel;

import com.robonobo.common.concurrent.CatchingRunnable;
import com.robonobo.core.api.UserAdapter;
import com.robonobo.core.api.model.*;
import com.robonobo.gui.GuiUtil;
import com.robonobo.gui.components.base.RLabel16B;
import com.robonobo.gui.components.base.RRadioButton;
import com.robonobo.gui.frames.RobonoboFrame;
import com.robonobo.gui.model.PlaylistTableModel;

@SuppressWarnings("serial")
public class RadioContentPanel extends MyPlaylistContentPanel {
	public RRadioButton autoRb;
	public RRadioButton manualRb;
	public RRadioButton offRb;

	public RadioContentPanel(RobonoboFrame f, Playlist pl, PlaylistConfig pc) {
		super(f, pl, pc, PlaylistTableModel.create(f, pl, false));
		tabPane.insertTab("radio", null, new RadioSettingsPanel(), null, 0);
		commentsPanel = new PlaylistCommentsPanel(f);
		tabPane.insertTab("comments", null, commentsPanel, null, UNDEFINED_CONDITION);
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
			}
		});
	}

	private void settingsChanged() {
		String cfg = null;
		if (autoRb.isSelected())
			cfg = "auto";
		else if (manualRb.isSelected())
			cfg = "manual";
		else if (offRb.isSelected())
			cfg = "off";
		if (cfg == null)
			return;
		final String flarp = cfg;
		frame.ctrl.getExecutor().execute(new CatchingRunnable() {
			public void doRun() throws Exception {
				frame.ctrl.saveUserConfigItem("radioPlaylist", flarp);
			}
		});
	}

	class RadioSettingsPanel extends JPanel {
		public RadioSettingsPanel() {
			double[][] cells = { { 10, TableLayout.FILL, 10 }, { 10, 20, 10, 30, 5, 30, 10, 30, 10, 30, TableLayout.FILL } };
			setLayout(new TableLayout(cells));
			add(new RLabel16B("Radio playlist"), "1,1");
			ActionListener al = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					settingsChanged();
				}
			};
			autoRb = new RRadioButton("Add the tracks I play in robonobo");
			autoRb.setEnabled(false);
			autoRb.addActionListener(al);
			add(autoRb, "1,3");
			manualRb = new RRadioButton("Manage radio playlist manually");
			manualRb.setEnabled(false);
			manualRb.addActionListener(al);
			add(manualRb, "1,5");
			offRb = new RRadioButton("Turn off radio playlist");
			offRb.setEnabled(false);
			offRb.addActionListener(al);
			add(offRb, "1,7");
			ButtonGroup bg = new ButtonGroup();
			bg.add(autoRb);
			bg.add(manualRb);
			bg.add(offRb);
			add(new PlaylistToolsPanel(), "1,9");
		}
	}
}
