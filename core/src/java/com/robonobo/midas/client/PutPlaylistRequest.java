package com.robonobo.midas.client;

import com.robonobo.core.api.model.Playlist;
import com.robonobo.core.api.proto.CoreApi.PlaylistMsg;
import com.robonobo.core.metadata.PlaylistHandler;
import com.robonobo.midas.client.Params.Operation;

public class PutPlaylistRequest implements Request {
	MidasClientConfig cfg;
	Playlist p;
	PlaylistHandler handler;

	public PutPlaylistRequest(MidasClientConfig cfg, Playlist p, PlaylistHandler handler) {
		this.cfg = cfg;
		this.p = p;
		this.handler = handler;
	}

	@Override
	public int remaining() {
		if (p == null)
			return 0;
		return 1;
	}

	@Override
	public Params getNextParams() {
		// Unlike most classes, putplaylist also returns the updated playlist - with the plId set if this is a new
		// playlist (or just to catch updates from other users if not)
		Params params = new Params(Operation.Put, p.toMsg(), PlaylistMsg.newBuilder(), cfg.getPlaylistUrl(p.getPlaylistId()), p);
		p = null;
		return params;
	}

	@Override
	public void success(Object obj) {
		PlaylistMsg msg = (PlaylistMsg) obj;
		if(handler != null)
			handler.success(new Playlist(msg));
	}

	@Override
	public void error(Params p, Exception e) {
		if(handler != null) {
			Playlist pl = (Playlist) p.obj;
			handler.error(pl.getPlaylistId(), e);
		}
	}
}
