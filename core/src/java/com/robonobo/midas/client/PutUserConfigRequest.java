package com.robonobo.midas.client;

import com.robonobo.core.api.model.UserConfig;
import com.robonobo.core.metadata.UserConfigHandler;
import com.robonobo.midas.client.Params.Operation;

public class PutUserConfigRequest implements Request {
	MidasClientConfig cfg;
	UserConfig uc;
	UserConfigHandler handler;

	public PutUserConfigRequest(MidasClientConfig cfg, UserConfig uc, UserConfigHandler handler) {
		this.cfg = cfg;
		this.uc = uc;
		this.handler = handler;
	}

	@Override
	public int remaining() {
		if (uc == null)
			return 0;
		return 1;
	}

	@Override
	public Params getNextParams() {
		Params p = new Params(Operation.Put, uc.toMsg(), null, cfg.getUserConfigUrl(uc.getUserId()), uc);
		uc = null;
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
			UserConfig uc = (UserConfig) p.obj;
			handler.error(uc.getUserId(), e);
		}
	}
}
