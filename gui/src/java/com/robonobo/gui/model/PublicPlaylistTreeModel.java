package com.robonobo.gui.model;

import java.util.HashMap;
import java.util.Map;

import javax.swing.tree.TreePath;

import com.robonobo.common.swing.SortedTreeModel;
import com.robonobo.core.api.model.Playlist;
import com.robonobo.gui.frames.RobonoboFrame;

@SuppressWarnings("serial")
public class PublicPlaylistTreeModel extends SortedTreeModel {
	private RobonoboFrame frame;
	private SelectableTreeNode myRoot;
	private Map<Long, PlaylistTreeNode> playlistNodes = new HashMap<Long, PlaylistTreeNode>();
	
	public PublicPlaylistTreeModel(RobonoboFrame frame) {
		super(null);
		this.frame = frame;
		myRoot = new SelectableTreeNode("Public Playlists");
		setRoot(myRoot);
	}

	public void addPlaylist(Playlist p) {
		PlaylistTreeNode ptn = new PlaylistTreeNode(p, frame);
		playlistNodes.put(p.getPlaylistId(), ptn);
		insertNodeSorted(myRoot, ptn);
	}
	
	public TreePath getPlaylistTreePath(Long playlistId) {
		// NB If the playlist is in the tree more than once (eg shared
		// playlist), this will select the first instance only...
		if(playlistNodes.containsKey(playlistId))
			return new TreePath(getPathToRoot(playlistNodes.get(playlistId)));
		return null;
	}
	
	public boolean hasPlaylist(long plId) {
		return playlistNodes.containsKey(plId);
	}
}
