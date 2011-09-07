package com.robonobo.gui.model;

import java.awt.datatransfer.Transferable;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.robonobo.common.concurrent.CatchingRunnable;
import com.robonobo.common.exceptions.SeekInnerCalmException;
import com.robonobo.common.swing.SortableTreeNode;
import com.robonobo.core.api.model.Playlist;
import com.robonobo.gui.frames.RobonoboFrame;

@SuppressWarnings("serial")
public class PlaylistTreeNode extends SelectableTreeNode {
	Log log = LogFactory.getLog(getClass());
	RobonoboFrame frame;
	public Playlist p;
	public int numUnseenTracks;
	public boolean hasComments;

	public PlaylistTreeNode(Playlist p, RobonoboFrame frame) {
		super(p.getTitle());
		this.p = p;
		this.frame = frame;
		numUnseenTracks = frame.ctrl.numUnseenTracks(p);
	}

	public Playlist getPlaylist() {
		return p;
	}

	public void setPlaylist(final Playlist playlist, boolean isSelected) {
		this.p = playlist;
		setUserObject(playlist.getTitle());
		if (isSelected) {
			numUnseenTracks = 0;
			frame.ctrl.getExecutor().execute(new CatchingRunnable() {
				public void doRun() throws Exception {
					frame.ctrl.markAllAsSeen(playlist);
				}
			});
		} else
			numUnseenTracks = frame.ctrl.numUnseenTracks(playlist);
	}

	@Override
	public boolean wantSelect() {
		return true;
	}

	@Override
	public boolean handleSelect() {
		numUnseenTracks = 0;
		frame.mainPanel.selectContentPanel(contentPanelName());
		frame.ctrl.getExecutor().execute(new CatchingRunnable() {
			public void doRun() throws Exception {
				frame.ctrl.markAllAsSeen(p);
				// Start finding sources for this guy
				PlaylistTableModel model = (PlaylistTableModel) frame.mainPanel.getContentPanel(contentPanelName()).trackList.getModel();
				model.activate();
			}
		});
		return true;
	}

	protected String contentPanelName() {
		return "playlist/" + p.getPlaylistId();
	}

	protected int getSpecialIndex() {
		return Integer.MAX_VALUE;
	}

	@Override
	public boolean importData(Transferable t) {
		return false;
	}

	@Override
	public int compareTo(SortableTreeNode o) {
		int result;
		if(o instanceof PlaylistTreeNode) {
			PlaylistTreeNode optn = (PlaylistTreeNode) o;
			String t1 = p.getTitle();
			String t2 = optn.p.getTitle();
			int specIdx = getSpecialIndex();
			int oSpecIdx = optn.getSpecialIndex();
			if (specIdx < oSpecIdx)
				result = -1;
			else if (oSpecIdx < specIdx)
				result = 1;
			else 
				result = p.getTitle().toLowerCase().compareTo(optn.p.getTitle().toLowerCase());
			log.debug("Comparing PTNs for playlists "+p.getTitle()+" and "+optn.p.getTitle()+" - result is "+result);
		} else if (o instanceof LibraryTreeNode)
			result = 1;
		else
			throw new SeekInnerCalmException();
		return result;
	}
}