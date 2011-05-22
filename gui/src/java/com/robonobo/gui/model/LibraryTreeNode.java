package com.robonobo.gui.model;

import java.awt.datatransfer.Transferable;

import com.robonobo.common.concurrent.CatchingRunnable;
import com.robonobo.common.swing.SortableTreeNode;
import com.robonobo.core.api.model.Library;
import com.robonobo.gui.components.TrackList;
import com.robonobo.gui.frames.RobonoboFrame;

@SuppressWarnings("serial")
public class LibraryTreeNode extends SelectableTreeNode {
	Library lib;
	RobonoboFrame frame;
	int numUnseenTracks;

	public LibraryTreeNode(RobonoboFrame frame, Library lib) {
		super("Library");
		this.frame = frame;
		this.lib = lib;
		numUnseenTracks = frame.getController().numUnseenTracks(lib);
	}

	@Override
	public int compareTo(SortableTreeNode o) {
		// We are equal to other library nodes (though there shouldn't be any), but before anything else
		if (o instanceof LibraryTreeNode)
			return 0;
		return -1;
	}

	public Library getLib() {
		return lib;
	}

	public void setLib(final Library lib, boolean isSelected) {
		this.lib = lib;
		// If we're selected, don't show any unseen tracks
		if (isSelected) {
			numUnseenTracks = 0;
			frame.getController().getExecutor().execute(new CatchingRunnable() {
				public void doRun() throws Exception {
					frame.getController().markAllAsSeen(lib);
				}
			});
		} else
			numUnseenTracks = frame.getController().numUnseenTracks(lib);
	}

	public int getNumUnseenTracks() {
		return numUnseenTracks;
	}

	protected String contentPanelName() {
		return "library/" + lib.getUserId();
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
				// Do this off the ui thread as there's a db hit marking 10^4 tracks as read
				frame.getController().markAllAsSeen(lib);
				// Activate this panel so it can find sources
				TrackList trackList = frame.getMainPanel().getContentPanel(contentPanelName()).getTrackList();
				FriendLibraryTableModel model = (FriendLibraryTableModel) trackList.getModel();
				model.activate();
				trackList.activate();
			}
		});
		return true;
	}

	@Override
	public boolean importData(Transferable t) {
		return false;
	}
}
