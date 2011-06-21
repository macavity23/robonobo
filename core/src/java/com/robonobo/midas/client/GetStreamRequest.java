package com.robonobo.midas.client;

import java.util.*;

import com.robonobo.core.api.model.Stream;
import com.robonobo.core.api.proto.CoreApi.StreamMsg;
import com.robonobo.core.metadata.StreamCallback;
import com.robonobo.midas.client.Params.Operation;

public class GetStreamRequest implements Request {
	MidasClientConfig cfg;
	private StreamCallback handler;
	private Stack<String> sids = new Stack<String>();
	
	public GetStreamRequest(MidasClientConfig cfg, Collection<String> sids, StreamCallback handler) {
		this.cfg = cfg;
		this.sids.addAll(sids);
		this.handler = handler;
	}

	@Override
	public Params getNextParams() {
		String sid = sids.pop();
		return new Params(Operation.Get, null, StreamMsg.newBuilder(), cfg.getStreamUrl(sid), sid);
	}

	@Override
	public int remaining() {
		return sids.size();
	}
	
	@Override
	public void success(Object obj) {
		StreamMsg msg = (StreamMsg) obj;
		handler.success(new Stream(msg));
	}
	
	@Override
	public void error(Params p, Exception e) {
		String sid = (String) p.obj;
		handler.error(sid, e);
	}
}
