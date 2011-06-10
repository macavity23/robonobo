package com.robonobo.midas.client;

import com.robonobo.core.metadata.PlaylistHandler;
import com.robonobo.midas.client.Params.Operation;

public class PlaylistServiceUpdateRequest implements Request {
	MidasClientConfig cfg;
	String service;
	long playlistId;
	String msg;
	PlaylistHandler handler;

	public PlaylistServiceUpdateRequest(MidasClientConfig cfg, String service, long playlistId, String msg, PlaylistHandler handler) {
		this.cfg = cfg;
		this.service = service;
		this.playlistId = playlistId;
		this.msg = msg;
		this.handler = handler;
	}

	@Override
	public int remaining() {
		if (service == null)
			return 0;
		return 1;
	}

	@Override
	public Params getNextParams() {
		Params p = new Params(Operation.Get, null, null, cfg.getPlaylistServiceUpdateUrl(service, playlistId, msg), playlistId);
		service = null;
		return p;
	}

	@Override
	public void success(Object obj) {
		if (handler != null)
			handler.success(null);
	}

	@Override
	public void error(Params p, Exception e) {
		if (handler != null) {
			Long plId = (Long) p.obj;
			handler.error(plId, e);
		}
	}
}
