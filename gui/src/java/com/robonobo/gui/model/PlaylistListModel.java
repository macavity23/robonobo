package com.robonobo.gui.model;

import java.util.HashSet;
import java.util.Set;

import com.robonobo.core.RobonoboController;
import com.robonobo.core.api.model.Playlist;

@SuppressWarnings("serial")
public class PlaylistListModel extends SortedListModel<Playlist> {
	RobonoboController control;
	Set<Long> plIds = new HashSet<Long>();

	public PlaylistListModel(RobonoboController control) {
		this.control = control;
	}

	@Override
	public Object getElementAt(int index) {
		String title = get(index).getTitle();
		return title;
	}

	public Playlist getPlaylistAt(int index) {
		return get(index);
	}

	public int getPlaylistIndex(Playlist p) {
		return list.indexOf(p);
	}
	
	@Override
	public void insertSorted(Playlist p) {
		plIds.add(p.getPlaylistId());
		super.insertSorted(p);
	}
	
	@Override
	public void remove(Playlist p) {
		plIds.remove(p.getPlaylistId());
		super.remove(p);
	}
	
	public boolean hasPlaylist(long plId) {
		return plIds.contains(plId);
	}
}
