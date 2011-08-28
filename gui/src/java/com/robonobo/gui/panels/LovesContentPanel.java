package com.robonobo.gui.panels;

import info.clearthought.layout.TableLayout;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import com.robonobo.common.concurrent.CatchingRunnable;
import com.robonobo.core.api.UserAdapter;
import com.robonobo.core.api.model.*;
import com.robonobo.gui.GuiUtil;
import com.robonobo.gui.components.base.*;
import com.robonobo.gui.frames.RobonoboFrame;
import com.robonobo.gui.model.LovesTableModel;

@SuppressWarnings("serial")
public class LovesContentPanel extends MyPlaylistContentPanel {
	RCheckBox fbCb, twitCb;
	JPanel fbPanel, twitPanel;
	RButton addFbBtn, addTwitBtn;
	RRadioButton groupPostRb;
	RRadioButton immPostRb;

	public LovesContentPanel(RobonoboFrame f, Playlist pl, PlaylistConfig pc) {
		super(f, pl, pc, LovesTableModel.create(f, pl));
		tabPane.insertTab("loves", null, new LovesSettingsPanel(), null, 0);
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
				if (uc.getItem("facebookId") == null) {
					fbCb.setSelected(false);
					fbCb.setEnabled(false);
					if (addFbBtn == null) {
						addFbBtn = new RGlassButton("Add Facebook details...");
						addFbBtn.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								frame.showFacebookSignupSheet("Add Facebook details", "Before you can post to your wall from robonobo, you need to add your Facebook details.");
							}
						});
					}
					fbPanel.removeAll();
					fbPanel.add(addFbBtn);
					fbPanel.add(Box.createHorizontalStrut(5));
					fbPanel.add(new RLabel13("to post loves to Facebook"));
				} else {
					fbCb.setEnabled(true);
					String fbCfg = uc.getItem("postLovesToFb");
					fbCb.setSelected(fbCfg == null || fbCfg.equalsIgnoreCase("true"));
					fbPanel.removeAll();
					fbPanel.add(new RLabel13("Facebook (" + uc.getItem("facebookName") + ")"));
				}
				if (uc.getItem("twitId") == null) {
					twitCb.setSelected(false);
					twitCb.setEnabled(false);
					if (addTwitBtn == null) {
						addTwitBtn = new RGlassButton("Add twitter details...");
						addTwitBtn.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								frame.showTwitterSignupSheet("Add Twitter details", "Before you can tweet from robonobo, you need to add your Twitter details.");
							}
						});
					}
					twitPanel.removeAll();
					twitPanel.add(addTwitBtn);
					twitPanel.add(Box.createHorizontalStrut(5));
					twitPanel.add(new RLabel13("to post loves to Twitter"));
				} else {
					twitCb.setEnabled(true);
					String twitCfg = uc.getItem("postLovesToTwitter");
					twitCb.setSelected(twitCfg == null || twitCfg.equalsIgnoreCase("true"));
					twitPanel.removeAll();
					twitPanel.add(new RLabel13("Twitter (" + uc.getItem("twitterScreenName") + ")"));
				}
			}
		});
	}

	class LovesSettingsPanel extends JPanel {
		public LovesSettingsPanel() {
			double[][] cells = { { 10, 20, 5, 20, 200, TableLayout.FILL, 250, 10 }, { 10, 20, 10, 30, 5, 30, 10, 30, TableLayout.FILL } };
			setLayout(new TableLayout(cells));
			add(new RLabel16B("Post loves to:"), "1,1,5,1,LEFT,TOP");
			fbCb = new RCheckBox();
			fbCb.setSelected(false);
			fbCb.setEnabled(false);
			fbCb.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					final boolean sel = fbCb.isSelected();
					frame.ctrl.getExecutor().execute(new CatchingRunnable() {
						public void doRun() throws Exception {
							frame.ctrl.saveUserConfigItem("postLovesToFacebook", Boolean.toString(sel));
						}
					});
				}
			});
			add(fbCb, "1,3");
			add(new JLabel(GuiUtil.createImageIcon("/icon/facebook.png", null)), "3,3,CENTER,CENTER");
			fbPanel = new JPanel();
			fbPanel.setLayout(new BoxLayout(fbPanel, BoxLayout.X_AXIS));
			fbPanel.add(new RLabel13("Loading details..."));
			add(fbPanel, "4,3");
			twitCb = new RCheckBox();
			twitCb.setSelected(false);
			twitCb.setEnabled(false);
			twitCb.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					final boolean sel = twitCb.isSelected();
					frame.ctrl.getExecutor().execute(new CatchingRunnable() {
						public void doRun() throws Exception {
							frame.ctrl.saveUserConfigItem("postLovesToTwitter", Boolean.toString(sel));
						}
					});
				}
			});
			add(twitCb, "1,5");
			add(new JLabel(GuiUtil.createImageIcon("/icon/twitter.png", null)), "3,5,CENTER,CENTER");
			twitPanel = new JPanel();
			twitPanel.setLayout(new BoxLayout(twitPanel, BoxLayout.X_AXIS));
			twitPanel.add(new RLabel13("Loading details..."));
			add(twitPanel, "4,5");
			add(new RLabel16B("When I love one track after another:"), "6,1,LEFT,TOP");
			groupPostRb = new RRadioButton("Post them together (waits 30 minutes)");
			groupPostRb.setEnabled(false);
			add(groupPostRb, "6,3");
			immPostRb = new RRadioButton("Post each immediately");
			immPostRb.setEnabled(false);
			add(immPostRb, "6,5");
			ButtonGroup bg = new ButtonGroup();
			bg.add(groupPostRb);
			bg.add(immPostRb);
			add(new ToolsPanel(), "1,7,6,7,LEFT,TOP");
		}
	}
	
	class ToolsPanel extends PlaylistToolsPanel {
		@Override
		protected String urlText() {
			long myUid = frame.ctrl.getMyUser().getUserId();
			return frame.ctrl.getConfig().getShortUrlBase() + "sp/" + Long.toHexString(myUid) + "/loves";
		}
	}
}
