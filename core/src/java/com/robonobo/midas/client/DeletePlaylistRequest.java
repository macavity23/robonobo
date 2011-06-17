package com.robonobo.midas.client;

import com.robonobo.core.metadata.PlaylistCallback;
import com.robonobo.midas.client.Params.Operation;

public class DeletePlaylistRequest implements Request {
	MidasClientConfig cfg;
	Long plId;
	PlaylistCallback handler;
	
	public DeletePlaylistRequest(MidasClientConfig cfg, Long plId, PlaylistCallback handler) {
		this.cfg = cfg;
		this.plId = plId;
		this.handler = handler;
	}

	@Override
	public int remaining() {
		if(plId == null)
			return 0;
		return 1;
	}

	@Override
	public Params getNextParams() {
		Params p = new Params(Operation.Delete, null, null, cfg.getPlaylistUrl(plId), plId);
		plId = null;
		return p;
	}

	@Override
	public void success(Object obj) {
		if(handler != null)
			handler.success(null);
	}

	@Override
	public void error(Params p, Exception e) {
		if(handler != null) {
			Long plId = (Long) p.obj;
			handler.error(plId, e);
		}
	}

}
