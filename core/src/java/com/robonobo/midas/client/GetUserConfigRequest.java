package com.robonobo.midas.client;

import com.robonobo.core.api.model.UserConfig;
import com.robonobo.core.api.proto.CoreApi.UserConfigMsg;
import com.robonobo.core.metadata.UserConfigCallback;
import com.robonobo.midas.client.Params.Operation;

public class GetUserConfigRequest implements Request {
	MidasClientConfig cfg;
	long uid;
	UserConfigCallback handler;
	
	public GetUserConfigRequest(MidasClientConfig cfg, long uid, UserConfigCallback handler) {
		this.cfg = cfg;
		this.uid = uid;
		this.handler = handler;
	}

	@Override
	public int remaining() {
		if(uid > 0)
			return 1;
		return 0;
	}

	@Override
	public Params getNextParams() {
		Params params = new Params(Operation.Get, null, UserConfigMsg.newBuilder(), cfg.getUserConfigUrl(uid), uid);
		uid = -1;
		return params;
	}

	@Override
	public void success(Object obj) {
		UserConfigMsg msg = (UserConfigMsg) obj;
		if(handler != null)
			handler.success(new UserConfig(msg));
	}

	@Override
	public void error(Params p, Exception e) {
		Long uid = (Long) p.obj;
		if(handler != null)
			handler.error(uid, e);
	}

}
