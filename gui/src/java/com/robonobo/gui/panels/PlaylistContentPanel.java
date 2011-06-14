package com.robonobo.gui.panels;

import static com.robonobo.gui.GuiUtil.*;
import info.clearthought.layout.TableLayout;

import java.awt.Toolkit;
import java.awt.datatransfer.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JPanel;

import com.robonobo.core.api.model.Playlist;
import com.robonobo.core.api.model.PlaylistConfig;
import com.robonobo.gui.components.base.*;
import com.robonobo.gui.frames.RobonoboFrame;
import com.robonobo.gui.model.PlaylistTableModel;

@SuppressWarnings("serial")
public abstract class PlaylistContentPanel extends ContentPanel implements ClipboardOwner {
	protected RobonoboFrame frame;
	protected Playlist p;
	protected PlaylistConfig pc;

	public PlaylistContentPanel(RobonoboFrame frame, Playlist p, PlaylistConfig pc, boolean myPlaylist) {
		super(frame, PlaylistTableModel.create(frame, p, myPlaylist));
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

	protected PlaylistTableModel getModel() {
		return (PlaylistTableModel) getTrackList().getModel();
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
					frame.postToFacebook(p);
				}
			});
			fbBtn.setEnabled(p.getPlaylistId() > 0);
			add(fbBtn, "4,0");
			RButton twitBtn = new RSmallRoundButton(createImageIcon("/icon/twitter.png", null));
			twitBtn.setToolTipText("Post playlist update to twitter");
			twitBtn.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					frame.postToTwitter(p);
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
	
	@Override
	public void lostOwnership(Clipboard clipboard, Transferable contents) {
		// Do nothing
	}
}
