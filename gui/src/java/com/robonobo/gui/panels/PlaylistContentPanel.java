package com.robonobo.gui.panels;

import static com.robonobo.gui.GuiUtil.*;
import info.clearthought.layout.TableLayout;

import java.awt.Toolkit;
import java.awt.datatransfer.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;

import javax.swing.JPanel;

import com.robonobo.common.concurrent.CatchingRunnable;
import com.robonobo.core.api.model.*;
import com.robonobo.core.metadata.CommentCallback;
import com.robonobo.gui.components.base.*;
import com.robonobo.gui.frames.RobonoboFrame;
import com.robonobo.gui.model.PlaylistTableModel;
import com.robonobo.gui.model.TrackListTableModel;

@SuppressWarnings("serial")
public abstract class PlaylistContentPanel extends ContentPanel implements ClipboardOwner {
	protected Playlist p;
	protected PlaylistConfig pc;
	/** Note, you must initialize this in the subclass constructor */
	protected PlaylistCommentsPanel commentsPanel;
	boolean unreadComments = false;
	protected PlaylistToolsPanel toolsPanel;

	public PlaylistContentPanel(RobonoboFrame frame, Playlist p, PlaylistConfig pc, boolean myPlaylist) {
		super(frame, PlaylistTableModel.create(frame, p, myPlaylist));
		this.p = p;
		this.pc = pc;
	}

	public PlaylistContentPanel(RobonoboFrame frame, Playlist p, PlaylistConfig pc, TrackListTableModel model) {
		super(frame, model);
		this.p = p;
		this.pc = pc;
		this.frame = frame;
	}

	protected PlaylistTableModel ptm() {
		return (PlaylistTableModel) trackList.getModel();
	}

	public class PlaylistToolsPanel extends JPanel {
		private RButton fbBtn;
		private RButton twitBtn;
		private RButton copyBtn;

		public PlaylistToolsPanel() {
			double[][] cellSizen = { { 35, 5, 215, 5, 30, 5, 30, 5, 90 }, { 25 } };
			setLayout(new TableLayout(cellSizen));
			RLabel urlLbl = new RLabel13("URL:");
			add(urlLbl, "0,0");
			final RTextField urlField = new RTextField(urlText());
			urlField.setEnabled(false);
			add(urlField, "2,0");
			fbBtn = new RSmallRoundButton(createImageIcon("/icon/facebook-16x16.png", null));
			fbBtn.setToolTipText("Post playlist update to facebook");
			fbBtn.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					frame.postToFacebook(p);
				}
			});
			fbBtn.setEnabled(p.getPlaylistId() > 0);
			add(fbBtn, "4,0");
			twitBtn = new RSmallRoundButton(createImageIcon("/icon/twitter-16x16.png", null));
			twitBtn.setToolTipText("Post playlist update to twitter");
			twitBtn.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					frame.postToTwitter(p);
				}
			});
			twitBtn.setEnabled(p.getPlaylistId() > 0);
			add(twitBtn, "6,0");
			copyBtn = new RSmallRoundButton("Copy URL");
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
			checkPlaylistVisibility();
		}

		protected String urlText() {
			return (p.getPlaylistId() > 0) ? frame.ctrl.getConfig().getShortUrlBase() + "p/" + Long.toHexString(p.getPlaylistId()) : "(none)";
		}

		public void checkPlaylistVisibility() {
			// If this is a new playlist, disable buttons
			if (p.getPlaylistId() <= 0) {
				fbBtn.setEnabled(false);
				twitBtn.setEnabled(false);
				copyBtn.setEnabled(false);
				return;
			}
			copyBtn.setEnabled(true);
			if (frame.ctrl.getMyUser().getPlaylistIds().contains(p.getPlaylistId())) {
				// It's my playlist, enable everything
				fbBtn.setEnabled(true);
				fbBtn.setToolTipText("Post playlist update to facebook");
				twitBtn.setEnabled(true);
				twitBtn.setToolTipText("Post playlist update to twitter");
			} else {
				// For playlists that aren't mine, only enable the fb/twit buttons if the playlist is public
				if (p.getVisibility().equals(Playlist.VIS_ALL)) {
					fbBtn.setEnabled(true);
					fbBtn.setToolTipText("Post playlist update to facebook");
					twitBtn.setEnabled(true);
					twitBtn.setToolTipText("Post playlist update to twitter");
				} else {
					fbBtn.setEnabled(false);
					fbBtn.setToolTipText("Cannot post: this playlist is not public");
					twitBtn.setEnabled(false);
					twitBtn.setToolTipText("Cannot post: this playlist is not public");
				}
			}
		}
	}

	@Override
	public void lostOwnership(Clipboard clipboard, Transferable contents) {
		// Do nothing
	}

	public void gotPlaylistComments(long plId, boolean anyUnread, Map<Comment, Boolean> comments) {
		if (commentsPanel == null)
			return;
		if (plId != p.getPlaylistId())
			return;
		if (anyUnread && !(tabPane.getSelectedIndex() == 1)) {
			unreadComments = true;
			runOnUiThread(new CatchingRunnable() {
				public void doRun() throws Exception {
					addBangToTab(1);
				}
			});
		}
		List<Comment> cl = new ArrayList<Comment>(comments.keySet());
		Collections.sort(cl);
		commentsPanel.addComments(cl);
	}

	public class PlaylistCommentsPanel extends CommentsTabPanel {
		public PlaylistCommentsPanel(RobonoboFrame frame) {
			super(frame);
		}

		@Override
		protected boolean canRemoveComment(Comment c) {
			long myUid = frame.ctrl.getMyUser().getUserId();
			// If I own this comment, I can remove it
			if (c.getUserId() == myUid)
				return true;
			// If I don't own this playlist, I can't remove comments
			if (!p.getOwnerIds().contains(myUid))
				return false;
			// I do own this playlist - I can remove this comment unless it's made by another owner
			return !(p.getOwnerIds().contains(c.getUserId()));
		}

		@Override
		protected void newComment(long parentCmtId, String text, CommentCallback cb) {
			frame.ctrl.newCommentForPlaylist(p.getPlaylistId(), parentCmtId, text, cb);
		}
	}
}
