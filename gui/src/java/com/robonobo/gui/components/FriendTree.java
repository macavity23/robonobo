package com.robonobo.gui.components;

import static com.robonobo.gui.GuiUtil.*;
import static com.robonobo.gui.RoboColor.*;

import java.awt.*;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;

import com.robonobo.gui.GuiUtil;
import com.robonobo.gui.RoboFont;
import com.robonobo.gui.frames.RobonoboFrame;
import com.robonobo.gui.model.*;
import com.robonobo.gui.panels.ContentPanel;
import com.robonobo.gui.panels.LeftSidebar;

@SuppressWarnings("serial")
public class FriendTree extends LeftSidebarTree implements LeftSidebarComponent {
	static final Dimension MAX_LVL0_SZ = new Dimension(150, Integer.MAX_VALUE);
	static final Dimension MAX_LVL1_SZ = new Dimension(145, Integer.MAX_VALUE);
	static final Dimension MAX_LVL2_SZ = new Dimension(135, Integer.MAX_VALUE);
	LeftSidebar sideBar;
	ImageIcon rootIcon, addFriendsIcon, friendIcon, playlistIcon, libraryIcon;
	Font normalFont, boldFont;

	public FriendTree(LeftSidebar sb, RobonoboFrame frame) {
		super(new FriendTreeModel(frame), frame);
		this.sideBar = sb;
		getModel().setTree(this);
		normalFont = RoboFont.getFont(12, false);
		boldFont = RoboFont.getFont(12, true);
		setName("robonobo.playlist.tree");
		setAlignmentX(0.0f);
		setRootVisible(true);
		collapseRow(0);
		rootIcon = createImageIcon("/icon/friends.png", null);
		addFriendsIcon = createImageIcon("/icon/add_friends.png", null);
		friendIcon = createImageIcon("/icon/friend.png", null);
		playlistIcon = createImageIcon("/icon/playlist.png", null);
		libraryIcon = createImageIcon("/icon/home.png", null);
		setCellRenderer(new CellRenderer());
		setSelectionModel(new SelectionModel());
		getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		addTreeSelectionListener(new SelectionListener());
	}

	@Override
	public FriendTreeModel getModel() {
		return (FriendTreeModel) super.getModel();
	}

	public void selectForPlaylist(Long playlistId) {
		setSelectionPath(getModel().getPlaylistTreePath(playlistId));
	}

	public void selectForLibrary(long userId) {
		setSelectionPath(getModel().getLibraryTreePath(userId));
	}

	private class SelectionListener implements TreeSelectionListener {
		public void valueChanged(TreeSelectionEvent e) {
			TreePath tp = e.getNewLeadSelectionPath();
			if (tp == null)
				return;
			Object selNode = tp.getLastPathComponent();
			if (selNode instanceof ButtonTreeNode) {
				ButtonTreeNode btn = (ButtonTreeNode) selNode;
				btn.onClick();
				setSelectionPath(e.getOldLeadSelectionPath());
				return;
			}
			if (!(selNode instanceof SelectableTreeNode))
				return;
			SelectableTreeNode stn = (SelectableTreeNode) selNode;
			if (stn.wantSelect()) {
				sideBar.clearSelectionExcept(FriendTree.this);
				// If this is a library or playlist node with comments, and the comments tab is showing, mark the
				// comments as read
				FriendTreeModel m = getModel();
				if (stn instanceof LibraryTreeNode) {
					LibraryTreeNode ltn = (LibraryTreeNode) stn;
					ContentPanel cp = frame.mainPanel.getContentPanel("library/" + ltn.userId);
					// library content panel gets created on demand, so might be null
					if (cp != null && cp.tabPane.getSelectedIndex() == 1)
						m.markLibraryCommentsAsRead(ltn.userId);
				} else if (stn instanceof PlaylistTreeNode) {
					PlaylistTreeNode ptn = (PlaylistTreeNode) stn;
					long plId = ptn.getPlaylist().getPlaylistId();
					ContentPanel cp = frame.mainPanel.getContentPanel("playlist/" + plId);
					if (cp.tabPane.getSelectedIndex() == 1)
						m.markPlaylistCommentsAsRead(plId);
				}
				if (stn.handleSelect())
					m.firePathToRootChanged(stn);
			} else
				setSelectionPath(e.getOldLeadSelectionPath());
		}
	}

