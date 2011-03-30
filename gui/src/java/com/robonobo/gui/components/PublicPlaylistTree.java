package com.robonobo.gui.components;

import static com.robonobo.gui.GUIUtil.*;
import static com.robonobo.gui.RoboColor.*;

import java.awt.*;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;

import com.robonobo.gui.GUIUtil;
import com.robonobo.gui.RoboFont;
import com.robonobo.gui.frames.RobonoboFrame;
import com.robonobo.gui.model.*;
import com.robonobo.gui.panels.LeftSidebar;

@SuppressWarnings("serial")
public class  PublicPlaylistTree extends LeftSidebarTree {
	private LeftSidebar sideBar;
	private ImageIcon rootIcon;
	private ImageIcon playlistIcon;
	private Font font;

	public PublicPlaylistTree(LeftSidebar sb, RobonoboFrame frame) {
		super(new PublicPlaylistTreeModel(frame), frame);
		this.sideBar = sb;
		setName("robonobo.playlist.tree");
		setAlignmentX(0.0f);
		setRootVisible(true);
		font = RoboFont.getFont(12, false);
		rootIcon = createImageIcon("/icon/world.png", null);
		playlistIcon = createImageIcon("/icon/playlist.png", null);
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
					sideBar.clearSelectionExcept(PublicPlaylistTree.this);
					if (stn.handleSelect())
						getModel().firePathToRootChanged(stn);
				} else
					setSelectionPath(e.getOldLeadSelectionPath());
			}
		});
	}

	@Override
	public PublicPlaylistTreeModel getModel() {
		return (PublicPlaylistTreeModel) super.getModel();
	}
	
	public void selectForPlaylist(Long playlistId) {
		setSelectionPath(getModel().getPlaylistTreePath(playlistId));
	}

	private class CellRenderer extends DefaultTreeCellRenderer {
		@Override
		public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
				boolean leaf, int row, boolean hasFocus) {
			final JLabel lbl = (JLabel) super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row,
					hasFocus);
			final TreeNode node = (TreeNode) value;
			if (node instanceof PlaylistTreeNode) {
				lbl.setIcon(playlistIcon);
				lbl.setMaximumSize(FriendTree.MAX_LVL1_SZ);
				lbl.setPreferredSize(FriendTree.MAX_LVL1_SZ);
			} else if (node.getParent() == null)
				lbl.setIcon(rootIcon);
			if (getSelectionPath() != null && node.equals(getSelectionPath().getLastPathComponent()))
				lbl.setForeground(BLUE_GRAY);
			else
				lbl.setForeground(DARK_GRAY);
			lbl.setFont(font);
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
	}
}
