package com.robonobo.midas.client;

import com.robonobo.core.api.model.User;
import com.robonobo.core.api.proto.CoreApi.UserMsg;
import com.robonobo.core.metadata.UserHandler;
import com.robonobo.midas.client.Params.Operation;

public class LoginRequest implements Request {
	private MidasClientConfig cfg;
	private String email;
	private String password;
	private UserHandler handler;

	public LoginRequest(MidasClientConfig cfg, String email, String password, UserHandler handler) {
		this.cfg = cfg;
		this.email = email;
		this.password = password;
		this.handler = handler;
	}

	@Override
	public int remaining() {
		return (email == null) ? 0 : 1;
	}

	@Override
	public Params getNextParams() {
		Params p = new Params(Operation.Get, null, UserMsg.newBuilder(), cfg.getUserUrl(email), email);
		p.username = email;
		p.password = password;
		email = null;
		return p;
	}

	@Override
	public void success(Object obj) {
		UserMsg msg = (UserMsg) obj;
		handler.success(new User(msg));
	}

	@Override
	public void error(Params p, Exception e) {
		handler.error(-1, e);
	}
}
