package com.robonobo.gui.model;

import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.robonobo.core.RobonoboController;
import com.robonobo.core.api.model.Playlist;

@SuppressWarnings("serial")
public class PlaylistListModel extends SortedListModel<Playlist> {
	RobonoboController control;
	Set<Long> plIds = new HashSet<Long>();
	Map<Long, Integer> unseenMap = new HashMap<Long, Integer>();
	Map<Long, Boolean> hasCommentMap = new HashMap<Long, Boolean>();
	Log log = LogFactory.getLog(getClass());

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
	
	public int numUnseen(int index) {
		Playlist p = get(index);
		Long plId = p.getPlaylistId();
		Integer result = unseenMap.get(plId);
		if(result == null)
			return 0;
		return result;
	}
	
	public boolean hasComments(long plId) {
		Boolean result = hasCommentMap.get(plId);
		if(result == null)
			return false;
		return result;
	}
	
	/**
	 * @return whether the value changed or not
	 */
	public void setHasComments(long plId, boolean has) {
		Boolean had = hasCommentMap.get(plId);
		if(had == null)
			had = false;
		hasCommentMap.put(plId, has);
		boolean changed = (has != had);
		if(changed)
			fireContentsChanged(this, 0, getSize()-1);
	}
	
	public void markAllAsSeen(int index) {
		Playlist p = get(index);
		Long plId = p.getPlaylistId();
		unseenMap.put(plId, 0);
	}
	
	@Override
	public void insertSorted(Playlist p) {
		long plId = p.getPlaylistId();
		if(plIds.contains(plId)) {
			reInsertSorted(p);
			return;
		}
		plIds.add(plId);
		int unseen = control.numUnseenTracks(p);
		unseenMap.put(plId, unseen);
		super.insertSorted(p);
	}
	
	@Override
	public void remove(Playlist p) {
		long plId = p.getPlaylistId();
		plIds.remove(plId);
		unseenMap.remove(plId);
		hasCommentMap.remove(plId);
		super.remove(p);
	}
	
	private void reInsertSorted(Playlist p) {
		super.remove(p);
		super.insertSorted(p);
	}
	
	public boolean hasPlaylist(long plId) {
		return plIds.contains(plId);
	}
}
