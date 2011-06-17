package com.robonobo.midas.client;

import java.util.Collection;
import java.util.Stack;

import com.robonobo.core.api.model.User;
import com.robonobo.core.api.proto.CoreApi.StreamMsg;
import com.robonobo.core.api.proto.CoreApi.UserMsg;
import com.robonobo.core.metadata.UserCallback;
import com.robonobo.midas.client.Params.Operation;

public class GetUsersRequest implements Request {
	MidasClientConfig cfg;
	private UserCallback handler;
	private Stack<Long> uids = new Stack<Long>();
	
	public GetUsersRequest(MidasClientConfig cfg, Collection<Long> uids, UserCallback handler) {
		this.cfg = cfg;
		this.uids.addAll(uids);
		this.handler = handler;
	}

	public GetUsersRequest(MidasClientConfig cfg, long uid, UserCallback handler) {
		this.cfg = cfg;
		this.uids.add(uid);
		this.handler = handler;
	}

	@Override
	public Params getNextParams() {
		Long uid = uids.pop();
		return new Params(Operation.Get, null, UserMsg.newBuilder(), cfg.getUserUrl(uid), uid);
	}

	@Override
	public int remaining() {
		return uids.size();
	}
	
	@Override
	public void success(Object obj) {
		UserMsg msg = (UserMsg) obj;
		handler.success(new User(msg));
	}
	
	@Override
	public void error(Params p, Exception e) {
		Long uid = (Long) p.obj;
		handler.error(uid, e);
	}
}
