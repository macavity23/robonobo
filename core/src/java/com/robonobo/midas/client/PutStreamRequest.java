package com.robonobo.midas.client;

import java.util.Collection;
import java.util.Stack;

import com.robonobo.core.api.model.Stream;
import com.robonobo.core.metadata.StreamHandler;
import com.robonobo.midas.client.Params.Operation;

public class PutStreamRequest implements Request {
	MidasClientConfig cfg;
	StreamHandler handler;
	Stack<Stream> streams = new Stack<Stream>();

	public PutStreamRequest(MidasClientConfig cfg, Collection<Stream> streams, StreamHandler handler) {
		this.cfg = cfg;
		this.handler = handler;
		this.streams.addAll(streams);
	}

	public PutStreamRequest(MidasClientConfig cfg, Stream s, StreamHandler handler) {
		this.cfg = cfg;
		this.handler = handler;
		this.streams.add(s);
	}

	@Override
	public Params getNextParams() {
		Stream s = streams.pop();
		return new Params(Operation.Put, s.toMsg(), null, cfg.getStreamUrl(s.getStreamId()), s);
	}

	@Override
	public int remaining() {
		return streams.size();
	}

	@Override
	public void success(Object obj) {
		if(handler != null)
			handler.success(null);
	}

	@Override
	public void error(Params p, Exception e) {
		if (handler != null) {
			Stream s = (Stream) p.obj;
			handler.error(s.getStreamId(), e);
		}
	}

}