	private class CellRenderer extends DefaultTreeCellRenderer {
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
			final TreeNode node = (TreeNode) value;
			final JLabel lbl = (JLabel) super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);
			boolean useBold = false;
			boolean useRed = false;
			if (node instanceof PlaylistTreeNode) {
				lbl.setIcon(playlistIcon);
				lbl.setMaximumSize(MAX_LVL2_SZ);
				lbl.setPreferredSize(MAX_LVL2_SZ);
				PlaylistTreeNode ptn = (PlaylistTreeNode) node;
				int unseen = ptn.numUnseenTracks;
				if (!sel && unseen > 0) {
					lbl.setText("[" + unseen + "] " + lbl.getText());
					useBold = true;
				}
				if (ptn.hasComments) {
					useRed = true;
					useBold = true;
				}
			} else if (node instanceof LibraryTreeNode) {
				lbl.setIcon(libraryIcon);
				lbl.setMaximumSize(MAX_LVL2_SZ);
				lbl.setPreferredSize(MAX_LVL2_SZ);
				LibraryTreeNode ltn = (LibraryTreeNode) node;
				int unseen = ltn.numUnseenTracks;
				if (!sel && unseen > 0) {
					lbl.setText("[" + unseen + "] " + lbl.getText());
					useBold = true;
				}
				if (ltn.hasComments) {
					useRed = true;
					useBold = true;
				}
			} else if (node instanceof FriendTreeNode) {
				lbl.setIcon(friendIcon);
				lbl.setMaximumSize(MAX_LVL1_SZ);
				lbl.setPreferredSize(MAX_LVL1_SZ);
				int unseen = getTotalUnseen(node);
				if (!sel && unseen > 0) {
					lbl.setText("[" + unseen + "] " + lbl.getText());
					useBold = true;
				}
				if (anyComments(node)) {
					useBold = true;
					useRed = true;
				}
			} else if (node instanceof AddFriendsTreeNode) {
				lbl.setIcon(addFriendsIcon);
				lbl.setMaximumSize(MAX_LVL1_SZ);
				lbl.setPreferredSize(MAX_LVL1_SZ);
			} else if (node.getParent() == null) {
				lbl.setIcon(rootIcon);
				lbl.setMaximumSize(MAX_LVL0_SZ);
				lbl.setPreferredSize(MAX_LVL0_SZ);
				// Are there any unseen tracks at all?
				int unseen = getTotalUnseen(node);
				if (unseen > 0)
					useBold = true;
				if (anyComments(node)) {
					useBold = true;
					useRed = true;
				}
			}
			if (useBold)
				lbl.setFont(boldFont);
			else
				lbl.setFont(normalFont);
			if (sel) {
				lbl.setForeground(BLUE_GRAY);
				lbl.setBackground(LIGHT_GRAY);
			} else {
				lbl.setForeground(DARK_GRAY);
				lbl.setBackground(MID_GRAY);
			}
			if (useRed)
				lbl.setForeground(GREEN);
			return lbl;
		}

		public void paint(Graphics g) {
			paintComponent(g);
		}

		@Override
		protected void paintComponent(Graphics g) {
			GuiUtil.makeTextLookLessRubbish(g);
			super.paintComponent(g);
		}

		int getTotalUnseen(TreeNode n) {
			int unseen = 0;
			for (int i = 0; i < n.getChildCount(); i++) {
				TreeNode child = n.getChildAt(i);
				if (child instanceof PlaylistTreeNode)
					unseen += ((PlaylistTreeNode) child).numUnseenTracks;
				else if (child instanceof LibraryTreeNode)
					unseen += ((LibraryTreeNode) child).numUnseenTracks;
				else
					unseen += getTotalUnseen(child);
			}
			return unseen;
		}

		boolean anyComments(TreeNode n) {
			for (int i = 0; i < n.getChildCount(); i++) {
				TreeNode child = n.getChildAt(i);
				if (child instanceof PlaylistTreeNode) {
					if (((PlaylistTreeNode) child).hasComments)
						return true;
				} else if (child instanceof LibraryTreeNode) {
					if (((LibraryTreeNode) child).hasComments)
						return true;
				} else {
					if (anyComments(child))
						return true;
				}
			}
			return false;
		}
	}
}
