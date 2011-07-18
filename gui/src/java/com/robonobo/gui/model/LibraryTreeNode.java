package com.robonobo.gui.model;

import java.awt.datatransfer.Transferable;

import javax.swing.SwingUtilities;

import com.robonobo.common.concurrent.CatchingRunnable;
import com.robonobo.common.swing.SortableTreeNode;
import com.robonobo.core.api.model.Library;
import com.robonobo.gui.components.TrackList;
import com.robonobo.gui.frames.RobonoboFrame;
import com.robonobo.gui.panels.ContentPanel;
import com.robonobo.gui.panels.FriendLibraryContentPanel;
import com.robonobo.gui.sheets.PleaseWaitSheet;

@SuppressWarnings("serial")
public class LibraryTreeNode extends SelectableTreeNode {
	long userId;
	RobonoboFrame frame;
	int numUnseenTracks;

	public LibraryTreeNode(RobonoboFrame frame, long userId, int numUnseenTracks) {
		super("Library");
		this.frame = frame;
		this.userId = userId;
		this.numUnseenTracks = numUnseenTracks;
	}

	@Override
	public int compareTo(SortableTreeNode o) {
		// We are equal to other library nodes (though there shouldn't be any), but before anything else
		if (o instanceof LibraryTreeNode)
			return 0;
		return -1;
	}

	public void setNumUnseenTracks(int numUnseenTracks) {
		this.numUnseenTracks = numUnseenTracks;
	}

	public int getNumUnseenTracks() {
		return numUnseenTracks;
	}

	protected String contentPanelName() {
		return "library/" + userId;
	}

	@Override
	public boolean wantSelect() {
		return true;
	}

	@Override
	public boolean handleSelect() {
		boolean hadUnseen = (numUnseenTracks > 0);
		numUnseenTracks = 0;
		ContentPanel cp = frame.getMainPanel().getContentPanel(contentPanelName());
		if (cp != null) {
			frame.getMainPanel().selectContentPanel(contentPanelName());
			activatePanel();
		} else {
			// Need to create the content panel - this hangs the ui thread as it looks up the tracks and creates the
			// table, which might take a couple of seconds if there are a lot of tracks in the library
			// We could do the db lookup off the ui thread, but the table creation is the slowest bit and that has to be
			// on there, so might as well do the whole thing there and just show a loading screen to the user
			CatchingRunnable task = new CatchingRunnable() {
				public void doRun() throws Exception {
					Library lib = frame.getController().getFriendLibrary(userId);
					frame.getMainPanel().addContentPanel(contentPanelName(), new FriendLibraryContentPanel(frame, lib));
					frame.getMainPanel().selectContentPanel(contentPanelName());
					activatePanel();
				}
			};
			frame.runSlowTask("library loading", task);
		}
		return hadUnseen;
	}

	private void activatePanel() {
		frame.getController().markAllLibraryTracksAsSeen(userId);
		// Activate this panel so it can find sources
		TrackList trackList = frame.getMainPanel().getContentPanel(contentPanelName()).getTrackList();
		FriendLibraryTableModel model = (FriendLibraryTableModel) trackList.getModel();
		model.activate();
		trackList.updateViewport();
	}

	@Override
	public boolean importData(Transferable t) {
		return false;
	}
}
