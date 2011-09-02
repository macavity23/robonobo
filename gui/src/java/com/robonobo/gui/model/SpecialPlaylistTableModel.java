package com.robonobo.gui.model;

import ca.odell.glazedlists.EventList;
import ca.odell.glazedlists.SortedList;

import com.robonobo.core.api.model.Playlist;
import com.robonobo.core.api.model.Track;
import com.robonobo.gui.frames.RobonoboFrame;

public class SpecialPlaylistTableModel extends PlaylistTableModel {
	protected SpecialPlaylistTableModel(RobonoboFrame frame, Playlist p, boolean canEdit, EventList<Track> el, SortedList<Track> sl) {
		super(frame, p, canEdit, el, sl);
		// We always allow delete, even if we can't edit
		canDelete = true;
	}
	
	public void setCanEdit(boolean canEdit) {
		this.canEdit = canEdit;
		if(canEdit)
			activate();
	}
}
