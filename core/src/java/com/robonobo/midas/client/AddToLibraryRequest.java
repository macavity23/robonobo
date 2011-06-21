package com.robonobo.midas.client;

import com.robonobo.core.api.model.Library;
import com.robonobo.core.metadata.LibraryCallback;
import com.robonobo.midas.client.Params.Operation;

public class AddToLibraryRequest implements Request {
	protected MidasClientConfig cfg;
	protected Long userId;
	protected Library lib;
	protected LibraryCallback handler;

	public AddToLibraryRequest(MidasClientConfig cfg, long userId, Library lib, LibraryCallback handler) {
		this.cfg = cfg;
		this.userId = userId;
		this.lib = lib;
		this.handler = handler;
	}

	@Override
	public int remaining() {
		if(userId == null)
			return 0;
		return 1;
	}

	@Override
	public Params getNextParams() {
		Params p = new Params(Operation.Put, lib.toMsg(), null, getUrl(), userId);
		userId = null;
		return p;
	}

	protected String getUrl() {
		return cfg.getLibraryAddUrl(userId);
	}

	@Override
	public void success(Object obj) {
		if(handler != null) 
			handler.success(null);
	}

	@Override
	public void error(Params p, Exception e) {
		if(handler != null) {
			Long uid = (Long) p.obj;
			handler.error(uid, e);
		}
	}
	
}
