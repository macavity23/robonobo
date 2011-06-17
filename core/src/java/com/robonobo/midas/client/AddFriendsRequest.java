package com.robonobo.midas.client;

import java.util.Collection;

import com.robonobo.core.metadata.GeneralCallback;
import com.robonobo.midas.client.Params.Operation;

public class AddFriendsRequest implements Request {
	private MidasClientConfig cfg;
	private Collection<String> emails;
	private GeneralCallback callback;

	public AddFriendsRequest(MidasClientConfig cfg, Collection<String> emails, GeneralCallback callback) {
		this.cfg = cfg;
		this.emails = emails;
		this.callback = callback;
	}

	@Override
	public int remaining() {
		if(emails == null)
			return 0;
		return 1;
	}

	@Override
	public Params getNextParams() {
		Params p = new Params(Operation.Get, null, null, cfg.getAddFriendsUrl(emails), emails);
		emails = null;
		return p;
	}

	@Override
	public void success(Object obj) {
		callback.success();
	}

	@Override
	public void error(Params p, Exception e) {
		callback.error(e);
	}
}
