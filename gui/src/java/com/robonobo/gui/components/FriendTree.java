package com.robonobo.gui.components;

import static com.robonobo.common.util.TextUtil.*;
import static com.robonobo.gui.GuiUtil.*;
import static com.robonobo.gui.RoboColor.*;

import java.awt.*;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

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
	Map<String, ImageIcon> specIcons = new HashMap<String, ImageIcon>();
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
		rootIcon = createImageIcon("/icon/friends.png");
		addFriendsIcon = createImageIcon("/icon/add_friends.png");
		friendIcon = createImageIcon("/icon/friend.png");
		playlistIcon = createImageIcon("/icon/playlist.png");
		libraryIcon = createImageIcon("/icon/home.png");
		// Special playlist icons
		specIcons.put("loves", createImageIcon("/icon/heart-small.png"));
		specIcons.put("radio", createImageIcon("/icon/radio-small.png"));
		setCellRenderer(new CellRenderer());
		setSelectionModel(new SelectionModel());
		getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
		addTreeSelectionListener(new SelectionListener());
		// This is required for tooltips to work
		ToolTipManager.sharedInstance().registerComponent(this);
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

	@Override
	public String getToolTipText(MouseEvent evt) {
		Point p = evt.getPoint();
		TreePath tp = getClosestPathForLocation(p.x, p.y);
		Object node = tp.getLastPathComponent();
		if(node instanceof PlaylistTreeNode) {
			PlaylistTreeNode ptn = (PlaylistTreeNode) node;
			int unseen = ptn.numUnseenTracks;
			if(unseen > 0) {
				String nt = numItems(unseen, "new track");
				if(ptn.hasComments) {
					// html allows us to have linebreaks
					return "<html>"+nt+"<br>Unread comments</html>";
				} else
					return nt;
			}
			if(ptn.hasComments)
				return "Unread comments";
		} else if(node instanceof LibraryTreeNode) {
			LibraryTreeNode ltn = (LibraryTreeNode) node;
			int unseen = ltn.numUnseenTracks;
			if(unseen > 0) {
				String nt = numItems(unseen, "new track");
				if(ltn.hasComments) {
					// html allows us to have linebreaks
					return "<html>"+nt+"<br>Unread comments</html>";
				} else
					return nt;
			}
			if(ltn.hasComments)
				return "Unread comments";
		}
		return null;
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
				PlaylistTreeNode ptn = (PlaylistTreeNode) node;
				Icon specIcon = specIcons.get(ptn.getPlaylist().getTitle().toLowerCase());
				if(specIcon != null)
					lbl.setIcon(specIcon);
				else
					lbl.setIcon(playlistIcon);
				lbl.setMaximumSize(MAX_LVL2_SZ);
				lbl.setPreferredSize(MAX_LVL2_SZ);
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
