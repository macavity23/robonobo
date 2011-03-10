package com.robonobo.gui.panels;

import static com.robonobo.gui.GUIUtils.*;
import info.clearthought.layout.TableLayout;

import java.awt.Toolkit;
import java.awt.datatransfer.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

import com.robonobo.common.concurrent.CatchingRunnable;
import com.robonobo.common.util.NetUtil;
import com.robonobo.core.api.model.*;
import com.robonobo.gui.GUIUtils;
import com.robonobo.gui.components.base.*;
import com.robonobo.gui.frames.RobonoboFrame;
import com.robonobo.gui.model.PlaylistTableModel;
import com.robonobo.gui.sheets.PostToFacebookSheet;
import com.robonobo.gui.sheets.PostToTwitterSheet;

public abstract class PlaylistContentPanel extends ContentPanel implements ClipboardOwner {
	protected RobonoboFrame frame;
	protected Playlist p;
	protected PlaylistConfig pc;

	public PlaylistContentPanel(RobonoboFrame frame, Playlist p, PlaylistConfig pc, boolean myPlaylist) {
		super(frame, new PlaylistTableModel(frame.getController(), p, myPlaylist));
		this.p = p;
		this.pc = pc;
		this.frame = frame;
	}

	public PlaylistContentPanel(RobonoboFrame frame, Playlist p, PlaylistConfig pc, PlaylistTableModel model) {
		super(frame, model);
		this.p = p;
		this.pc = pc;
		this.frame = frame;
	}

	class PlaylistToolsPanel extends JPanel {
		public PlaylistToolsPanel() {
			double[][] cellSizen = { {35, 5, 215, 5, 30, 5, 30, 5, 90}, { 25 } };
			setLayout(new TableLayout(cellSizen));
			
			RLabel urlLbl = new RLabel13("URL:");
			add(urlLbl, "0,0");
			String urlBase = frame.getController().getConfig().getPlaylistUrlBase();
			String urlText = (p.getPlaylistId() > 0) ? urlBase + Long.toHexString(p.getPlaylistId()) : "(none)";
			final RTextField urlField = new RTextField(urlText);
			urlField.setEnabled(false);
			add(urlField, "2,0");
			RButton fbBtn = new RSmallRoundButton(createImageIcon("/icon/facebook.png", null));
			fbBtn.setToolTipText("Post playlist update to facebook");
			fbBtn.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					fbUpdateBtnPressed();
				}
			});
			fbBtn.setEnabled(p.getPlaylistId() > 0);
			add(fbBtn, "4,0");
			RButton twitBtn = new RSmallRoundButton(createImageIcon("/icon/twitter.png", null));
			twitBtn.setToolTipText("Post playlist update to twitter");
			twitBtn.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					twitUpdateBtnPressed();
				}
			});
			twitBtn.setEnabled(p.getPlaylistId() > 0);
			add(twitBtn, "6,0");
			RButton copyBtn = new RSmallRoundButton("Copy URL");
			copyBtn.setToolTipText("Copy playlist URL to clipboard");
			copyBtn.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					Clipboard c = Toolkit.getDefaultToolkit().getSystemClipboard();
					StringSelection s = new StringSelection(urlField.getText());
					c.setContents(s, PlaylistContentPanel.this);
				}
			});
			copyBtn.setEnabled(p.getPlaylistId() > 0);
			add(copyBtn, "8,0");
		}
	}
	
	private void fbUpdateBtnPressed() {
		UserConfig uc = frame.getController().getMyUserConfig();
		if (uc == null || uc.getItem("facebookId") == null) {
			// We don't seem to be registered for facebook - fetch a fresh copy of the usercfg from midas in
			// case they've recently added themselves to fb, but GTFOTUT
			frame.getController().getExecutor().execute(new CatchingRunnable() {
				public void doRun() throws Exception {
					UserConfig freshUc = frame.getController().refreshMyUserConfig();
					if (freshUc == null || freshUc.getItem("facebookId") == null) {
						// They haven't associated their facebook account with their rbnb one... bounce them to
						// their account page so they can do so
						NetUtil.browse(frame.getController().getConfig().getWebsiteUrlBase()+"before-facebook-attach");
					} else {
						SwingUtilities.invokeLater(new CatchingRunnable() {
							public void doRun() throws Exception {
								frame.showSheet(new PostToFacebookSheet(frame, p));
							}
						});
					}
				}
			});
		} else {
			frame.showSheet(new PostToFacebookSheet(frame, p));
		}
	}
	
	private void twitUpdateBtnPressed() {
		UserConfig uc = frame.getController().getMyUserConfig();
		if (uc == null || uc.getItem("twitterId") == null) {
			// We don't seem to be registered for twitter - fetch a fresh copy of the usercfg from midas in
			// case they've recently added themselves, but GTFOTUT
			frame.getController().getExecutor().execute(new CatchingRunnable() {
				public void doRun() throws Exception {
					UserConfig freshUc = frame.getController().refreshMyUserConfig();
					if (freshUc == null || freshUc.getItem("twitterId") == null) {
						// They haven't associated their twitter account with their rbnb one... bounce them to
						// their account page so they can do so
						NetUtil.browse(frame.getController().getConfig().getWebsiteUrlBase()+"account");
					} else {
						SwingUtilities.invokeLater(new CatchingRunnable() {
							public void doRun() throws Exception {
								frame.showSheet(new PostToTwitterSheet(frame, p));
							}
						});
					}
				}
			});
		} else {
			frame.showSheet(new PostToTwitterSheet(frame, p));
		}			
	}

	@Override
	public void lostOwnership(Clipboard clipboard, Transferable contents) {
		// Do nothing
	}
}
