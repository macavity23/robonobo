package com.robonobo.gui.model;

import java.awt.datatransfer.Transferable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.robonobo.common.concurrent.CatchingRunnable;
import com.robonobo.common.swing.SortableTreeNode;
import com.robonobo.core.api.model.Playlist;
import com.robonobo.gui.frames.RobonoboFrame;

@SuppressWarnings("serial")
public class PlaylistTreeNode extends SelectableTreeNode {
	Log log = LogFactory.getLog(getClass());
	RobonoboFrame frame;
	private Playlist playlist;
	int numUnseenTracks;

	public PlaylistTreeNode(Playlist p, RobonoboFrame frame) {
		super(p.getTitle());
		this.playlist = p;
		this.frame = frame;
		numUnseenTracks = frame.getController().numUnseenTracks(p);
	}

	public Playlist getPlaylist() {
		return playlist;
	}

	public void setPlaylist(final Playlist playlist, boolean isSelected) {
		this.playlist = playlist;
		setUserObject(playlist.getTitle());
		if (isSelected) {
			numUnseenTracks = 0;
			frame.getController().getExecutor().execute(new CatchingRunnable() {
				public void doRun() throws Exception {
					frame.getController().markAllAsSeen(playlist);
				}
			});
		} else
			numUnseenTracks = frame.getController().numUnseenTracks(playlist);
	}

	@Override
	public boolean wantSelect() {
		return true;
	}

	@Override
	public boolean handleSelect() {
		numUnseenTracks = 0;
		frame.getMainPanel().selectContentPanel(contentPanelName());
		frame.getController().getExecutor().execute(new CatchingRunnable() {
			public void doRun() throws Exception {
				frame.getController().markAllAsSeen(playlist);
				// Start finding sources for this guy
				PlaylistTableModel model = (PlaylistTableModel) frame.getMainPanel()
						.getContentPanel(contentPanelName()).getTrackList().getModel();
				model.activate();
			}
		});
		return true;
	}

	public int getNumUnseenTracks() {
		return numUnseenTracks;
	}

	protected String contentPanelName() {
		return "playlist/" + playlist.getPlaylistId();
	}

	@Override
	public boolean importData(Transferable t) {
		return false;
	}

	@Override
	public int compareTo(SortableTreeNode o) {
		if (o instanceof LibraryTreeNode)
			return 1;
		PlaylistTreeNode other = (PlaylistTreeNode) o;
		return playlist.getTitle().compareTo(other.getPlaylist().getTitle());
	}
}