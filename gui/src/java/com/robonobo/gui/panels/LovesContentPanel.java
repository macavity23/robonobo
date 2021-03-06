package com.robonobo.gui.panels;

import static com.robonobo.gui.GuiUtil.*;
import info.clearthought.layout.TableLayout;

import java.awt.Dimension;
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
	JPanel fbPanel, twitPanel;
	RCheckBox fbCb, twitCb;
	JLabel fbIcon, twitIcon;
	RButton addFbBtn, addTwitBtn;
	RRadioButton groupPostRb;
	RRadioButton immPostRb;

	public LovesContentPanel(RobonoboFrame f, Playlist pl, PlaylistConfig pc) {
		super(f, pl, pc, LovesTableModel.create(f, pl));
		tabPane.insertTab("loves", null, new LovesSettingsPanel(), null, 0);
		commentsPanel = new PlaylistCommentsPanel(f);
		tabPane.insertTab("comments", null, commentsPanel, null, 1);
		tabPane.setSelectedIndex(0);
		setupComments();
		frame.ctrl.addUserListener(new UserAdapter() {
			public void userConfigChanged(UserConfig cfg) {
				userCfgUpdated(cfg);
			}
		});
		UserConfig uc = frame.ctrl.getMyUserConfig();
		if (uc != null)
			userCfgUpdated(uc);
		if (frame.guiCfg.getShowLovesDesc()) {
			runOnUiThread(new CatchingRunnable() {
				public void doRun() throws Exception {
					showMessage("What are my loves?",
							"<html>Your loves are your essential tunes, your all-time favourite tracks of all-time. Add to them by selecting any track and clicking the heart<br> button above. Only your friends can see your loves.</html>",
							"showLovesDesc");
				}
			});
		}
	}

	private void userCfgUpdated(final UserConfig uc) {
		runOnUiThread(new CatchingRunnable() {
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
					fbPanel.add(fbCb);
					fbPanel.add(Box.createHorizontalStrut(5));
					fbPanel.add(fbIcon);
					fbPanel.add(Box.createHorizontalStrut(5));
					fbPanel.add(addFbBtn);
					fbPanel.add(Box.createHorizontalStrut(5));
					fbPanel.add(new RLabel13("to post loves to Facebook"));
				} else {
					fbCb.setEnabled(true);
					String fbCfg = uc.getItem("postLovesToFacebook");
					fbCb.setSelected(fbCfg == null || fbCfg.equalsIgnoreCase("true"));
					fbPanel.removeAll();
					fbPanel.add(fbCb);
					fbPanel.add(Box.createHorizontalStrut(5));
					fbPanel.add(fbIcon);
					fbPanel.add(Box.createHorizontalStrut(5));
					fbPanel.add(new RLabel13B("Facebook "));
					fbPanel.add(new RLabel13("("+uc.getItem("facebookName")+")"));
				}
				fbPanel.revalidate();
				GuiUtil.markAsDirty(fbPanel);
				if (uc.getItem("twitterId") == null) {
					twitCb.setSelected(false);
					twitCb.setEnabled(false);
					if (addTwitBtn == null) {
						addTwitBtn = new RGlassButton("Add Twitter details...");
						addTwitBtn.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								frame.showTwitterSignupSheet("Add Twitter details", "Before you can tweet from robonobo, you need to add your Twitter details.");
							}
						});
					}
					twitPanel.removeAll();
					twitPanel.add(twitCb);
					twitPanel.add(Box.createHorizontalStrut(5));
					twitPanel.add(twitIcon);
					twitPanel.add(Box.createHorizontalStrut(5));
					twitPanel.add(addTwitBtn);
					twitPanel.add(Box.createHorizontalStrut(5));
					twitPanel.add(new RLabel13("to post loves to Twitter"));
				} else {
					twitCb.setEnabled(true);
					String twitCfg = uc.getItem("postLovesToTwitter");
					twitCb.setSelected(twitCfg == null || twitCfg.equalsIgnoreCase("true"));
					twitPanel.removeAll();
					twitPanel.add(twitCb);
					twitPanel.add(Box.createHorizontalStrut(5));
					twitPanel.add(twitIcon);
					twitPanel.add(Box.createHorizontalStrut(5));
					twitPanel.add(new RLabel13B("Twitter "));
					twitPanel.add(new RLabel13("("+uc.getItem("twitterScreenName")+")"));
				}
				twitPanel.revalidate();
				GuiUtil.markAsDirty(twitPanel);
				groupPostRb.setEnabled(true);
				immPostRb.setEnabled(true);
				String plCfgStr = uc.getItem("postLoves");
				if(plCfgStr == null) 
					groupPostRb.setSelected(true);
				else {
					if(plCfgStr.equalsIgnoreCase("immediate"))
						immPostRb.setSelected(true);
					else
						groupPostRb.setSelected(true);
				}
			}
		});
	}

	@Override
	public void playlistChanged(Playlist p) {
		if (p.equals(ptm().getPlaylist()))
			ptm().update(p);
	}
	
	class LovesSettingsPanel extends JPanel {
		public LovesSettingsPanel() {
			double[][] cells = { { 10, TableLayout.FILL, 10, 300, 10 }, { 5, TableLayout.FILL, 5 } };
			setLayout(new TableLayout(cells));
			JPanel servicePnl = new JPanel();
			servicePnl.setLayout(new BoxLayout(servicePnl, BoxLayout.Y_AXIS));
			RLabel16B serviceLbl = new RLabel16B("Post loves to:");
			serviceLbl.setAlignmentX(LEFT_ALIGNMENT);
			servicePnl.add(serviceLbl);
			servicePnl.add(Box.createVerticalStrut(10));
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
			fbIcon = new JLabel(GuiUtil.createImageIcon("/icon/facebook-24x24.png"));
			fbPanel = new JPanel();
			fbPanel.setLayout(new BoxLayout(fbPanel, BoxLayout.X_AXIS));
			fbPanel.add(fbCb);
			fbPanel.add(Box.createHorizontalStrut(5));
			fbPanel.add(fbIcon);
			fbPanel.add(Box.createHorizontalStrut(5));
			fbPanel.add(new RLabel13("Loading details..."));
			fbPanel.setAlignmentX(LEFT_ALIGNMENT);
			fbPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
			servicePnl.add(fbPanel);
			servicePnl.add(Box.createVerticalStrut(5));
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
			twitIcon = new JLabel(GuiUtil.createImageIcon("/icon/twitter-24x24.png"));
			twitPanel = new JPanel();
			twitPanel.setLayout(new BoxLayout(twitPanel, BoxLayout.X_AXIS));
			twitPanel.add(twitCb);
			twitPanel.add(Box.createHorizontalStrut(5));
			twitPanel.add(twitIcon);
			twitPanel.add(Box.createHorizontalStrut(5));
			twitPanel.add(new RLabel13("Loading details..."));
			twitPanel.setAlignmentX(LEFT_ALIGNMENT);
			twitPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
			servicePnl.add(twitPanel);
			add(servicePnl, "1,1");
			JPanel postPrefPnl = new JPanel();
			postPrefPnl.setLayout(new BoxLayout(postPrefPnl, BoxLayout.Y_AXIS));
			postPrefPnl.add(new RLabel16B("When I love one track after another:"));
			postPrefPnl.add(Box.createVerticalStrut(10));
			ActionListener postPrefAl = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					String cfg = "together";
					if(immPostRb.isSelected())
						cfg = "immediate";
					final String fCfg = cfg;
					frame.ctrl.getExecutor().execute(new CatchingRunnable() {
						public void doRun() throws Exception {
							frame.ctrl.saveUserConfigItem("postLoves", fCfg);
						}
					});
				}
			};
			int delayMins = frame.ctrl.getConfig().getPostLovesDelayMins();
			groupPostRb = new RRadioButton("Post them together (waits "+delayMins+" minutes)");
			groupPostRb.setEnabled(false);
			groupPostRb.addActionListener(postPrefAl);
			postPrefPnl.add(groupPostRb);
			postPrefPnl.add(Box.createVerticalStrut(5));
			immPostRb = new RRadioButton("Post each one immediately");
			immPostRb.setEnabled(false);
			immPostRb.addActionListener(postPrefAl);
			postPrefPnl.add(immPostRb);
			ButtonGroup bg = new ButtonGroup();
			bg.add(groupPostRb);
			bg.add(immPostRb);
			add(postPrefPnl, "3,1");
		}
	}
}
