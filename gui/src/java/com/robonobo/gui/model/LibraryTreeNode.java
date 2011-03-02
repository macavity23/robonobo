package com.robonobo.gui.model;

import java.awt.datatransfer.Transferable;

import com.robonobo.common.concurrent.CatchingRunnable;
import com.robonobo.common.swing.SortableTreeNode;
import com.robonobo.core.api.model.Library;
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
		if(o instanceof LibraryTreeNode)
			return 0;
		return -1;
	}

	public Library getLib() {
		return lib;
	}

	public void setLib(Library lib) {
		this.lib = lib;
		numUnseenTracks = frame.getController().numUnseenTracks(lib);
	}
	
	public int getNumUnseenTracks() {
		return numUnseenTracks;
	}
	
	protected String contentPanelName() {
		return "library/"+lib.getUserId();
	}


	@Override
	public boolean wantSelect() {
		return true;
	}
	
	@Override
	public boolean handleSelect() {
		numUnseenTracks = 0;
		frame.getController().markAllAsSeen(lib);
		frame.getMainPanel().selectContentPanel(contentPanelName());
		// Start finding sources for this guy
		frame.getController().getExecutor().execute(new CatchingRunnable() {
			public void doRun() throws Exception {
				FriendLibraryTableModel model = (FriendLibraryTableModel) frame.getMainPanel()
						.getContentPanel(contentPanelName()).getTrackList().getModel();
				model.activate();
			}
		});
		return true;
	}
	
	
	@Override
	public boolean importData(Transferable t) {
		return false;
	}
}
