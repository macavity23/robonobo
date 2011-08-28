package com.robonobo.midas.client;

import com.robonobo.core.metadata.PlaylistCallback;
import com.robonobo.midas.client.Params.Operation;

public class SpecialPlaylistPostRequest implements Request {
	MidasClientConfig cfg;
	String service;
	String msg;
	PlaylistCallback handler;
	private long userId;
	private String plName;

	public SpecialPlaylistPostRequest(MidasClientConfig cfg, String service, long userId, String plName, String msg, PlaylistCallback handler) {
		this.cfg = cfg;
		this.service = service;
		this.userId = userId;
		this.plName = plName;
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
		Params p = new Params(Operation.Get, null, null, cfg.getSpecialPlaylistPostUrl(service, userId, plName, msg), userId);
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
