package com.robonobo.midas.model;


import com.robonobo.core.api.model.Playlist;
import com.robonobo.core.api.proto.CoreApi.PlaylistMsg;

public class MidasPlaylist extends Playlist {
	public MidasPlaylist(PlaylistMsg msg) {
		super(msg);
	}
	
	public MidasPlaylist() {
	}
}
