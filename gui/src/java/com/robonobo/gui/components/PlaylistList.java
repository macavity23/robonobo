package com.robonobo.gui.components;

import static com.robonobo.gui.GuiUtil.*;
import static com.robonobo.gui.RoboColor.*;
import static javax.swing.SwingUtilities.*;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.util.ArrayList;
import java.util.List;

import javax.swing.*;

import org.jdesktop.swingx.renderer.DefaultListRenderer;

import com.robonobo.common.concurrent.CatchingRunnable;
import com.robonobo.core.Platform;
import com.robonobo.core.api.UserPlaylistListener;
import com.robonobo.core.api.model.*;
import com.robonobo.gui.components.base.RLabel12;
import com.robonobo.gui.frames.RobonoboFrame;
import com.robonobo.gui.model.PlaylistListModel;
import com.robonobo.gui.model.StreamTransfer;
import com.robonobo.gui.panels.ContentPanel;
import com.robonobo.gui.panels.LeftSidebar;

@SuppressWarnings("serial")
public class PlaylistList extends LeftSidebarList implements UserPlaylistListener {
	private static final int MAX_LBL_WIDTH = 170;

	ImageIcon playlistIcon;

	public PlaylistList(LeftSidebar sideBar, RobonoboFrame frame) {
		super(sideBar, frame, new PlaylistListModel(frame.getController()));
		playlistIcon = createImageIcon("/icon/playlist.png", null);
		setCellRenderer(new CellRenderer());
		setName("robonobo.playlist.list");
		setAlignmentX(0.0f);
		setMaximumSize(new Dimension(65535, 65535));
		// We do the listener stuff here rather than in the model as we may need to reselect or resize as a consequence
		frame.getController().addUserPlaylistListener(this);
		setTransferHandler(new DnDHandler());
	}

	public PlaylistListModel getModel() {
		return (PlaylistListModel) super.getModel();
	}

	public void selectPlaylist(final Playlist p) {
		invokeLater(new CatchingRunnable() {
			public void doRun() throws Exception {
				setSelectedIndex(getModel().getPlaylistIndex(p));
			}
		});
	}

	@Override
	public void loggedIn() {
		invokeLater(new CatchingRunnable() {
			public void doRun() throws Exception {
				getModel().clear();
			}
		});
	}

	@Override
	public void allUsersAndPlaylistsUpdated() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void userChanged(final User u) {
		invokeLater(new CatchingRunnable() {
			public void doRun() throws Exception {
				if (u.getUserId() != frame.getController().getMyUser().getUserId())
					return;

				Playlist selP = (getSelectedIndex() < 0) ? null : getModel().getPlaylistAt(getSelectedIndex());
				boolean selPGone = false;

				// Check for removed playlists
				List<Playlist> toRm = new ArrayList<Playlist>();
				for (Playlist p : getModel()) {
					if (!u.getPlaylistIds().contains(p.getPlaylistId()))
						toRm.add(p);
				}
				for (Playlist p : toRm) {
					if (p.equals(selP))
						selPGone = true;
					getModel().remove(p);
				}

				// Removing items might have buggered up the selection, so put it back.
				// If the selected playlist has gone, then go to my library
				if (selP != null) {
					if (selPGone)
						frame.getLeftSidebar().selectMyMusic();
					else {
						int idx = getModel().getPlaylistIndex(selP);
						setSelectedIndex(idx);
					}
				}
			}
		});
	}

	@Override
	public void playlistChanged(final Playlist p) {
		invokeLater(new CatchingRunnable() {
			public void doRun() throws Exception {
				Long plId = p.getPlaylistId();
				if (frame.getController().getMyUser().getPlaylistIds().contains(plId)) {
					Playlist selP = (getSelectedIndex() < 0) ? null : getModel().getPlaylistAt(getSelectedIndex());
					boolean needReselect = p.equals(selP);
					getModel().remove(p);
					getModel().insertSorted(p);
					if (needReselect) {
						int idx = getModel().getPlaylistIndex(p);
						setSelectedIndex(idx);
					}
				}
			}
		});
	}

	@Override
	public void libraryChanged(Library lib) {
		// Do nothing
	}

	@Override
	public void userConfigChanged(UserConfig cfg) {
		// Do nothing
	}

	@Override
	protected void itemSelected(int index) {
		PlaylistListModel m = (PlaylistListModel) getModel();
		Playlist p = m.getPlaylistAt(index);
		frame.getMainPanel().selectContentPanel("playlist/" + p.getPlaylistId());
	}

	class ItemLbl extends RLabel12 {
		public ItemLbl() {
			setOpaque(true);
			setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 0));
			setMaximumSize(new Dimension(MAX_LBL_WIDTH, 65535));
			setPreferredSize(new Dimension(MAX_LBL_WIDTH, 65535));
		}
	}

	class CellRenderer extends DefaultListRenderer {
		JLabel lbl = new ItemLbl();

		@Override
		public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
				boolean cellHasFocus) {
			lbl.setText((String) value);
			lbl.setIcon(playlistIcon);
			if (isSelected) {
				lbl.setBackground(LIGHT_GRAY);
				lbl.setForeground(BLUE_GRAY);
			} else {
				lbl.setBackground(MID_GRAY);
				lbl.setForeground(DARK_GRAY);
			}
			return lbl;
		}
	}

	class DnDHandler extends TransferHandler {
		@Override
		public boolean canImport(JComponent comp, DataFlavor[] transferFlavors) {
			for (DataFlavor dataFlavor : transferFlavors) {
				if (dataFlavor.equals(StreamTransfer.DATA_FLAVOR))
					return true;
			}
			return Platform.getPlatform().canDnDImport(transferFlavors);
		}

		@Override
		public boolean importData(JComponent comp, Transferable t) {
			Playlist p = getModel().getPlaylistAt(getSelectedIndex());
			String cpName = "playlist/" + p.getPlaylistId();
			ContentPanel cp = frame.getMainPanel().getContentPanel(cpName);
			return cp.importData(comp, t);
		}
	}
}
