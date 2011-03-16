package com.robonobo.gui.components;

import static com.robonobo.gui.GUIUtil.*;
import static com.robonobo.gui.RoboColor.*;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;

import com.robonobo.core.api.model.User;
import com.robonobo.gui.GUIUtil;
import com.robonobo.gui.RoboFont;
import com.robonobo.gui.frames.RobonoboFrame;
import com.robonobo.gui.model.*;
import com.robonobo.gui.panels.LeftSidebar;

@SuppressWarnings("serial")
public class FriendTree extends LeftSidebarTree implements LeftSidebarComponent {
	static final Dimension MAX_LVL0_SZ = new Dimension(150, Integer.MAX_VALUE);
	static final Dimension MAX_LVL1_SZ = new Dimension(145, Integer.MAX_VALUE);
	static final Dimension MAX_LVL2_SZ = new Dimension(135, Integer.MAX_VALUE);

	LeftSidebar sideBar;
	ImageIcon rootIcon, friendIcon, playlistIcon, libraryIcon;
	Font normalFont, boldFont;

	public FriendTree(LeftSidebar sb, RobonoboFrame frame) {
		super(new FriendTreeModel(frame), frame);
		this.sideBar = sb;

		normalFont = RoboFont.getFont(13, false);
		boldFont = RoboFont.getFont(13, true);
		setName("robonobo.playlist.tree");
		setAlignmentX(0.0f);
		setRootVisible(true);
		collapseRow(0);

		rootIcon = createImageIcon("/icon/friends.png", null);
		friendIcon = createImageIcon("/icon/friend.png", null);
		playlistIcon = createImageIcon("/icon/playlist.png", null);
		libraryIcon = createImageIcon("/icon/home.png", null);

		setCellRenderer(new CellRenderer());
		setSelectionModel(new SelectionModel());
		getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		addTreeSelectionListener(new TreeSelectionListener() {
			public void valueChanged(TreeSelectionEvent e) {
				TreePath tp = e.getNewLeadSelectionPath();
				if (tp == null)
					return;
				if (!(tp.getLastPathComponent() instanceof SelectableTreeNode))
					return;
				SelectableTreeNode stn = (SelectableTreeNode) tp.getLastPathComponent();
				if (stn.wantSelect()) {
					sideBar.clearSelectionExcept(FriendTree.this);
					if (stn.handleSelect())
						getModel().firePathToRootChanged(stn);
				} else
					setSelectionPath(e.getOldLeadSelectionPath());
			}
		});
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
	
	private class CellRenderer extends DefaultTreeCellRenderer {
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
				boolean leaf, int row, boolean hasFocus) {
			final TreeNode node = (TreeNode) value;
			final JLabel lbl = (JLabel) super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row,
					hasFocus);
			if (node instanceof PlaylistTreeNode) {
				lbl.setIcon(playlistIcon);
				lbl.setMaximumSize(MAX_LVL2_SZ);
				lbl.setPreferredSize(MAX_LVL2_SZ);
				PlaylistTreeNode ptn = (PlaylistTreeNode) node;
				int unseen = ptn.getNumUnseenTracks();
				if (unseen > 0) {
					lbl.setText("[" + unseen + "] " + lbl.getText());
					lbl.setFont(boldFont);
				} else
					lbl.setFont(normalFont);
			} else if(node instanceof LibraryTreeNode) {
				lbl.setIcon(libraryIcon);
				lbl.setMaximumSize(MAX_LVL2_SZ);
				lbl.setPreferredSize(MAX_LVL2_SZ);
				LibraryTreeNode ltn = (LibraryTreeNode) node;
				int unseen = ltn.getNumUnseenTracks();
				if (unseen > 0) {
					lbl.setText("[" + unseen + "] " + lbl.getText());
					lbl.setFont(boldFont);
				} else
					lbl.setFont(normalFont);
			} else if (node instanceof FriendTreeNode) {
				lbl.setIcon(friendIcon);
				lbl.setMaximumSize(MAX_LVL1_SZ);
				lbl.setPreferredSize(MAX_LVL1_SZ);
				int unseen = getTotalUnseen(node);
				if (unseen > 0) {
					lbl.setText("[" + unseen + "] " + lbl.getText());
					lbl.setFont(boldFont);
				} else
					lbl.setFont(normalFont);
			} else if (node.getParent() == null) {
				lbl.setIcon(rootIcon);
				lbl.setMaximumSize(MAX_LVL0_SZ);
				lbl.setPreferredSize(MAX_LVL0_SZ);
				// Are there any unseen tracks at all?
				int unseen = getTotalUnseen(node);
				if (unseen > 0)
					lbl.setFont(boldFont);
				else
					lbl.setFont(normalFont);
			} else
				lbl.setFont(normalFont);

			if (sel) {
				lbl.setForeground(BLUE_GRAY);
				lbl.setBackground(LIGHT_GRAY);
			} else {
				lbl.setForeground(DARK_GRAY);
				lbl.setBackground(MID_GRAY);
			}
			return lbl;
		}

		public void paint(Graphics g) {
			paintComponent(g);
		}

		@Override
		protected void paintComponent(Graphics g) {
			GUIUtil.makeTextLookLessRubbish(g);
			super.paintComponent(g);
		}
		
		int getTotalUnseen(TreeNode n) {
			int unseen = 0;
			for (int i = 0; i < n.getChildCount(); i++) {
				TreeNode child = n.getChildAt(i);
				if(child instanceof PlaylistTreeNode)
					unseen += ((PlaylistTreeNode)child).getNumUnseenTracks();
				else if(child instanceof LibraryTreeNode)
					unseen += ((LibraryTreeNode)child).getNumUnseenTracks();
				else
					unseen += getTotalUnseen(child);
			}
			return unseen;
		}
	}
}
